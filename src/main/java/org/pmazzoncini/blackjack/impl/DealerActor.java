package org.pmazzoncini.blackjack.impl;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.pmazzoncini.blackjack.impl.model.FrenchDeck.newDeck;
import static org.pmazzoncini.blackjack.impl.model.Game.GameResult;
import static org.pmazzoncini.blackjack.impl.model.Game.GameResult.LOST;
import static org.pmazzoncini.blackjack.impl.model.Game.GameResult.RETIRED;
import static org.pmazzoncini.blackjack.impl.model.Game.GameResult.TIE;
import static org.pmazzoncini.blackjack.impl.model.Game.GameResult.WON;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.pmazzoncini.blackjack.impl.model.Card;
import org.pmazzoncini.blackjack.impl.model.Game;
import org.pmazzoncini.blackjack.impl.model.Player;
import org.pmazzoncini.blackjack.impl.model.Round;
import scala.PartialFunction;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

/**
 * Actor tha models a dealer behaviour. <br> The dealer behave differently for each game phase
 * <ul>
 * <li>
 * waiting for player phase: In this phase the dealer waits for players' subscription<br> When a subscription arrives switches to bet
 * phase.</li>
 * <li>
 * <b>bet phase</b>: In this phase the dealer asks players for bets, waiting some seconds. <br>
 * When bets are received the dealer switches to <b>in game phase</b>. <br> If no bets are received and there are subscribed players he
 * restarts <b>bet phase</b><br> If no bets are received and there aren't any subscribed player he switches to <b>waiting for player
 * phase</b>.
 * </li>
 * <li>
 * <b>in game phase</b>: In this phase the dealer deals cards to every player and himself. <br>
 * Players will play one at a time. When all players have played the dealer plays then if the are subscribed players he switches to <b>bet
 * phase</b> otherwise he switches to <b>waiting for player phase</b>.
 * </li>
 * </ul>
 */
public class DealerActor extends AbstractActor {

    static final ArrayList<Game> EMPTY_LIST = new ArrayList<>();
    private static final String START_GAME = "startGame";
    protected final List<Card> cards = new ArrayList<>();
    private final LoggingAdapter log = Logging.getLogger(context().system(), this);
    private final List<Player> players = new ArrayList<>();
    private final TreeSet<Game> currentGames = new TreeSet<>(Game::scoreComparator);
    private final List<Game> completedGames = new ArrayList<>();
    private Game myGame;
    private Game currentlyPlayedGame;
    private Round currentRound;
    private long dealerStash = 10000L;
    private String myName;

    @Override
    public void preStart() throws Exception {
        super.preStart();

        myName = self().path().name();
    }

    @Override
    public Receive createReceive() {
        return waitingForPlayersPhase();
    }

    /**
     * @return the bet phase {@link scala.PartialFunction} that models the actor behaviour
     */
    private PartialFunction<Object, BoxedUnit> betPhase() {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> dealer {} entering in betPhase <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", myName);
        currentGames.clear();
        completedGames.clear();
        newShuffledDeck();

        myGame = Game.dealer();
        Card draw = draw();
        log.debug("Drawn face down card for dealer {}: {}", myName, draw);
        myGame.cardDrawn(draw);

        currentRound = new Round(self().path().name());

        askForBets();

        log.debug("dealer {} asked for bets and waits 5 seconds before starting game", myName);

        startGameAfter(5, SECONDS);

        return ReceiveBuilder.create()
            .match(String.class, (s -> {
                switch (s) {
                    case DealerPlayerContract.WANNA_PLAY:
                        // player is added but cannot bet in current round
                        addPlayer();
                        break;
                    case DealerPlayerContract.STOP_PLAY:
                        Player player = searchPlayer(sender());
                        retire(player);

                        Game game = searchGame(player);
                        if (game != null) {
                            gameLost(game, true);
                        }

                        break;
                    case START_GAME:
                        if (currentGames.isEmpty()) {
                            if (players.isEmpty()) {
                                become(waitingForPlayersPhase().onMessage());
                            } else {
                                become(betPhase());
                            }
                        } else {
                            become(inGamePhase());
                        }
                        break;
                    default:
                        replyToWrongMessage(s);
                        break;
                }
            }))
            .match(AddGame.class, addGame -> {
                log.debug("Adding game {} for player {}", addGame.game.getGameId(), addGame.game.getPlayerName());
                currentGames.add(addGame.game);
                currentRound.addGame(addGame.game);
            })
            .matchAny(this::replyToWrongMessage)
            .build()
            .onMessage();

    }

    /**
     * @return the in game phase {@link scala.PartialFunction} that models the actor behaviour
     */
    private PartialFunction<Object, BoxedUnit> inGamePhase() {
        // distribuire una carta per ogni game in connection order
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> dealer {} entering in gamePhase <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", myName);
        Set<Player> playingPlayers = currentGames.stream().map(Game::getPlayer).collect(toSet());

        log.debug("dealer {} dealing a faceUp card to every playing player", myName);
        dealACardToEveryPlayingPlayer(playingPlayers);

        Card dealerCard = draw();
        myGame.cardDrawn(dealerCard);
        log.debug("dealer {} drawn a face up card {}", myName, dealerCard);
        faceUpDealerCardDrawn(playingPlayers, dealerCard);

        log.debug("dealer {} dealing a faceUp card to every playing player", myName);
        dealACardToEveryPlayingPlayer(playingPlayers);

        nextPlayerTurn();
        return ReceiveBuilder.create()
            .match(String.class, stringMsg -> {
                switch (stringMsg) {
                    case DealerPlayerContract.WANNA_PLAY:
                        addPlayer();
                        break;
                    case DealerPlayerContract.STOP_PLAY:
                        Player player = searchPlayer(sender());
                        log.info("player {} is stopping playing", player.getRef().path().name());
                        retire(player);
                        Game game = searchGame(player);

                        if (game != null) {
                            if (game.equals(currentlyPlayedGame)) {
                                nextPlayerTurn();
                            }
                            gameLost(game, true);
                        }

                        if (currentGames.isEmpty()) {
                            if (players.isEmpty()) {
                                become(waitingForPlayersPhase().onMessage());
                            } else {
                                become(betPhase());
                            }
                        }
                        break;
                    default:
                        replyToWrongMessage(stringMsg);
                        break;
                }
            })
            .match(DealerPlayerContract.DealerRequest.class, request -> {
                switch (request.request()) {
                    case DealerPlayerContract.DealerRequest.HIT:

                        log.debug("dealer {}, received an HIT message", myName);

                        if (request.player().equals(currentlyPlayedGame.getPlayer().getRef())) {
                            Card card = draw();
                            currentlyPlayedGame.cardDrawn(card);
                            sendDrawnCardMessage(card);
                            if (currentlyPlayedGame.getScore() > 21) {
                                gameLost(currentlyPlayedGame);

                                nextPlayerTurn();

                            } else if (currentlyPlayedGame.getScore() == 21) {
                                currentlyPlayedGame.getPlayer().getRef().tell(DealerPlayerContract.MUST_STAND, self());
                                completedGames.add(currentlyPlayedGame);

                                nextPlayerTurn();
                            }
                        } else {
                            log.warning("Hit received from an unexepcted actor {}", sender().path().toSerializationFormat());
                            replyToWrongMessage(request);
                        }
                        break;
                    case DealerPlayerContract.DealerRequest.STAND:
                        if (request.player().equals(currentlyPlayedGame.getPlayer().getRef())) {
                            completedGames.add(currentlyPlayedGame);
                            nextPlayerTurn();

                        }
                        break;
                }
            })
            .matchAny(this::replyToWrongMessage)
            .build()
            .onMessage();
    }

    /**
     * @return the waiting for player phase {@link scala.PartialFunction} that models the actor behaviour
     */
    private Receive waitingForPlayersPhase() {
        log.info("dealer {} entering in waitingForPlayersPhase", myName);
        return ReceiveBuilder.create()
            .match(String.class, stringMsg -> {
                switch (stringMsg) {
                    case DealerPlayerContract.WANNA_PLAY:
                        addPlayer();
                        become(betPhase());
                        break;
                    default:
                        replyToWrongMessage(stringMsg);
                        break;
                }
            })
            .matchAny(this::replyToWrongMessage)
            .build();
    }

    private void startGameAfter(int length, TimeUnit seconds) {
        context().system().scheduler()
            .scheduleOnce(Duration.create(length, seconds), self(), START_GAME, getContext().system().dispatcher(), self());
    }

    private void askForBets() {
        ExecutionContextExecutor dispatcher = getContext().system().dispatcher();
        players.stream().forEach(player -> {
            Future<Object> bet = Patterns.ask(player.getRef(), DealerPlayerContract.PLEASE_BET, 5000L);
            bet.onSuccess(onReceivedBet(player), dispatcher);
            bet.onFailure(new OnFailure() {
                @Override
                public void onFailure(Throwable failure) throws Throwable {

                }
            }, dispatcher);
        });
    }

    private OnSuccess<Object> onReceivedBet(final Player player) {
        return new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object result) throws Throwable {
                if (result instanceof DealerPlayerContract.Bet) {
                    self().tell(new AddGame(new Game(player, ((DealerPlayerContract.Bet) result).getBet())), self());
                }
            }
        };
    }


    private void retire(Player player) {
        dealerStash += player.getPot();
        players.remove(player);
    }

    private void faceUpDealerCardDrawn(Set<Player> playingPlayers, Card dealerCard) {
        DealerPlayerContract.CardDrawn dealerCardMessage = new DealerPlayerContract.CardDrawn(dealerCard, true);
        playingPlayers.forEach(player -> player.getRef().tell(dealerCardMessage, self()));
    }

    private void nextPlayerTurn() {
        log.debug("currentGames size: {}", currentGames.size());
        currentlyPlayedGame = currentGames.pollFirst();
        log.debug("Currently played game: {}, currentGames remaining size: {}", currentlyPlayedGame, currentGames.size());
        if (currentlyPlayedGame != null) {
            currentlyPlayedGame.getPlayer().getRef().tell(DealerPlayerContract.YOUR_TURN, self());
        } else {
            dealerTurn();
            calculateResults();

            saveRound();

            if (players.isEmpty()) {
                become(waitingForPlayersPhase().onMessage());
            } else {
                become(betPhase());
            }
        }
    }

    /**
     * Save round data for future reference
     */
    protected void saveRound() {
        currentRound.setEndDate(new Date());
        currentRound.setDealerScore(myGame.getScore());
        log.debug("Saving round: {}", currentRound);
        GameHistory.instance().saveCompletedRound(currentRound);
    }


    private void dealACardToEveryPlayingPlayer(Set<Player> playingPlayers) {
        // Using players to maintain connection order while dealing cards
        players.stream().filter(playingPlayers::contains).map(this::searchGame).forEach(game -> {
            Card card = draw();
            log.debug("Dealer {} drawn card [{}] for player {} in game {}", myName, card, game.getPlayerName(), game.getGameId());
            game.cardDrawn(card);
            sendDrawnCardMessageTo(card, game.getPlayer());
        });
    }

    private void sendDrawnCardMessage(Card card) {
        sender().tell(new DealerPlayerContract.CardDrawn(card), self());
    }

    private void sendDrawnCardMessageTo(Card card, Player player) {
        player.getRef().tell(new DealerPlayerContract.CardDrawn(card), self());
    }

    private void dealerTurn() {
        Set<Player> inGamePlayers = completedGames.stream().map(Game::getPlayer).collect(toSet());

        while (myGame.getScore() < 16) {
            Card dealerCard = draw();
            myGame.cardDrawn(dealerCard);
            faceUpDealerCardDrawn(inGamePlayers, dealerCard);
        }
    }

    private void calculateResults() {

        if (myGame.getScore() > 21) {
            completedGames.stream().forEach(this::gameWon);
        }

        Map<GameResult, List<Game>> gameResults = completedGames.stream().collect(groupingBy(game -> {
            if (game.getScore() > myGame.getScore()) {
                return WON;
            } else if (game.getScore() == myGame.getScore()) {
                return TIE;
            } else {
                return LOST;
            }
        }));

        gameResults.getOrDefault(WON, EMPTY_LIST).stream().forEach(this::gameWon);
        gameResults.getOrDefault(LOST, EMPTY_LIST).stream().forEach(this::gameLost);
        gameResults.getOrDefault(TIE, EMPTY_LIST).stream().forEach(this::gameTied);
    }

    private void addPlayer() {
        ActorRef sender = sender();
        log.debug("Adding player {} for next game with {} dealer", sender.path().name(), myName);
        players.add(new Player(sender));

    }

    private void gameLost(Game gameLost) {
        gameLost(gameLost, false);
    }

    private void gameLost(Game gameLost, boolean retired) {
        if (!retired) {
            log.debug("player {} has lost game [{}] with dealer {}", gameLost.getPlayerName(), gameLost.getGameId(), myName);
            gameLost.setGameResult(LOST);
        } else {
            log.debug("player {} has retired from game [{}] with dealer {}", gameLost.getPlayerName(), gameLost.getGameId(), myName);
            gameLost.setGameResult(RETIRED);
        }
        dealerStash += gameLost.getBet() + gameLost.getPlayer().getPot();
        gameLost.getPlayer().setPot(0L);
        ActorRef playerRef = gameLost.getPlayer().getRef();
        playerRef.tell(DealerPlayerContract.YOU_LOST, self());
    }

    private void gameWon(Game gameWon) {
        log.debug("player {} has won game [{}] with dealer {}", gameWon.getPlayerName(), gameWon.getGameId(), myName);
        gameWon.setGameResult(WON);
        dealerStash -= gameWon.getBet();
        Player player = gameWon.getPlayer();
        player.getRef().tell(new DealerPlayerContract.YouWon(gameWon.getBet() * 2 + player.getPot()), self());
        player.setPot(0L);
    }

    private void gameTied(Game gameTied) {
        log.debug("player {} has tied game [{}] with dealer {}", gameTied.getPlayerName(), gameTied.getGameId(), myName);
        gameTied.setGameResult(TIE);
        gameTied.getPlayer().setPot(gameTied.getPlayer().getPot() + gameTied.getBet());
        ActorRef playerRef = gameTied.getPlayer().getRef();
        playerRef.tell(DealerPlayerContract.TIED_GAME, self());
    }

    protected void newShuffledDeck() {
        cards.clear();
        cards.addAll(newDeck());
        cards.addAll(newDeck());
        Collections.shuffle(cards);
    }

    private Card draw() {
        return cards.remove(0);
    }

    private Player searchPlayer(ActorRef ref) {
        return players.stream().filter(player -> player.getRef().equals(ref)).findAny().orElse(null);
    }

    private Game searchGame(Player player) {
        return currentGames.stream().filter(game -> game.getPlayer().equals(player)).findAny().orElse(null);
    }

    private void replyToWrongMessage(Object mess) {
        sender().tell(new DealerPlayerContract.WrongMessage(mess), self());
    }

    private void become(PartialFunction<Object, BoxedUnit> behavior) {
        context().become(behavior);
    }

    /**
     * The AddGame self-message class
     */
    private static class AddGame {

        private final Game game;

        private AddGame(Game game) {
            this.game = game;
        }
    }


}

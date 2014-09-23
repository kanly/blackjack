package org.pmazzoncini.blackjack.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import org.pmazzoncini.blackjack.impl.model.Card;
import org.pmazzoncini.blackjack.impl.model.Game;
import org.pmazzoncini.blackjack.impl.model.Player;
import scala.PartialFunction;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.pmazzoncini.blackjack.impl.model.FrenchDeck.newDeck;

public class DealerActor extends AbstractActor {
    private static final String START_GAME = "startGame";
    public static final ArrayList<Game> EMPTY_LIST = new ArrayList<>();

    protected final List<Card> cards = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private final TreeSet<Game> currentGames = new TreeSet<>((game, otherGame) -> game.getScore() - otherGame.getScore());
    private final List<Game> completedGames = new ArrayList<>();
    private Game myGame;
    private Game currentlyPlayedGame;
    private long dealerStash = 10000L;

    public DealerActor() {
        receive(waitingForPlayersPhase());
    }

    private PartialFunction<Object, BoxedUnit> betPhase() {
        newShuffledDeck();

        myGame = Game.dealer();
        myGame.cardDrawn(draw());

        askForBets();

        startGameAfter(5, SECONDS);

        return ReceiveBuilder
                .match(String.class, (s -> {
                    switch (s) {
                        case DealerMessages.WANNA_PLAY:
                            // player is added but cannot bet in current round
                            addPlayer();
                            break;
                        case DealerMessages.STOP_PLAY:
                            Player player = searchPlayer(sender());
                            retire(player);

                            Game game = searchGame(player);
                            if (game != null) {
                                gameLost(game);
                            }

                            break;
                        case START_GAME:
                            if (currentGames.isEmpty()) {
                                if (players.isEmpty()) {
                                    become(waitingForPlayersPhase());
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
                .match(AddGame.class, addGame -> currentGames.add(addGame.game))
                .matchAny(this::replyToWrongMessage)
                .build();

    }

    private PartialFunction<Object, BoxedUnit> inGamePhase() {
        // distribuire una carta per ogni game in connection order
        Set<Player> playingPlayers = currentGames.stream().map(Game::getPlayer).collect(toSet());

        dealACardToEveryPlayingPlayer(playingPlayers);

        Card dealerCard = draw();
        myGame.cardDrawn(dealerCard);
        faceUpDealerCardDrawn(playingPlayers, dealerCard);

        dealACardToEveryPlayingPlayer(playingPlayers);

        nextPlayerTurn();
        return ReceiveBuilder
                .match(String.class, stringMsg -> {
                    switch (stringMsg) {
                        case DealerMessages.HIT:
                            if (sender().equals(currentlyPlayedGame.getPlayer().getRef())) {
                                Card card = draw();
                                currentlyPlayedGame.cardDrawn(card);
                                sendDrawnCardMessage(card, currentlyPlayedGame.getPlayer());
                                if (currentlyPlayedGame.getScore() > 21) {
                                    gameLost(currentlyPlayedGame);

                                    nextPlayerTurn();

                                } else if (currentlyPlayedGame.getScore() == 21) {
                                    currentlyPlayedGame.getPlayer().getRef().tell(DealerMessages.MUST_STAND, self());
                                    completedGames.add(currentlyPlayedGame);

                                    nextPlayerTurn();
                                }
                            } else {
                                replyToWrongMessage(stringMsg);
                            }
                            break;
                        case DealerMessages.STAND:
                            if (sender().equals(currentlyPlayedGame.getPlayer().getRef())) {
                                completedGames.add(currentlyPlayedGame);
                                nextPlayerTurn();

                            } else {
                                replyToWrongMessage(stringMsg);
                            }
                            break;
                        case DealerMessages.WANNA_PLAY:
                            addPlayer();
                            break;
                        case DealerMessages.STOP_PLAY:
                            Player player = searchPlayer(sender());
                            retire(player);
                            Game game = searchGame(player);

                            if (game != null) {
                                if (game.equals(currentlyPlayedGame)) {
                                    nextPlayerTurn();
                                }
                                gameLost(game);
                            }

                            if (currentGames.isEmpty()) {
                                if (players.isEmpty()) {
                                    become(waitingForPlayersPhase());
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
                .matchAny(this::replyToWrongMessage)
                .build();
    }

    private PartialFunction<Object, BoxedUnit> waitingForPlayersPhase() {
        return ReceiveBuilder
                .match(String.class, stringMsg -> {
                    switch (stringMsg) {
                        case DealerMessages.WANNA_PLAY:
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
        context().system().scheduler().scheduleOnce(Duration.create(length, seconds), self(), START_GAME, getContext().system().dispatcher(), self());
    }

    private void askForBets() {
        ExecutionContextExecutor dispatcher = getContext().system().dispatcher();
        players.stream().forEach(player -> {
            Future<Object> bet = Patterns.ask(player.getRef(), DealerMessages.PLEASE_BET, 5000L);
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
                if (result instanceof DealerMessages.Bet) {
                    self().tell(new AddGame(new Game(player, ((DealerMessages.Bet) result).getBet())), self());
                }
            }
        };
    }


    private void retire(Player player) {
        dealerStash += player.getPot();
        players.remove(player);
    }

    private void faceUpDealerCardDrawn(Set<Player> playingPlayers, Card dealerCard) {
        DealerMessages.CardDrawn dealerCardMessage = new DealerMessages.CardDrawn(dealerCard, true);
        playingPlayers.forEach(player -> player.getRef().tell(dealerCardMessage, self()));
    }

    private void nextPlayerTurn() {
        currentlyPlayedGame = currentGames.pollFirst();
        if (currentlyPlayedGame != null) {
            currentlyPlayedGame.getPlayer().getRef().tell(DealerMessages.YOUR_TURN, self());
        } else {
            dealerTurn();
            calculateResults();

            if (players.isEmpty()) {
                become(waitingForPlayersPhase());
            } else {
                become(betPhase());
            }
        }
    }


    private void dealACardToEveryPlayingPlayer(Set<Player> playingPlayers) {
        // Using players to maintain connection order while dealing cards
        players.stream().filter(playingPlayers::contains).map(this::searchGame).forEach(game -> {
            Card card = draw();
            game.cardDrawn(card);
            sendDrawnCardMessage(card, game.getPlayer());
        });
    }

    private void sendDrawnCardMessage(Card card, Player player) {
        player.getRef().tell(new DealerMessages.CardDrawn(card), self());
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
                return GameResult.WON;
            } else if (game.getScore() == myGame.getScore()) {
                return GameResult.TIE;
            } else {
                return GameResult.LOST;
            }
        }));

        gameResults.getOrDefault(GameResult.WON, EMPTY_LIST).stream().forEach(this::gameWon);
        gameResults.getOrDefault(GameResult.LOST, EMPTY_LIST).stream().forEach(this::gameLost);
        gameResults.getOrDefault(GameResult.TIE, EMPTY_LIST).stream().forEach(this::gameTied);
    }

    private void addPlayer() {
        ActorRef sender = sender();
        players.add(new Player(sender));

    }

    private void gameLost(Game gameLost) {
        dealerStash += gameLost.getBet() + gameLost.getPlayer().getPot();
        gameLost.getPlayer().setPot(0L);
        ActorRef playerRef = gameLost.getPlayer().getRef();
        playerRef.tell(DealerMessages.YOU_LOST, self());
    }

    private void gameWon(Game gameWon) {
        dealerStash -= gameWon.getBet();
        Player player = gameWon.getPlayer();
        player.getRef().tell(new DealerMessages.YouWon(gameWon.getBet() + player.getPot()), self());
        player.setPot(0L);
    }

    private void gameTied(Game gameTied) {
        gameTied.getPlayer().setPot(gameTied.getPlayer().getPot() + gameTied.getBet());
        ActorRef playerRef = gameTied.getPlayer().getRef();
        playerRef.tell(DealerMessages.TIED_GAME, self());
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
        sender().tell(new DealerMessages.WrongMessage(mess), self());
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

    enum GameResult {
        WON, TIE, LOST
    }

}

package org.pmazzoncini.blackjack.impl;

import static akka.pattern.Patterns.ask;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.Bet;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.CardDrawn;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.MUST_STAND;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.PLEASE_BET;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.STOP_PLAY;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.TIED_GAME;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.WrongMessage;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.YOUR_TURN;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.YOU_LOST;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.YouWon;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import java.util.Random;
import scala.concurrent.Future;

/**
 * Actor that models a blackjack player behaviour. <br> By default it uses same rules of the dealer (stand when points &gt;15)
 */
public class PlayerActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);
    // the Dealer of this player table
    private final ActorRef myDealer;
    private final Random rnd = new Random();
    private long stash = 1000;
    private long pot = 0L;
    private int currentPoints = 0;
    private long lastBet = 0L;
    private String myName;

    public PlayerActor(ActorRef myDealer) {
        this.myDealer = myDealer;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        myName = self().path().name();

        log.info("{} wants to play, he is subscribing to dealer {}", myName, dealer().path().name());
        dealer().tell(DealerPlayerContract.WANNA_PLAY, self());
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
            .match(String.class, msg -> {
                    switch (msg) {
                        case PLEASE_BET:
                            log.debug("dealer has asked {} a bet", myName);
                            int bet = rnd.nextInt((int) stash / 2);
                            log.debug("{} bets {}", myName, bet);
                            stash -= bet;
                            sender().tell(new Bet(bet), self());
                            break;
                        case YOUR_TURN:
                            log.debug("It's {} turn! He/she has {} points", myName, currentPoints);
                            doPlay();
                            break;
                        case YOU_LOST:
                            log.debug("{} has lost the game, now his stash is {}", myName, stash);
                            prepareForNextRound();
                            break;
                        case TIED_GAME:
                            pot += lastBet;
                            log.debug("{} has tied a game, now his pot is {} and his stash is {}", myName, pot, stash);
                            prepareForNextRound();
                            break;
                        case MUST_STAND:
                            break;
                    }
                }
            )
            .match(CardDrawn.class, this::cardDrawn)
            .match(YouWon.class, message -> {
                stash = stash + message.getAmount();

                log.debug("{} has won a game, winning {} now his stash is {}", myName, message.getAmount(), stash);

                prepareForNextRound();
            })
            .build();
    }

    @Override
    public void postStop() throws Exception {
        log.debug("{} stopped!", myName);
        super.postStop();
    }

    protected ActorRef dealer() {
        return myDealer;
    }

    /**
     * Reset all match related variables <br> If this player runs out of money he will stop playing and stop this actor
     */
    private void prepareForNextRound() {
        currentPoints = 0;
        lastBet = 0L;
        if (stash < 100L) {
            log.info("{} is out of money. He's stopping playing and taking a poison pill!", myName);
            dealer().tell(STOP_PLAY, self());
            self().tell(PoisonPill.getInstance(), self());
            context()
                .become(ReceiveBuilder.create().matchAny(m -> log.debug("arrived message while shutting dowm {}", m)).build().onMessage());
        } else {
            log.info("Preparing for next round: stash [{}], pot [{}]", stash, pot);
        }
    }

    /**
     * Implements default play logic
     */
    protected void doPlay() {
        log.debug("DoPlay {} currentPoints {}", myName, currentPoints);
        if (currentPoints < 16) {
            log.debug("{} hits", myName);
            Future<Object> nextCard = ask(dealer(), DealerPlayerContract.DealerRequest.hit(self()), 3000L);
            nextCard.onSuccess(onReceiveCard(), getContext().system().dispatcher());
        } else {
            log.debug("{} stands", myName);
            dealer().tell(DealerPlayerContract.DealerRequest.stand(self()), self());
        }
    }

    /**
     * The logic to be used when a card is received
     *
     * @return {@link akka.dispatch.OnSuccess} object that handles
     */
    private OnSuccess<Object> onReceiveCard() {
        return new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object result) throws Throwable {
                if (result instanceof CardDrawn) {
                    CardDrawn drawn = (CardDrawn) result;

                    cardDrawn(drawn);
                    doPlay();
                } else if (result instanceof WrongMessage) {
                    log.warning("{} Received a Wrong message message", myName);
                }
            }
        };
    }

    /**
     * Handles the card drawn event
     */
    private void cardDrawn(CardDrawn cardDrawn) {
        if (!cardDrawn.isDealer()) {
            log.debug("player {} received a {}", myName, cardDrawn.getCard());
            int cardValue = cardDrawn.getCard().getValue();
            currentPoints += cardValue;
        }
    }
}

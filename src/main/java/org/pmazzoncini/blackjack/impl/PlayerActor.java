package org.pmazzoncini.blackjack.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.dispatch.OnSuccess;
import akka.japi.pf.ReceiveBuilder;
import scala.concurrent.Future;

import java.util.Random;

import static akka.pattern.Patterns.ask;
import static org.pmazzoncini.blackjack.impl.DealerMessages.*;

public class PlayerActor extends AbstractActor {
    private long stash = 1000L;
    private long pot = 0L;
    private int currentPoints = 0;
    private long lastBet = 0L;

    @Override
    public void preStart() throws Exception {
        super.preStart();

        dealer().tell(DealerMessages.WANNA_PLAY, self());
    }

    public PlayerActor() {
        receive(ReceiveBuilder
                        .match(String.class, msg -> {
                                    switch (msg) {
                                        case PLEASE_BET:
                                            long bet = new Random(stash).nextLong();
                                            stash -= bet;
                                            sender().tell(new Bet(bet), self());
                                            break;
                                        case YOUR_TURN:
                                            doPlay();
                                            break;
                                        case YOU_LOST:
                                            prepareForNextRound();
                                            break;
                                        case TIED_GAME:
                                            pot += lastBet;
                                            prepareForNextRound();
                                            break;
                                        case MUST_STAND:
                                            break;
                                    }
                                }
                        )
                        .match(CardDrawn.class, this::cardDrawn)
                        .match(YouWon.class, message -> {
                            stash += message.getAmount();
                            prepareForNextRound();
                        })
                        .build()
        );
    }

    protected ActorRef dealer() {
        return System.dealer;
    }

    private void prepareForNextRound() {
        currentPoints = 0;
        lastBet = 0L;
        if (stash < 100L) {
            dealer().tell(STOP_PLAY, self());
            self().tell(PoisonPill.getInstance(), self());
        }
    }

    protected void doPlay() {
        if (currentPoints < 16) {
            Future<Object> nextCard = ask(dealer(), HIT, 3000L);
            nextCard.onSuccess(onReceiveCard(), getContext().system().dispatcher());
        } else if (currentPoints <= 21) {
            dealer().tell(STAND, self());
        }
    }

    private OnSuccess<Object> onReceiveCard() {
        return new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object result) throws Throwable {
                cardDrawn((CardDrawn) result);
                doPlay();
            }
        };
    }

    private void cardDrawn(CardDrawn cardDrawn) {
        int cardValue = cardDrawn.getCard().getValue();
        currentPoints += cardValue;
    }
}

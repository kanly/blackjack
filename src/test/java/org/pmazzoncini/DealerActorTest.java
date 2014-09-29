package org.pmazzoncini;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pmazzoncini.blackjack.impl.DealerActor;
import org.pmazzoncini.blackjack.impl.DealerMessages;
import org.pmazzoncini.blackjack.impl.model.Card;

import static org.pmazzoncini.blackjack.impl.DealerMessages.*;
import static org.pmazzoncini.blackjack.impl.model.FrenchDeck.*;

public class DealerActorTest {
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void singlePlayerLose() {
        new JavaTestKit(system) {{
            Props props = Props.create(DealerActor.class, () -> new DealerActor() {
                @Override
                protected void newShuffledDeck() {
                    super.cards.add(new Card(DIAMONDS, ACE)); //dealer
                    super.cards.add(new Card(DIAMONDS, "2")); //player
                    super.cards.add(new Card(DIAMONDS, "8")); //dealer
                    super.cards.add(new Card(DIAMONDS, "6")); //player
                    super.cards.add(new Card(DIAMONDS, "5")); //player
                    super.cards.add(new Card(DIAMONDS, "7")); //player (20)
                    super.cards.add(new Card(SPADES, "2")); //dealer
                    super.cards.add(new Card(DIAMONDS, QUEEN));//dealer (21)
                }

                @Override
                protected void saveRound() {

                }
            });
            ActorRef dealer = system.actorOf(props);

            dealer.tell(WANNA_PLAY, getRef());

            expectMsgEquals(duration("15 seconds"), PLEASE_BET);
            getLastSender().tell(new Bet(50), getRef());

            expectMsgClass(duration("15 seconds"), CardDrawn.class);
            expectMsgClass(duration("5 seconds"), CardDrawn.class);
            expectMsgClass(duration("5 seconds"), CardDrawn.class);
            expectMsgEquals(duration("5 seconds"), YOUR_TURN);

            dealer.tell(DealerMessages.DealerRequest.hit(getRef()), getRef());
            expectMsgClass(duration("5 seconds"), CardDrawn.class);

            dealer.tell(DealerMessages.DealerRequest.hit(getRef()), getRef());
            expectMsgClass(duration("5 seconds"), CardDrawn.class);

            dealer.tell(DealerMessages.DealerRequest.stand(getRef()), getRef());

            expectMsgClass(duration("5 seconds"), CardDrawn.class);
            expectMsgClass(duration("5 seconds"), CardDrawn.class);

            expectMsgEquals(duration("5 seconds"), YOU_LOST);

            expectMsgEquals(duration("5 seconds"), PLEASE_BET);

            dealer.tell(STOP_PLAY, getRef());

            expectNoMsg(duration("3 seconds"));
        }};
    }


    @Test
    public void singlePLayerWon() {
        new JavaTestKit(system) {{
            Props props = Props.create(DealerActor.class, () -> new DealerActor() {
                @Override
                protected void newShuffledDeck() {
                    super.cards.add(new Card(DIAMONDS, ACE)); //dealer
                    super.cards.add(new Card(DIAMONDS, "2")); //player
                    super.cards.add(new Card(DIAMONDS, "8")); //dealer
                    super.cards.add(new Card(DIAMONDS, "6")); //player
                    super.cards.add(new Card(DIAMONDS, "5")); //player
                    super.cards.add(new Card(DIAMONDS, "7")); //player (20)
                    super.cards.add(new Card(SPADES, "2")); //dealer
                    super.cards.add(new Card(HEARTS, "8"));//dealer (19)
                }

                @Override
                protected void saveRound() {

                }
            });
            ActorRef dealer = system.actorOf(props);

            dealer.tell(WANNA_PLAY, getRef());

            expectMsgEquals(duration("15 seconds"), PLEASE_BET);
            getLastSender().tell(new Bet(50), getRef());

            expectMsgClass(duration("15 seconds"), CardDrawn.class);
            expectMsgClass(duration("5 seconds"), CardDrawn.class);
            expectMsgClass(duration("5 seconds"), CardDrawn.class);
            expectMsgEquals(duration("5 seconds"), YOUR_TURN);

            dealer.tell(DealerMessages.DealerRequest.hit(getRef()), getRef());
            expectMsgClass(duration("5 seconds"), CardDrawn.class);

            dealer.tell(DealerMessages.DealerRequest.hit(getRef()), getRef());
            expectMsgClass(duration("5 seconds"), CardDrawn.class);

            dealer.tell(DealerMessages.DealerRequest.stand(getRef()), getRef());

            expectMsgClass(duration("5 seconds"), CardDrawn.class);
            expectMsgClass(duration("5 seconds"), CardDrawn.class);

            expectMsgEquals(duration("5 seconds"), new YouWon(50L));

            expectMsgEquals(duration("5 seconds"), PLEASE_BET);

            dealer.tell(STOP_PLAY, getRef());

            expectNoMsg(duration("3 seconds"));

        }};
    }

    @Test
    public void singlePLayerTie() {
        new JavaTestKit(system) {{
            Props props = Props.create(DealerActor.class, () -> new DealerActor() {
                @Override
                protected void newShuffledDeck() {
                    super.cards.add(new Card(DIAMONDS, ACE)); //dealer
                    super.cards.add(new Card(DIAMONDS, "2")); //player
                    super.cards.add(new Card(DIAMONDS, "8")); //dealer
                    super.cards.add(new Card(DIAMONDS, "6")); //player
                    super.cards.add(new Card(DIAMONDS, "5")); //player
                    super.cards.add(new Card(DIAMONDS, "7")); //player (20)
                    super.cards.add(new Card(SPADES, "2")); //dealer
                    super.cards.add(new Card(HEARTS, "9"));//dealer (20)
                }

                @Override
                protected void saveRound() {

                }
            });
            ActorRef dealer = system.actorOf(props);

            dealer.tell(WANNA_PLAY, getRef());

            expectMsgEquals(duration("15 seconds"), PLEASE_BET);
            getLastSender().tell(new Bet(50), getRef());

            expectMsgClass(duration("15 seconds"), CardDrawn.class);
            expectMsgClass(duration("5 seconds"), CardDrawn.class);
            expectMsgClass(duration("5 seconds"), CardDrawn.class);
            expectMsgEquals(duration("5 seconds"), YOUR_TURN);

            dealer.tell(DealerMessages.DealerRequest.hit(getRef()), getRef());
            expectMsgClass(duration("5 seconds"), CardDrawn.class);

            dealer.tell(DealerMessages.DealerRequest.hit(getRef()), getRef());
            expectMsgClass(duration("5 seconds"), CardDrawn.class);

            dealer.tell(DealerMessages.DealerRequest.stand(getRef()), getRef());

            expectMsgClass(duration("5 seconds"), CardDrawn.class);
            expectMsgClass(duration("5 seconds"), CardDrawn.class);

            expectMsgEquals(duration("5 seconds"), TIED_GAME);

            expectMsgEquals(duration("5 seconds"), PLEASE_BET);

            dealer.tell(STOP_PLAY, getRef());

            expectNoMsg(duration("3 seconds"));

        }};
    }
}

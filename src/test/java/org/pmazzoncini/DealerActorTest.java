package org.pmazzoncini;


import static org.junit.Assert.assertTrue;
import static org.pmazzoncini.Durations.in15Seconds;
import static org.pmazzoncini.Durations.in3Seconds;
import static org.pmazzoncini.Durations.in5Seconds;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.Bet;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.CardDrawn;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.PLEASE_BET;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.STOP_PLAY;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.TIED_GAME;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.WANNA_PLAY;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.YOUR_TURN;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.YOU_LOST;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.YouWon;
import static org.pmazzoncini.blackjack.impl.model.FrenchDeck.ACE;
import static org.pmazzoncini.blackjack.impl.model.FrenchDeck.DIAMONDS;
import static org.pmazzoncini.blackjack.impl.model.FrenchDeck.HEARTS;
import static org.pmazzoncini.blackjack.impl.model.FrenchDeck.QUEEN;
import static org.pmazzoncini.blackjack.impl.model.FrenchDeck.SPADES;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pmazzoncini.blackjack.impl.DealerActor;
import org.pmazzoncini.blackjack.impl.DealerPlayerContract;
import org.pmazzoncini.blackjack.impl.model.Card;

public class DealerActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system, in15Seconds, true);
        system = null;
    }

    @Test
    public void singlePlayerLose() {
        new TestKit(system) {{
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

            dealer.tell(WANNA_PLAY, testActor());

            expectMsg(in15Seconds, PLEASE_BET);
            lastSender().tell(new Bet(50), testActor());

            expectMsgClass(in15Seconds, CardDrawn.class);
            expectMsgClass(in5Seconds, CardDrawn.class);
            expectMsgClass(in5Seconds, CardDrawn.class);
            expectMsg(in5Seconds, YOUR_TURN);

            dealer.tell(DealerPlayerContract.DealerRequest.hit(testActor()), testActor());
            expectMsgClass(in5Seconds, CardDrawn.class);

            dealer.tell(DealerPlayerContract.DealerRequest.hit(testActor()), testActor());
            expectMsgClass(in5Seconds, CardDrawn.class);

            dealer.tell(DealerPlayerContract.DealerRequest.stand(testActor()), testActor());

            expectMsgClass(in5Seconds, CardDrawn.class);
            expectMsgClass(in5Seconds, CardDrawn.class);

            expectMsg(in5Seconds, YOU_LOST);

            expectMsg(in5Seconds, PLEASE_BET);

            dealer.tell(STOP_PLAY, testActor());

            expectNoMsg(in3Seconds);
        }};
    }


    @Test
    public void singlePLayerWon() {
        new TestKit(system) {{
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

            dealer.tell(WANNA_PLAY, testActor());

            expectMsg(in15Seconds, PLEASE_BET);
            lastSender().tell(new Bet(50), testActor());

            expectMsgClass(in15Seconds, CardDrawn.class);
            expectMsgClass(in5Seconds, CardDrawn.class);
            expectMsgClass(in5Seconds, CardDrawn.class);
            expectMsg(in5Seconds, YOUR_TURN);

            dealer.tell(DealerPlayerContract.DealerRequest.hit(testActor()), testActor());
            expectMsgClass(in5Seconds, CardDrawn.class);

            dealer.tell(DealerPlayerContract.DealerRequest.hit(testActor()), testActor());
            expectMsgClass(in5Seconds, CardDrawn.class);

            dealer.tell(DealerPlayerContract.DealerRequest.stand(testActor()), testActor());

            expectMsgClass(in5Seconds, CardDrawn.class);
            expectMsgClass(in5Seconds, CardDrawn.class);

            YouWon youWon = expectMsgClass(YouWon.class);
            assertTrue("should've won 50", youWon.equals(new YouWon(100L)));

            expectMsg(in5Seconds, PLEASE_BET);

            dealer.tell(STOP_PLAY, testActor());

            expectNoMsg(in3Seconds);

        }};
    }

    @Test
    public void singlePLayerTie() {
        new TestKit(system) {{
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

            dealer.tell(WANNA_PLAY, testActor());

            expectMsg(in15Seconds, PLEASE_BET);
            lastSender().tell(new Bet(50), testActor());

            expectMsgClass(in15Seconds, CardDrawn.class);
            expectMsgClass(in5Seconds, CardDrawn.class);
            expectMsgClass(in5Seconds, CardDrawn.class);
            expectMsg(in5Seconds, YOUR_TURN);

            dealer.tell(DealerPlayerContract.DealerRequest.hit(testActor()), testActor());
            expectMsgClass(in5Seconds, CardDrawn.class);

            dealer.tell(DealerPlayerContract.DealerRequest.hit(testActor()), testActor());
            expectMsgClass(in5Seconds, CardDrawn.class);

            dealer.tell(DealerPlayerContract.DealerRequest.stand(testActor()), testActor());

            expectMsgClass(in5Seconds, CardDrawn.class);
            expectMsgClass(in5Seconds, CardDrawn.class);

            expectMsg(in5Seconds, TIED_GAME);

            expectMsg(in5Seconds, PLEASE_BET);

            dealer.tell(STOP_PLAY, testActor());

            expectNoMsg(in3Seconds);

        }};
    }
}

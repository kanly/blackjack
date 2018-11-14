package org.pmazzoncini;

import static org.pmazzoncini.Durations.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pmazzoncini.blackjack.impl.DealerPlayerContract;
import org.pmazzoncini.blackjack.impl.PlayerActor;
import org.pmazzoncini.blackjack.impl.model.Card;
import org.pmazzoncini.blackjack.impl.model.FrenchDeck;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.pmazzoncini.blackjack.impl.DealerPlayerContract.*;

public class PlayerActorTest {
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
    public void testBet() throws Exception {
        new TestKit(system) {{
            Props props = Props.create(PlayerActor.class, () -> new PlayerActor(testActor()));
            ActorRef player = system.actorOf(props, "John_I");

            Future<Object> ask = ask(player, PLEASE_BET, 2000L);
            Object result = Await.result(ask, Duration.create(2L, SECONDS));
            assertTrue(result instanceof DealerPlayerContract.Bet);
        }};


    }

    @Test
    public void testPlayBehavior() {
        new TestKit(system) {{
            Props props = Props.create(PlayerActor.class, () -> new PlayerActor(testActor()));
            ActorRef player = system.actorOf(props, "John_II");
            expectMsg(WANNA_PLAY);

            player.tell(YOUR_TURN, testActor());
            expectMsg(DealerPlayerContract.DealerRequest.hit(player));

            Card nextCard = new Card(FrenchDeck.DIAMONDS, "5");
            //send(getLastSender(),new CardDrawn(nextCard));
            lastSender().tell(new CardDrawn(nextCard), testActor());

            expectMsg(DealerPlayerContract.DealerRequest.hit(player));

            nextCard = new Card(FrenchDeck.DIAMONDS, "9");
            //send(getLastSender(),new CardDrawn(nextCard));
            lastSender().tell(new CardDrawn(nextCard), testActor());
            expectMsg(DealerPlayerContract.DealerRequest.hit(player));

            nextCard = new Card(FrenchDeck.DIAMONDS, "4");
            //send(getLastSender(),new CardDrawn(nextCard));
            lastSender().tell(new CardDrawn(nextCard), testActor());
            expectMsg(DealerPlayerContract.DealerRequest.stand(player));


        }};
    }
}

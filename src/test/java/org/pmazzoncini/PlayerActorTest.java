package org.pmazzoncini;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
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
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testBet() throws Exception {
        new JavaTestKit(system) {{
            Props props = Props.create(PlayerActor.class, () -> new PlayerActor(getTestActor()));
            ActorRef player = system.actorOf(props, "John_I");

            Future<Object> ask = ask(player, PLEASE_BET, 2000L);
            Object result = Await.result(ask, Duration.create(2L, SECONDS));
            assertTrue(result instanceof DealerPlayerContract.Bet);
        }};


    }

    @Test
    public void testPlayBehavior() {
        new JavaTestKit(system) {{
            Props props = Props.create(PlayerActor.class, () -> new PlayerActor(getTestActor()));
            ActorRef player = system.actorOf(props, "John_II");
            expectMsgEquals(WANNA_PLAY);

            player.tell(YOUR_TURN, getTestActor());
            expectMsgEquals(DealerPlayerContract.DealerRequest.hit(player));

            Card nextCard = new Card(FrenchDeck.DIAMONDS, "5");
            //send(getLastSender(),new CardDrawn(nextCard));
            reply(new CardDrawn(nextCard));
            expectMsgEquals(DealerPlayerContract.DealerRequest.hit(player));

            nextCard = new Card(FrenchDeck.DIAMONDS, "9");
            //send(getLastSender(),new CardDrawn(nextCard));
            reply(new CardDrawn(nextCard));
            expectMsgEquals(DealerPlayerContract.DealerRequest.hit(player));

            nextCard = new Card(FrenchDeck.DIAMONDS, "4");
            //send(getLastSender(),new CardDrawn(nextCard));
            reply(new CardDrawn(nextCard));
            expectMsgEquals(DealerPlayerContract.DealerRequest.stand(player));


        }};
    }
}

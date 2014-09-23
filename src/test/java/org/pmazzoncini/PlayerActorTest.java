package org.pmazzoncini;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pmazzoncini.blackjack.impl.DealerMessages;
import org.pmazzoncini.blackjack.impl.PlayerActor;
import org.pmazzoncini.blackjack.impl.model.Card;
import org.pmazzoncini.blackjack.impl.model.FrenchDeck;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.pmazzoncini.blackjack.impl.DealerMessages.*;

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

        Props props = Props.create(PlayerActor.class);
        ActorRef player = system.actorOf(props);

        Future<Object> ask = ask(player, PLEASE_BET, 2000L);
        Object result = Await.result(ask, Duration.create(2L, SECONDS));
        assertTrue(result instanceof DealerMessages.Bet);


    }

    @Test
    public void testPlayBehavior() {
        new JavaTestKit(system) {{
            Props props = Props.create(PlayerActor.class, () -> new PlayerActor() {
                @Override
                protected ActorRef dealer() {
                    return getTestActor();
                }
            });
            ActorRef player = system.actorOf(props);
            expectMsgEquals(WANNA_PLAY);

            player.tell(YOUR_TURN, getTestActor());
            expectMsgEquals(HIT);

            Card nextCard = new Card(FrenchDeck.DIAMONDS, "5");
            //send(getLastSender(),new CardDrawn(nextCard));
            reply(new CardDrawn(nextCard));
            expectMsgEquals(HIT);

            nextCard = new Card(FrenchDeck.DIAMONDS, "9");
            //send(getLastSender(),new CardDrawn(nextCard));
            reply(new CardDrawn(nextCard));
            expectMsgEquals(HIT);

            nextCard = new Card(FrenchDeck.DIAMONDS, "4");
            //send(getLastSender(),new CardDrawn(nextCard));
            reply(new CardDrawn(nextCard));
            expectMsgEquals(STAND);


        }};
    }
}

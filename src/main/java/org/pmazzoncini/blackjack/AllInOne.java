package org.pmazzoncini.blackjack;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import org.pmazzoncini.blackjack.impl.DealerManager;
import org.pmazzoncini.blackjack.impl.PlayerActor;
import scala.concurrent.Future;

public class AllInOne {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("allInOne");

        ActorRef dealerManager = system.actorOf(Props.create(DealerManager.class), "John");

        Future<Object> futureDealer = Patterns.ask(dealerManager, DealerManager.GetAvailableDealer.instance, 5000L);

        futureDealer.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object result) throws Throwable {
                if (result instanceof DealerManager.AvailableDealer) {
                    ActorRef dealer = ((DealerManager.AvailableDealer) result).dealer;

                    system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(dealer)), "Harry");
                    system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(dealer)), "William");
                }
            }
        }, system.dispatcher());
    }

}

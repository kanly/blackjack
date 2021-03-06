package org.pmazzoncini.blackjack;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import com.typesafe.config.ConfigFactory;
import org.pmazzoncini.blackjack.impl.DealerManager;
import org.pmazzoncini.blackjack.impl.PlayerActor;
import scala.concurrent.Future;

public class Client {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("aClient", ConfigFactory.load("application-client.conf"));

        ActorSelection dealerManager = system.actorSelection("akka.tcp://bjackServer@127.0.0.1:2552/user/" + Server.MANAGER_NAME);
        Future<Object> futureDealer = Patterns.ask(dealerManager, DealerManager.GetAvailableDealer.instance, 5000L);

        futureDealer.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object result) throws Throwable {
                if (result instanceof DealerManager.AvailableDealer) {
                    ActorRef dealer = ((DealerManager.AvailableDealer) result).dealer;

                    system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(dealer)), "Jane");
                    system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(dealer)), "Mary");
                    system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(dealer)), "Paul");
                    system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(dealer)), "Andrew");

                }
            }
        }, system.dispatcher());

    }
}

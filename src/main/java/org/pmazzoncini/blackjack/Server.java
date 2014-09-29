package org.pmazzoncini.blackjack;

import akka.actor.ActorSystem;
import akka.actor.Props;
import org.pmazzoncini.blackjack.impl.DealerActor;
import org.pmazzoncini.blackjack.impl.DealerManager;

public class Server {

    public static final String MANAGER_NAME = "Mikey";

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("bjackServer");

        system.actorOf(Props.create(DealerManager.class), MANAGER_NAME);
    }

}

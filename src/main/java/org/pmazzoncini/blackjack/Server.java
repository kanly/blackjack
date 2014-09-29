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

//        ActorRef jule = Actors.system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(Actors.getDealer())), "Jule");
//        ActorRef harry = Actors.system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(Actors.getDealer())), "Harry");
//        ActorRef mark = Actors.system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(Actors.getDealer())), "Mark");
//        ActorRef jay = Actors.system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(Actors.getDealer())), "Jay");
//        ActorRef jane = Actors.system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(Actors.getDealer())), "Jane");
//        ActorRef mary = Actors.system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(Actors.getDealer())), "Mary");
//        ActorRef paul = Actors.system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(Actors.getDealer())), "Paul");
//        ActorRef andrew = Actors.system.actorOf(Props.create(PlayerActor.class, () -> new PlayerActor(Actors.getDealer())), "Andrew");

    }

}

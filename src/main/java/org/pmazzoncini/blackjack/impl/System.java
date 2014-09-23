package org.pmazzoncini.blackjack.impl;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class System {
    public static final ActorSystem system = ActorSystem.create("Blackjack");
    public static final ActorRef dealer = system.actorOf(Props.create(DealerActor.class));
}

package org.pmazzoncini.blackjack.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import java.io.Serializable;

/**
 * DealerManager should return a ref to an available dealer to requester. At the moment it returns always the same dealer
 */
public class DealerManager extends AbstractActor {

    final ActorRef aDealer;

    public DealerManager() {
        aDealer = context().system().actorOf(Props.create(DealerActor.class), "Martin");
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
            .match(GetAvailableDealer.class, m -> sender().tell(new AvailableDealer(aDealer), self()))
            .build();
    }

    public static class GetAvailableDealer implements Serializable {

        public static GetAvailableDealer instance = new GetAvailableDealer();

        private GetAvailableDealer() {
        }
    }

    public static class AvailableDealer implements Serializable {

        public final ActorRef dealer;

        private AvailableDealer(ActorRef dealer) {
            this.dealer = dealer;
        }
    }
}

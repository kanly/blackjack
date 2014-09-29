package org.pmazzoncini.blackjack.impl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import java.io.Serializable;

public class DealerManager extends AbstractActor {


    public DealerManager() {
        final ActorRef aDealer = context().system().actorOf(Props.create(DealerActor.class), "Martin");

        receive(ReceiveBuilder
                .match(GetAvailableDealer.class, m -> sender().tell(new AvailableDealer(aDealer), self()))
                .build()
        );
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

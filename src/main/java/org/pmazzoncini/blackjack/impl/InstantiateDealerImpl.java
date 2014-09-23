package org.pmazzoncini.blackjack.impl;

import akka.actor.ActorSystem;
import akka.actor.Props;
import org.pmazzoncini.blackjack.api.InstantiateDealer;


public class InstantiateDealerImpl implements InstantiateDealer {

    @Override
    public boolean instantiate(String actorSytemName, String dealerID) {

        try {
            ActorSystem system = ActorSystem.create(actorSytemName);
            system.actorOf(Props.create(DealerActor.class), dealerID);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

package org.pmazzoncini.blackjack.osgi;

import akka.actor.ActorSystem;
import akka.actor.Props;
import org.pmazzoncini.blackjack.osgi.api.InstantiateDealer;
import org.pmazzoncini.blackjack.impl.DealerActor;


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

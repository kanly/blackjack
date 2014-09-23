package org.pmazzoncini.blackjack.impl;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;


public class PlayerActor extends AbstractActor {

    @Override
    public void preStart() throws Exception {
        super.preStart();

        System.dealer.tell(DealerMessages.WANNA_PLAY,self());
    }

    public PlayerActor() {
        receive(ReceiveBuilder
                .match(String.class, msg -> {
                    switch(msg) {

                    }
                        }
                ).build()
            );
    }
}

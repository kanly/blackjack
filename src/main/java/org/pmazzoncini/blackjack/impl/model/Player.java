package org.pmazzoncini.blackjack.impl.model;


import akka.actor.ActorRef;
import java.io.Serializable;

/**
 * A model representing a player
 */
public class Player implements Serializable {

    private final ActorRef ref;
    private long pot;

    public Player(ActorRef ref) {
        this.ref = ref;
    }

    public ActorRef getRef() {
        return ref;
    }

    public long getPot() {
        return pot;
    }

    public void setPot(long pot) {
        this.pot = pot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Player player = (Player) o;

        if (pot != player.pot) {
            return false;
        }
        if (ref != null ? !ref.equals(player.ref) : player.ref != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = ref != null ? ref.hashCode() : 0;
        result = 31 * result + (int) (pot ^ (pot >>> 32));
        return result;
    }
}

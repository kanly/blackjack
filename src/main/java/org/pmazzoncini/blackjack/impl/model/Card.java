package org.pmazzoncini.blackjack.impl.model;

import java.io.Serializable;
import java.text.MessageFormat;

import static org.pmazzoncini.blackjack.impl.model.FrenchDeck.*;

public class Card  implements Serializable  {
    private final String seed;
    private final String rank;
    private final int value;

    public Card(String seed, String rank) {
        this.seed = seed;
        this.rank = rank;

        switch (rank) {
            case ACE:
                value = 1;
                break;
            case JACK:
            case QUEEN:
            case KING:
                value = 10;
                break;
            default:
                value = Integer.parseInt(rank);
        }
    }

    public String getSeed() {
        return seed;
    }

    public String getRank() {
        return rank;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} {1}", rank, seed);
    }
}

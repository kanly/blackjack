package org.pmazzoncini.blackjack.impl.model;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of constants representing card values and seeds in a french deck <br> Is also included a method that create a new deck in the form of
 * a List of cards
 */
public interface FrenchDeck {

    String ACE = "A";
    String JACK = "J";
    String QUEEN = "Q";
    String KING = "K";

    String HEARTS = "Hearts";
    String CLUBS = "Clubs";
    String DIAMONDS = "Diamonds";
    String SPADES = "Spades";
    List<String> SEEDS = new ArrayList<String>(4) {{
        add(HEARTS);
        add(CLUBS);
        add(DIAMONDS);
        add(SPADES);
    }};


    List<String> RANKS = new ArrayList<String>(13) {{
        add(ACE);
        add("2");
        add("3");
        add("4");
        add("5");
        add("6");
        add("7");
        add("8");
        add("9");
        add("10");
        add(JACK);
        add(QUEEN);
        add(KING);
    }};

    static List<Card> newDeck() {
        return SEEDS.stream()
            .flatMap(seed -> RANKS.stream().map(rank -> new Card(seed, rank)))
            .collect(toList());
    }
}

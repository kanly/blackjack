package org.pmazzoncini.blackjack.impl.model;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public interface FrenchDeck {
    public static final String ACE = "A";
    public static final String JACK = "J";
    public static final String QUEEN = "Q";
    public static final String KING = "K";

    public static final String HEARTS = "Hearts";
    public static final String CLUBS = "Clubs";
    public static final String DIAMONDS = "Diamonds";
    public static final String SPADES = "Spades";
    public static final List<String> SEEDS = new ArrayList<String>(4) {{
        add(HEARTS);
        add(CLUBS);
        add(DIAMONDS);
        add(SPADES);
    }};


    public static final List<String> RANKS = new ArrayList<String>(13) {{
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

    public static List<Card> newDeck() {
        return SEEDS.stream()
                    .flatMap(seed -> RANKS.stream().map(rank -> new Card(seed, rank)))
                    .collect(toList());
    }
}

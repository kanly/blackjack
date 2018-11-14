package org.pmazzoncini.blackjack.impl.model;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
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
    List<String> SEEDS = ImmutableList.of(
        HEARTS,
        CLUBS,
        DIAMONDS,
        SPADES
    );


    List<String> RANKS = ImmutableList.of(
        ACE,
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
        JACK,
        QUEEN,
        KING
    );

    static List<Card> newDeck() {
        return SEEDS.stream()
            .flatMap(seed -> RANKS.stream().map(rank -> new Card(seed, rank)))
            .collect(toList());
    }
}

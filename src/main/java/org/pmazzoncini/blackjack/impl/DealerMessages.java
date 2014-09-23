package org.pmazzoncini.blackjack.impl;


import org.pmazzoncini.blackjack.impl.model.Card;

import java.io.Serializable;

public interface DealerMessages {
    public static final String HIT = "hit!";
    public static final String STAND = "stand!";
    public static final String MUST_STAND = "mustStand";
    public static final String YOUR_TURN = "yourTurn";
    public static final String WANNA_PLAY = "wantToPlay";
    public static final String STOP_PLAY = "stopPlaying";
    public static final String PLEASE_BET = "bet";
    public static final String YOU_LOST = "youLost";
    public static final String TIED_GAME = "tied_game";


    /**
     * This message is sent to clients that have sent an unrecognized message <br/>
     * It will contain the original message
     */
    public static class WrongMessage implements Serializable {
        private final Object message;

        public WrongMessage(Object message) {
            this.message = message;
        }

        public Object getMessage() {
            return message;
        }
    }

    /**
     * The Bet Message
     */
    public static class Bet implements Serializable {
        private final Long bet;

        public Bet(Long bet) {
            this.bet = bet;
        }

        public Long getBet() {
            return bet;
        }
    }

    /**
     * Message for drawn cards
     */
    public static class CardDrawn implements Serializable {
        private final Card card;
        private final boolean isDealer;

        public CardDrawn(Card card) {
            this(card, false);
        }

        public CardDrawn(Card card, boolean isDealer) {
            this.card = card;
            this.isDealer = isDealer;
        }

        public Card getCard() {
            return card;
        }
    }

    public static class YouWon implements Serializable {
        private final long amount;

        public YouWon(long amount) {
            this.amount = amount;
        }

        public long getAmount() {
            return amount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            YouWon youWon = (YouWon) o;

            if (amount != youWon.amount) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (int) (amount ^ (amount >>> 32));
        }
    }

}

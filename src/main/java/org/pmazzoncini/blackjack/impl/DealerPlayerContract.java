package org.pmazzoncini.blackjack.impl;


import akka.actor.ActorRef;
import java.io.Serializable;
import org.pmazzoncini.blackjack.impl.model.Card;

/**
 * interface that defines messages sent and received by {@link org.pmazzoncini.blackjack.impl.DealerActor} <br> Some are static String
 * message:
 * <ul>
 * <li><b>MUST_STAND</b>: sent by dealer to a player when the latter exceeds 20 points</li>
 * <li><b>YOUR_TURN</b>: sent by dealer to a player to indicate that it's player turn</li>
 * <li><b>WANNA_PLAY</b>: sent by a player to dealer to subscribe to dealer's table</li>
 * <li><b>STOP_PLAY</b>: sent by a player to a dealer to unsubscribe from dealer's table</li>
 * <li><b>PLEASE_BET</b>: sent by a dealer to a player to ask for bet</li>
 * <li><b>YOU_LOST</b>: sent by a dealer to a player to communicate to a player that he lost the match</li>
 * <li><b>TIED_GAME</b>: sent by a dealer to a player to communicate to a player that he tied the match</li>
 * </ul>
 *
 * Others are classes to contain other data:
 * <ul>
 * <li><b>{@link DealerPlayerContract.DealerRequest}</b>: sent by player to dealer it has two factory methods for the two
 * possible values (hit, stand) this factory methods will also need the ref of the sending player</li>
 * <li><b>{@link DealerPlayerContract.WrongMessage}</b>: sent by dealer to a requester that sent an unexpected message</li>
 * <li><b>{@link DealerPlayerContract.Bet}</b>: sent by player to dealer to bet an amount</li>
 * <li><b>{@link DealerPlayerContract.CardDrawn}</b>: sent by dealer to a player when a card is drawn for the player or to every
 * player when a card is drawn for the dealer </li>
 * <li><b>{@link DealerPlayerContract.YouWon}</b>: sent by dealer to a player to communicate to a player that he tied the match
 * and how much he won
 * </li>
 * </ul>
 */
public interface DealerPlayerContract {

    String MUST_STAND = "mustStand";
    String YOUR_TURN = "yourTurn";
    String WANNA_PLAY = "wantToPlay";
    String STOP_PLAY = "stopPlaying";
    String PLEASE_BET = "bet";
    String YOU_LOST = "youLost";
    String TIED_GAME = "tied_game";


    class DealerRequest implements Serializable {

        static final String HIT = "hit!";
        static final String STAND = "stand!";

        private final String request;
        private final ActorRef player;

        private DealerRequest(String request, ActorRef player) {
            this.request = request;
            this.player = player;
        }

        public static DealerRequest hit(ActorRef player) {
            return new DealerRequest(HIT, player);
        }

        public static DealerRequest stand(ActorRef player) {
            return new DealerRequest(STAND, player);
        }

        String request() {
            return request;
        }

        ActorRef player() {
            return player;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DealerRequest that = (DealerRequest) o;

            if (!player.equals(that.player)) {
                return false;
            }
            if (!request.equals(that.request)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = request.hashCode();
            result = 31 * result + player.hashCode();
            return result;
        }
    }

    /**
     * This message is sent to clients that have sent an unrecognized message <br> It will contain the original message
     */
    class WrongMessage implements Serializable {

        private final Object message;

        WrongMessage(Object message) {
            this.message = message;
        }

        public Object getMessage() {
            return message;
        }
    }

    /**
     * The Bet Message
     */
    class Bet implements Serializable {

        private final int bet;

        public Bet(int bet) {
            this.bet = bet;
        }

        int getBet() {
            return bet;
        }
    }

    /**
     * Message for drawn cards
     */
    class CardDrawn implements Serializable {

        private final Card card;
        private final boolean isDealer;

        public CardDrawn(Card card) {
            this(card, false);
        }

        CardDrawn(Card card, boolean isDealer) {
            this.card = card;
            this.isDealer = isDealer;
        }

        Card getCard() {
            return card;
        }

        public boolean isDealer() {
            return isDealer;
        }
    }

    /**
     * The you won message
     */
    class YouWon implements Serializable {

        private final long amount;

        public YouWon(long amount) {
            this.amount = amount;
        }

        long getAmount() {
            return amount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            YouWon youWon = (YouWon) o;

            return amount == youWon.amount;
        }

        @Override
        public int hashCode() {
            return (int) (amount ^ (amount >>> 32));
        }
    }

}

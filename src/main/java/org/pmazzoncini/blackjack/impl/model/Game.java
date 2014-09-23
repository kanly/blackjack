package org.pmazzoncini.blackjack.impl.model;

import java.util.UUID;

public class Game {
    private final Player player;
    private final long bet;
    private int score = 0;
    private final UUID gameId = UUID.randomUUID();

    public Game(Player player, long bet) {
        this.player = player;
        this.bet = bet;
    }

    public Player getPlayer() {
        return player;
    }

    public long getBet() {
        return bet;
    }

    public int getScore() {
        return score;
    }

    public void cardDrawn(Card drawnCard) {
        score += drawnCard.getValue();
    }

    public static Game dealer() {
        return new Game(null, 0L);
    }

    public UUID getGameId() {
        return gameId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Game game = (Game) o;

        if (bet != game.bet) return false;
        if (score != game.score) return false;
        if (!gameId.equals(game.gameId)) return false;
        if (!player.equals(game.player)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = player.hashCode();
        result = 31 * result + (int) (bet ^ (bet >>> 32));
        result = 31 * result + gameId.hashCode();
        return result;
    }
}

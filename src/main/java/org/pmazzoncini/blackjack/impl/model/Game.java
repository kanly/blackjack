package org.pmazzoncini.blackjack.impl.model;

import java.io.Serializable;
import java.util.UUID;

public class Game implements Serializable {

    private final UUID gameId = UUID.randomUUID();
    private transient Player player;
    private String playerName;
    private long bet;
    private int score = 0;
    private GameResult gameResult;

    public Game() {
    }

    public Game(Player player, long bet) {
        this.player = player;
        this.playerName = player.getRef().path().name();
        this.bet = bet;
    }

    public static Game dealer() {
        return new Game();
    }

    public static int scoreComparator(Game aGame, Game otherGame) {
        int compare = aGame.getScore() - otherGame.getScore();
        if (compare == 0) {
            int idCompare = aGame.getGameId().compareTo(otherGame.getGameId());
            if (idCompare > 0) {
                compare += 1;
            }
            if (idCompare < 0) {
                compare -= 1;
            }
        }
        return compare;
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

    public UUID getGameId() {
        return gameId;
    }

    public GameResult getGameResult() {
        return gameResult;
    }

    public void setGameResult(GameResult gameResult) {
        this.gameResult = gameResult;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String name) {
        playerName = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Game game = (Game) o;

        if (bet != game.bet) {
            return false;
        }
        if (score != game.score) {
            return false;
        }
        if (gameId != null ? !gameId.equals(game.gameId) : game.gameId != null) {
            return false;
        }
        if (gameResult != game.gameResult) {
            return false;
        }
        if (player != null ? !player.equals(game.player) : game.player != null) {
            return false;
        }
        if (playerName != null ? !playerName.equals(game.playerName) : game.playerName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = player != null ? player.hashCode() : 0;
        result = 31 * result + (playerName != null ? playerName.hashCode() : 0);
        result = 31 * result + (int) (bet ^ (bet >>> 32));
        result = 31 * result + score;
        result = 31 * result + (gameId != null ? gameId.hashCode() : 0);
        result = 31 * result + (gameResult != null ? gameResult.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Game{" +
            "player=" + playerName +
            ", bet=" + bet +
            ", score=" + score +
            ", gameId=" + gameId +
            ", gameResult=" + gameResult +
            '}';
    }

    public enum GameResult implements Serializable {
        WON, TIE, LOST, RETIRED
    }
}

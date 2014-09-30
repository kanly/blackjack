package org.pmazzoncini.blackjack.impl.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A model representing a game round.
 */
public class Round implements Serializable {
    private final UUID roundId = UUID.randomUUID();
    private final List<Game> games = new ArrayList<>();
    private final Date roundStartDate = new Date();
    private final String dealer;
    private Date endDate;
    private int dealerScore;

    public Round(String dealer) {
        this.dealer = dealer;
    }

    public UUID getRoundId() {
        return roundId;
    }

    public void addGame(Game game) {
        games.add(game);
    }

    public List<Game> getGames() {
        return games;
    }

    public Date getRoundStartDate() {
        return roundStartDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<Player> players() {
        return games.stream().map(Game::getPlayer).collect(Collectors.toList());
    }

    public int getDealerScore() {
        return dealerScore;
    }

    public void setDealerScore(int dealerScore) {
        this.dealerScore = dealerScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Round round = (Round) o;

        if (!roundId.equals(round.roundId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return roundId.hashCode();
    }

    @Override
    public String toString() {

        String gamesString = games.parallelStream().map(Game::toString).collect(Collectors.joining("; "));

        return "Round{" +
                "roundId=" + roundId +
                ", games=" + gamesString +
                ", roundStartDate=" + roundStartDate +
                ", dealer='" + dealer + '\'' +
                ", endDate=" + endDate + '\'' +
                ", dealerScore=" + dealerScore  +
                '}';
    }
}

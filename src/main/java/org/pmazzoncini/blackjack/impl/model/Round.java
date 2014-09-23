package org.pmazzoncini.blackjack.impl.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Round implements Serializable {
    private final UUID roundId = UUID.randomUUID();
    private final List<Game> games = new ArrayList<>();
    private final Date roundStartDate = new Date();
    private Date endDate;

    public UUID getRoundId() {
        return roundId;
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
}

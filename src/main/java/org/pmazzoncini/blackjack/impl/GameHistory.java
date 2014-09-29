package org.pmazzoncini.blackjack.impl;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.pmazzoncini.blackjack.impl.model.Round;

/**
 * this class wraps an in memory hazelcast instance that will be used to save Game History data in the form of
 * {@link org.pmazzoncini.blackjack.impl.model.Round} objects
 */
public class GameHistory {
    private final HazelcastInstance hazelcast;

    private GameHistory() {
        Config cfg = new Config();
        this.hazelcast = Hazelcast.newHazelcastInstance(cfg);
    }

    private static class Holder {
        private static final GameHistory INSTANCE = new GameHistory();
    }

    public static GameHistory instance() {
        return Holder.INSTANCE;
    }

    public void saveCompletedRound(Round round) {
        hazelcast.getList("rounds").add(round);
    }

}

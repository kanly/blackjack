package org.pmazzoncini.blackjack.impl;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class GameData {
    private final HazelcastInstance hazelcast;


    private GameData() {
        Config cfg = new Config();
        this.hazelcast = Hazelcast.newHazelcastInstance(cfg);


    }

    private static class Holder {
        private static final GameData INSTANCE = new GameData();
    }

    public static GameData instance() {
        return Holder.INSTANCE;
    }


}

package org.pmazzoncini.blackjack;

import com.hazelcast.config.Config;
import com.hazelcast.core.*;
import org.pmazzoncini.blackjack.impl.GameHistory;
import org.pmazzoncini.blackjack.impl.model.Round;

public class HistoryReader {

    public static void main(String[] args) {
        Config cfg = new Config();
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(cfg);

        IList<Object> list = hazelcast.getList(GameHistory.ROUNDS_LIST_KEY);

        list.stream().map(obj -> (Round) obj).forEach(round -> System.out.println("Existing: " + round));

        list.addItemListener(new ItemListener<Object>() {
            @Override
            public void itemAdded(ItemEvent<Object> objectItemEvent) {
                System.out.println("Added: " + objectItemEvent.getItem());
            }

            @Override
            public void itemRemoved(ItemEvent<Object> objectItemEvent) {
                System.out.println("Removed: " + objectItemEvent.getItem());
            }
        }, true);

    }
}

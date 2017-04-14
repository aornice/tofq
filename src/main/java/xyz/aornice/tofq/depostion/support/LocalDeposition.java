package xyz.aornice.tofq.depostion.support;

import xyz.aornice.tofq.depostion.DepositionListener;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.util.SortedQueue;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by drfish on 09/04/2017.
 */
public class LocalDeposition implements CargoDeposition {

    private final ConcurrentMap<Topic, SortedQueue<Cargo>> topicMap = new ConcurrentHashMap<>();
    private final BlockingQueue<Topic> batchedTopics = new LinkedBlockingQueue<>();

    private static final int BATCH_DEPOSITION_SIZE = 300;
    private static final long DEPOSITION_INTERVAL_NANO = 100000;


    private LocalDeposition() {
        new Thread(new DepositionTask()).start();
    }

    public static CargoDeposition getInstance() {
        return Singleton.INSTANCE;
    }

    @Override
    public void write(Cargo cargo) {
        topicMap.get(cargo.getTopic()).put(cargo);
        if (topicMap.size() == BATCH_DEPOSITION_SIZE) notifyDeposition(cargo.getTopic());
    }

    @Override
    public void addDepositionListener(DepositionListener listener) {
    }

    private void notifyDeposition(Topic topic) {
        try {
            batchedTopics.put(topic);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void deposite(Topic topic) {
    }

    private class DepositionTask implements Runnable{

        private long timestamp;
        private final Map<Topic, Boolean> cleaned = new HashMap<>();

        @Override
        public void run() {
            timestamp = System.nanoTime();
            for (;;) {
                if (System.nanoTime() - timestamp > DEPOSITION_INTERVAL_NANO) {
                    for (Topic topic: topicMap.keySet()) {
                        if (cleaned.get(topic)) continue;
                        deposite(topic);
                    }
                    for (Map.Entry<Topic, Boolean> e: cleaned.entrySet()) e.setValue(false);
                    batchedTopics.clear();
                    timestamp = System.nanoTime();

                } else {
                    try {
                        Topic topic = batchedTopics.poll(System.nanoTime() - timestamp, TimeUnit.NANOSECONDS);
                        if (topic == null) break;
                        deposite(topic);
                        cleaned.put(topic, true);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class Singleton {
        static LocalDeposition INSTANCE = new LocalDeposition();
    }
}

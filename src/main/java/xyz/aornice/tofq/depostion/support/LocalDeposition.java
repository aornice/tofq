package xyz.aornice.tofq.depostion.support;

import xyz.aornice.tofq.depostion.DepositionListener;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.depostion.ICargo;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.util.SortedQueue;
import xyz.aornice.tofq.Topic;

import java.util.*;
import java.util.concurrent.*;


public class LocalDeposition implements CargoDeposition {

    private static final int BATCH_DEPOSITION_SIZE = 300;
    private static final long DEPOSITION_INTERVAL_NANO = 100000;
    private static final long CARGO_OFFSET_LEN_BYTES = 8;

    private final ConcurrentMap<Topic, SortedQueue<ICargo>> topicMap = new ConcurrentHashMap<>();
    private final BlockingQueue<Topic> batchedTopics = new LinkedBlockingQueue<>();
    private final List<ICargo> cargoCache = new ArrayList<>(BATCH_DEPOSITION_SIZE * 3 / 2);
    private final Harbour harbour = null;



    private LocalDeposition() {
        new Thread(new DepositionTask()).start();
    }

    public static CargoDeposition getInstance() {
        return Singleton.INSTANCE;
    }

    @Override
    public void write(ICargo cargo) {
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
        Iterator<ICargo> cargoIt = topicMap.get(topic).takeAll();
        cargoIt.forEachRemaining(cargoCache::add);

        String topicFile = topic.getNewestFile();
        final int size = cargoCache.size();
        int start = 0, dataOffset = 0;
        do {
            int fileRemains;
            while ((fileRemains = topic.CARGO_MAX_NUM - topic.getOffset()) == 0)
                topicFile = topic.newTopicFile();

            int end = fileRemains > (size - start) ? size: start + fileRemains;
            for (int i = start; i < end; i++) ;

            start = end;
        } while (start != size);
    }

    private class DepositionTask implements Runnable {

        private final Map<Topic, Boolean> cleaned = new HashMap<>();

        private long timestamp;

        @Override
        public void run() {
            timestamp = System.nanoTime();
            for (; ; ) {
                if (System.nanoTime() - timestamp > DEPOSITION_INTERVAL_NANO) {
                    for (Topic topic : topicMap.keySet()) {
                        if (cleaned.get(topic)) continue;
                        deposite(topic);
                    }
                    for (Map.Entry<Topic, Boolean> e : cleaned.entrySet()) e.setValue(false);
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

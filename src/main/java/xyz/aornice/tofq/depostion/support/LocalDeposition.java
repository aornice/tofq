package xyz.aornice.tofq.depostion.support;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.depostion.DepositionListener;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.util.SuccessiveList;
import xyz.aornice.tofq.Topic;
import static xyz.aornice.tofq.TopicFileFormat.Header;
import static xyz.aornice.tofq.TopicFileFormat.Offset;
import static xyz.aornice.tofq.TopicFileFormat.Data;

import java.util.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class LocalDeposition implements CargoDeposition {

    private static final Logger logger = LogManager.getLogger(LocalDeposition.class);
    private static final int BATCH_DEPOSITION_SIZE = 300;
    private static final long DEPOSITION_INTERVAL_NANO = 100000;

    private final ConcurrentMap<Topic, SuccessiveList<Cargo>> topicMap = new ConcurrentHashMap<>();
    private final BlockingQueue<Topic> batchedTopics = new LinkedBlockingQueue<>();
    private final List<Cargo> cargoCache = new ArrayList<>(BATCH_DEPOSITION_SIZE * 3 / 2);
    private final List<DepositionListener> listeners = new CopyOnWriteArrayList<>();
    private final Harbour harbour = null;


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
        listeners.add(listener);
    }

    private void notifyDeposition(Topic topic) {
        try {
            batchedTopics.put(topic);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void deposit(Topic topic) {
        topicMap.get(topic).takeAllSuccessive(topic.getMaxStoredId() + 1, cargoCache);

        String topicFile = topic.getNewestFile();
        final int size = cargoCache.size();

        if (size == 0) return;
        if (cargoCache.get(0).getId() == topic.getStartId() + 1) {
            logger.debug("Cargoes is not successive to lasted deposited cargo");
            return;
        }

        int start = 0, maxStoredId = 0;
        do {
            int fileRemains;
            while ((fileRemains = topic.CARGO_MAX_NUM - topic.getCount()) == 0)
                topicFile = topic.newTopicFile();
            int end = fileRemains > (size - start) ? size : start + fileRemains;

            {
                long putOffset = Offset.OFFSET_BYTE + topic.getCount() * Offset.OFFSET_SIZE_BYTE;
                long dataOffset = topic.getCount() == 0 ? Data.OFFSET_BYTE :
                        harbour.getLong(topicFile, putOffset - Offset.OFFSET_SIZE_BYTE);
                for (int i = start; i < end; i++, putOffset += Offset.OFFSET_SIZE_BYTE)
                    harbour.put(topicFile, dataOffset += cargoCache.get(i).size(), putOffset);
            }

            {
                long putOffset = topic.getCount() == 0 ? Data.OFFSET_BYTE :
                        harbour.getLong(topicFile, Offset.OFFSET_BYTE + (topic.getCount() - 1) * Offset.OFFSET_SIZE_BYTE);
                for (int i = start; i < end; i++, putOffset += cargoCache.get(i).size())
                    harbour.put(topicFile, cargoCache.get(i).getData(), putOffset);
            }
            harbour.put(topicFile, topic.getCount() + start - end, Header.COUNT_OFFSET_BYTE);
            topic.incrementCount(start - end);
            start = end;
            harbour.flush(topicFile);
        } while (start != size);
        for (DepositionListener l : listeners) l.notifyDeposition(topic, maxStoredId);
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
                        deposit(topic);
                    }
                    for (Map.Entry<Topic, Boolean> e : cleaned.entrySet()) e.setValue(false);
                    batchedTopics.clear();
                    timestamp = System.nanoTime();

                } else {
                    try {
                        Topic topic = batchedTopics.poll(System.nanoTime() - timestamp, TimeUnit.NANOSECONDS);
                        if (topic == null) break;
                        deposit(topic);
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

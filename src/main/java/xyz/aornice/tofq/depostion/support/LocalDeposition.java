package xyz.aornice.tofq.depostion.support;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.depostion.DepositionListener;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.depostion.util.ConcurrentSuccessiveList;
import xyz.aornice.tofq.depostion.util.SuccessiveList;
import xyz.aornice.tofq.Topic;
import static xyz.aornice.tofq.TopicFileFormat.Header;
import static xyz.aornice.tofq.TopicFileFormat.Offset;
import static xyz.aornice.tofq.TopicFileFormat.Data;

import java.util.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.TopicUpdateListener;


public class LocalDeposition implements CargoDeposition, TopicUpdateListener {

    private static final Logger logger = LogManager.getLogger(LocalDeposition.class);
    private static final int BATCH_DEPOSITION_SIZE = 2;
    private static final long DEPOSITION_INTERVAL_NANO = 5_000_000;

    private final ConcurrentMap<Topic, SuccessiveList<Cargo>> topicMap;
    private final BlockingQueue<Topic> batchedTopics;
    private final List<Cargo> cargoCache;
    private final List<DepositionListener> listeners;
    private final Thread thread;

    private Harbour harbour = null;
    private TopicCenter topicCenter = null;

    {
        topicMap = new ConcurrentHashMap<>();
        batchedTopics = new LinkedBlockingQueue<>();
        cargoCache = new ArrayList<>(BATCH_DEPOSITION_SIZE * 3 / 2);
        listeners = new CopyOnWriteArrayList<>();
    }

    private LocalDeposition() {
        thread = new Thread(new DepositionTask());
        thread.start();
    }

    public static CargoDeposition getInstance() {
        return Singleton.INSTANCE;
    }

    @Override
    public void write(Cargo cargo) {
        topicMap.get(cargo.getTopic()).put(cargo);
        if (topicMap.get(cargo.getTopic()).successiveSize() >= BATCH_DEPOSITION_SIZE) notifyDeposition(cargo.getTopic());
    }

    @Override
    public void addDepositionListener(DepositionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void topicUpdated(Topic newTopic) {
        topicMap.put(newTopic, new ConcurrentSuccessiveList<>(BATCH_DEPOSITION_SIZE * 3 / 2, newTopic.getMaxStoredId() + 1));
    }

    public void setHarbour(Harbour harbour) {
        this.harbour = harbour;
    }

    public void setTopicCenter(TopicCenter topicCenter) {
        this.topicCenter = topicCenter;
        for (Topic topic: topicCenter.getTopics())
            topicMap.put(topic, new ConcurrentSuccessiveList<>(BATCH_DEPOSITION_SIZE * 3 / 2, topic.getMaxStoredId() + 1));
    }

    public void close() {
        thread.interrupt();
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

        logger.debug("MaxStoredId {} ; size {}", topic.getMaxStoredId(), cargoCache.size());

        if (size == 0) return;
        if (cargoCache.get(0).getId() == topic.getStartId() + 1) {
            logger.debug("Cargoes is not successive to lasted deposited cargo");
            return;
        }

        logger.debug("Deposit topic {} start", topic.getName());
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
                for (int i = start; i < end; i++) {
                    harbour.put(topicFile, dataOffset += cargoCache.get(i).size(), putOffset);
                    putOffset += Offset.OFFSET_SIZE_BYTE;
                }

            }

            {
                long putOffset = topic.getCount() == 0 ? Data.OFFSET_BYTE :
                        harbour.getLong(topicFile, Offset.OFFSET_BYTE + (topic.getCount() - 1) * Offset.OFFSET_SIZE_BYTE);
                for (int i = start; i < end; i++) {
                    harbour.put(topicFile, cargoCache.get(i).getData(), putOffset);
                    putOffset += cargoCache.get(i).size();
                }
            }
            topic.incrementCount(end - start);
            harbour.put(topicFile, topic.getCount(), Header.COUNT_OFFSET_BYTE);
            start = end;
            harbour.flush(topicFile);
            topic.setMaxStoredId(cargoCache.get(end - 1).getId());
            logger.debug("max id {}", topic.getMaxStoredId());
        } while (start != size);
        for (DepositionListener l : listeners) l.notifyDeposition(topic, maxStoredId);
        cargoCache.clear();
        logger.debug("Deposit topic {} end", topic.getName());
    }


    private class DepositionTask implements Runnable {

        private final Map<Topic, Boolean> cleaned = new HashMap<>();

        private long timestamp;

        @Override
        public void run() {
            timestamp = System.nanoTime();
            for (; ; ) {
                if (System.nanoTime() - timestamp > DEPOSITION_INTERVAL_NANO) {
                    logger.debug("Time interval deposition start");
                    batchedTopics.clear();
                    for (Topic topic : topicMap.keySet()) {
                        if (cleaned.get(topic) != null && cleaned.get(topic)) continue;
                        deposit(topic);
                    }
                    for (Map.Entry<Topic, Boolean> e : cleaned.entrySet()) e.setValue(false);
                    timestamp = System.nanoTime();
                    logger.debug("Time interval deposition end");
                } else {
                    try {
                        Topic topic = batchedTopics.poll(DEPOSITION_INTERVAL_NANO - (System.nanoTime() - timestamp), TimeUnit.NANOSECONDS);
                        if (topic == null) continue;
                        logger.debug("Batch deposition start");
                        deposit(topic);
                        logger.debug("Batch deposition end");
                        cleaned.put(topic, true);
                    } catch (InterruptedException e) {
                        logger.debug("Detect interrupt, close the deposition");
                        return;
                    }
                }
                if (Thread.interrupted()) {
                    logger.debug("Detect interrupt, close the deposition");
                    return;
                }
            }
        }
    }

    private static class Singleton {
        static LocalDeposition INSTANCE = new LocalDeposition();
    }
}

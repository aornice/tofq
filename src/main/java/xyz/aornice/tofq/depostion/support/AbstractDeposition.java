package xyz.aornice.tofq.depostion.support;

import java.util.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Setting;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.depostion.DepositionListener;
import xyz.aornice.tofq.depostion.util.ConcurrentSuccessiveList;
import xyz.aornice.tofq.depostion.util.SuccessiveList;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.util.Memory;
import xyz.aornice.tofq.util.UnsafeMemory;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.TopicChangeListener;

import static xyz.aornice.tofq.TopicFileFormat.*;


public abstract class AbstractDeposition implements CargoDeposition, TopicChangeListener {

    private static final Logger logger = LogManager.getLogger(AbstractDeposition.class);

    private static final Memory unsafe = UnsafeMemory.INSTANCE;

    /**
     * status value to indicate deposition is ready to start
     */
    private static final int READY = 0;
    /**
     * status value to indicate deposition is running
     */
    private static final int RUNNING = 1;
    /**
     * status value to indicate deposition is shutting, currently dealing with
     * properly closing work
     */
    private static final int SHUTTING = 2;
    /**
     * status value to indicate deposition has been shutdown completely
     */
    private static final int SHUTDOWN = 3;

    /**
     * The offset of status
     */
    private static final long statusOffset;

    /**
     * The map between topic and corresponding cargoes waiting to be deposited
     */
    private final ConcurrentMap<Topic, SuccessiveList<Cargo>> topicMap;

    /**
     * The topics waiting for batched deposition or depositing.
     * Use to preventing repeat add topic to
     * {@link AbstractDeposition#processingTopics}
     */
    private final Set<Topic> processingTopics;

    /**
     * The topics waiting for batched deposition
     */
    private final BlockingQueue<Topic> batchedTopics;

    /**
     * The listeners when topic is deposited need to be notified
     */
    private final Set<DepositionListener> listeners;

    /**
     * The thread used to run Deposition task
     */
    private Thread thread;

    private volatile int status = READY;

    /**
     * The harbour which the data write to and get from
     */
    private Harbour harbour;


    static {
        try {
            statusOffset = unsafe.getFieldOffset(AbstractDeposition.class.getDeclaredField("status"));
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    {
        topicMap = new ConcurrentHashMap<>();
        processingTopics = new CopyOnWriteArraySet<>();
        batchedTopics = new LinkedBlockingQueue<>();
        listeners = new CopyOnWriteArraySet<>();
    }

    @Override
    public void write(Cargo cargo) {
        if (status != RUNNING) throw new RejectException();
        topicMap.get(cargo.getTopic()).put(cargo);
        notifyDeposition(cargo.getTopic());
    }

    @Override
    public void addDepositionListener(DepositionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void start() {
        if (status > READY)
            throw new IllegalDepositionStateException("The deposition has already been start or closed");
        if (unsafe.compareAndSwapInt(this, statusOffset, READY, RUNNING)) {
            thread = new Thread(new DepositionTask(), "DepositionTask");
            thread.start();
        }
    }

    @Override
    public void shutdown() {
        if (status != RUNNING)
            throw new IllegalDepositionStateException("The deposition isn't running");
        if (unsafe.compareAndSwapInt(this, statusOffset, RUNNING, SHUTTING))
            thread.interrupt();
    }

    @Override
    public void shutdownGracefully() {
        if (status != RUNNING)
            throw new IllegalDepositionStateException("The deposition isn't running");
        unsafe.compareAndSwapInt(this, statusOffset, RUNNING, SHUTTING);
    }

    @Override
    public void topicAdded(Topic newTopic) {
        addTopicHelper(newTopic);
    }

    @Override
    public void topicDeleted(Topic topic) {
        topicMap.remove(topic);
    }

    /**
     * Set the specific harbour
     *
     * @param harbour - the harbour
     */
    protected void setHarbour(Harbour harbour) {
        this.harbour = harbour;
    }

    /**
     * Set the specific topic center and init the
     * {@link AbstractDeposition#topicMap} based on the topics in topic center
     *
     * @param topicCenter - the topic center
     */
    protected void setTopicCenter(TopicCenter topicCenter) {
        topicMap.clear();
        topicCenter.addListener(this);
        for (Topic topic : topicCenter.getTopics()) addTopicHelper(topic);
    }

    /**
     * Add topic to {@link AbstractDeposition#topicMap}. Initial the
     * {@link ConcurrentSuccessiveList} size to 3 / 2 of
     * {@link Setting#BATCH_DEPOSITION_SIZE}
     *
     * @param topic - the topic to be added
     */
    final private void addTopicHelper(Topic topic) {
        topicMap.put(topic, new ConcurrentSuccessiveList<>(Setting.BATCH_DEPOSITION_SIZE * 3 / 2, topic.getMaxStoredId() + 1));
    }

    /**
     * Notify {@link DepositionTask} to deposit topic's cargoes
     * (add to {@link AbstractDeposition#batchedTopics}).
     * When the topic already in batchedTopics, do nothing.
     *
     * @param topic - the topic to be notified deposition
     */
    final private void notifyDeposition(Topic topic) {
        if (processingTopics.contains(topic) || topicMap.get(topic).successiveSize() < Setting.BATCH_DEPOSITION_SIZE)
            return;
        synchronized (processingTopics) {
            if (processingTopics.contains(topic)) return;
            processingTopics.add(topic);
        }
        try {
            batchedTopics.put(topic);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class DepositionTask implements Runnable {

        /**
         * Mark the topics have already been cleaned in current time interval
         */
        private final Map<Topic, Boolean> cleaned = new HashMap<>();

        /**
         * Cache the topic's cargoes when depositing the topic to drain all
         * successive cargoes from topic.
         */
        private final List<Cargo> cargoCache;

        /**
         * The lasted timestamp run time-interval deposition
         */
        private long timestamp;

        /**
         * Today date str represent with {@link FileName#DATE_FORMAT}
         */
        private String today = FileName.DATE_FORMAT.format(new Date());


        {
            cargoCache = new ArrayList<>(Setting.BATCH_DEPOSITION_SIZE * 3 / 2);
        }

        @Override
        public void run() {
            try {
                runHelper();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Unexpected exception: ", e);
            } finally {
                if (status != SHUTTING)
                    throw new IllegalDepositionStateException("the interrupt don't invoke by shutdown");
                status = SHUTDOWN;
            }
        }

        final private void runHelper() {
            timestamp = System.nanoTime();
            boolean detectShutting = false;
            for (; ; ) {
                today = FileName.DATE_FORMAT.format(new Date());
                if (System.nanoTime() - timestamp > Setting.DEPOSITION_INTERVAL_NANO) {
                    logger.debug("Time interval deposition start");
                    batchedTopics.clear();
                    for (Topic topic : topicMap.keySet()) {
                        if (cleaned.get(topic) != null && cleaned.get(topic))
                            continue;
                        deposit(topic);
                        processingTopics.remove(topic);
                    }
                    for (Map.Entry<Topic, Boolean> e : cleaned.entrySet())
                        e.setValue(false);
                    timestamp = System.nanoTime();
                    logger.debug("Time interval deposition end");
                } else {
                    try {
                        final long waitingTime = Setting.DEPOSITION_INTERVAL_NANO - (System.nanoTime() - timestamp);
                        Topic topic = batchedTopics.poll(waitingTime, TimeUnit.NANOSECONDS);
                        if (topic == null) continue;
                        logger.debug("Batch deposition start");
                        deposit(topic);
                        processingTopics.remove(topic);
                        logger.debug("Batch deposition end");
                        cleaned.put(topic, true);
                    } catch (InterruptedException e) {
                        logger.info("Detect interrupt, shutdown the deposition");
                        return;
                    }
                }
                if (Thread.interrupted()) {
                    logger.info("Detect interrupt, shutdown the deposition");
                    return;
                }

                if (status == SHUTTING) {
                    if (detectShutting) {
                        logger.info("Shutdown gracefully");
                        return;
                    } else {
                        detectShutting = true;
                    }
                }
            }
        }

        /**
         * Deposit topic's cargoes
         *
         * @param topic - The topic to be deposited
         */
        final private void deposit(Topic topic) {
            try {
                topicMap.get(topic).takeAllSuccessive(topic.getMaxStoredId() + 1, cargoCache);

                long oldMaxStoredId = topic.getMaxStoredId();
                long maxStoredId = depositHelper(topic, cargoCache);

                if (oldMaxStoredId == maxStoredId) return;

                for (DepositionListener l : listeners)
                    l.notifyDeposition(topic, maxStoredId);
            } finally {
                cargoCache.clear();
            }
        }

        final private long depositHelper(Topic topic, List<Cargo> cargoes) {
            final int size = cargoes.size();
            if (size == 0) return topic.getMaxStoredId();


            if (cargoes.get(0).getId() != topic.getMaxStoredId() + 1)
                throw new Error(String.format("Cargoes %s is not successive to lasted deposited cargo %s", cargoes.get(0).getId(), topic.getMaxStoredId()));

            logger.debug("Deposit topic {} [maxStoredId {}, cargoesSize {}] start", topic.getName(), topic.getMaxStoredId(), cargoes.size());

            String topicFile = topic.getNewestFile();
            if (!topicFile.startsWith(today)) topicFile = topic.newTopicFile();

            int start = 0, maxStoredId = 0, count;
            do {
                int fileRemains;
                while ((fileRemains = Offset.CAPABILITY - (count = topic.getCount())) == 0)
                    topicFile = topic.newTopicFile();
                int end = fileRemains > (size - start) ? size : start + fileRemains;

                depositDataHelper(topicFile, count, cargoes, start, end);

                topic.setMaxStoredId(cargoes.get(end - 1).getId());

                logger.debug("Max Stored Cargo id {}", topic.getMaxStoredId());

                start = end;
            } while (start != size);

            logger.debug("Deposit topic {} end", topic.getName());
            return maxStoredId;
        }

        final private void depositDataHelper(String file, int count, List<Cargo> cargoes,
                                             int start, int end) {
            /** put cargoes' offset */
            {
                long putOffset = Offset.OFFSET_BYTE + count * Offset.OFFSET_SIZE_BYTE;
                long dataOffset = count == 0 ? Data.OFFSET_BYTE :
                        harbour.getLong(file, putOffset - Offset.OFFSET_SIZE_BYTE);
                for (int i = start; i < end; i++) {
                    harbour.put(file, dataOffset += cargoes.get(i).size(), putOffset);
                    putOffset += Offset.OFFSET_SIZE_BYTE;
                }

            }

            /** put cargoes' data */
            {
                long putOffset = count == 0 ? Data.OFFSET_BYTE :
                        harbour.getLong(file, Offset.OFFSET_BYTE + (count - 1) * Offset.OFFSET_SIZE_BYTE);
                for (int i = start; i < end; i++) {
                    harbour.put(file, cargoes.get(i).getData(), putOffset);
                    putOffset += cargoes.get(i).size();
                }
            }

            /** Set count */
            harbour.put(file, count + end - start, Header.COUNT_OFFSET_BYTE);
            harbour.flush(file);
        }

    }


}

package xyz.aornice.tofq;

/**
 * Created by robin on 10/04/2017.
 */

import xyz.aornice.tofq.harbour.Harbour;

import java.util.concurrent.atomic.AtomicLong;

public class Topic {
    public static final int CARGO_MAX_NUM = TopicFileFormat.Offset.CAPABILITY;

    private static final Harbour harbour = null;

    private String name;
    private AtomicLong maxId;
    private AtomicLong maxStoredId;
    private String newestFile;
    private int count;
    private long startId;


    public Topic(String name, String newestFile) {
        this.name = name;
        this.newestFile = newestFile;
        loadInfo();
    }

    private void loadInfo() {
        startId = harbour.getLong(newestFile, TopicFileFormat.Header.ID_START_OFFSET_BYTE);
        count = harbour.getInt(newestFile, TopicFileFormat.Header.COUNT_OFFSET_BYTE);
        maxId = new AtomicLong(startId + count);
        maxStoredId = new AtomicLong(startId + count);
    }

    public String getName() {
        return name;
    }

    /**
     * thread-safe
     * @return the incremented max id of cargo
     */
    public long incrementAndGetId() {
        return maxId.incrementAndGet();
    }

    public long getMaxStoredId() {
        return maxStoredId.get();
    }

    /**
     * thread-safe
     * @param id - the max id of the cargo has been deposited
     * @return if return true, represent set success. It might be failed, cause of
     * current saved cargo max id is large than the id
     */
    public boolean setMaxStoredId(long id) {
        long origin;
        do {
            origin = this.maxStoredId.get();
            if (origin >= id) return false;
        } while (!this.maxStoredId.compareAndSet(origin, id));
        return true;
    }

    public String getNewestFile() {
        return newestFile;
    }

    public void incrementCount() {
        count++;
        if (count > CARGO_MAX_NUM) throw new RuntimeException("Offset exceed");
    }

    public void incrementCount(int val) {
        count += val;
        if (count > CARGO_MAX_NUM) throw new RuntimeException("Offset exceed");
    }

    public int getCount() {
        return count;
    }

    public int getCountAndIncrement() {
        return count++;
    }

    public String newTopicFile() {
        // new topic file and reset count to 0;
        return null;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Topic)) return false;
        return this.name.equals(((Topic) obj).name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public long getStartId() {
        return startId;
    }
}

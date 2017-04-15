package xyz.aornice.tofq;

/**
 * Created by robin on 10/04/2017.
 */

import java.util.concurrent.atomic.AtomicLong;

public class Topic {
    public static final int CARGO_MAX_NUM = 1000000;

    private String name;
    private AtomicLong maxId;
    private AtomicLong maxStoredId;
    private String newestFile;
    private int offset;


    public Topic(String name, String newestFile, int offset) {
        this.name = name;
        this.newestFile = newestFile;
        this.offset = offset;
        this.maxId = new AtomicLong(0);
        this.maxStoredId = new AtomicLong(0);
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

    public void incrementOffset() {
        offset++;
        if (offset >= CARGO_MAX_NUM) throw new RuntimeException("Offset exceed");
    }

    public void incrementOffset(int val) {
        offset += val;
        if (offset >= CARGO_MAX_NUM) throw new RuntimeException("Offset exceed");
    }

    public int getOffset() {
        return offset;
    }

    public int getOffsetAndIncrement() {
        return offset++;
    }

    public String newTopicFile() {
        // new topic file and reset offset to 0;
        return null;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Topic)) return false;
        return this.name.equals(((Topic) obj).name);
    }

    public int hashCode() {
        return name.hashCode();
    }
}

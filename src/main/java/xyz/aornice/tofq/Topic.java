package xyz.aornice.tofq;

/**
 * Created by robin on 10/04/2017.
 */

import java.util.concurrent.atomic.AtomicLong;

public class Topic {
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

    public long incrementAndGetId() {
        return maxId.incrementAndGet();
    }

    public long getMaxStoredId() {
        return maxStoredId.get();
    }

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

    public int getOffset() {
        return offset;
    }

    public String newTopicFile() {
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

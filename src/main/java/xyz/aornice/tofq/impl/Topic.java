package xyz.aornice.tofq.impl;

/**
 * Created by robin on 10/04/2017.
 */

import java.util.concurrent.atomic.AtomicLong;

public class Topic {
    private String name;
    private AtomicLong maxId;
    private AtomicLong maxStoredId;


    public Topic(String name, long maxId) {
        this.name = name;
        this.maxId = new AtomicLong(maxId);
        this.maxStoredId = new AtomicLong(maxId);
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
        long origin = this.maxStoredId.get();
        if (origin >= id) return false;
        return this.maxStoredId.compareAndSet(origin, id);
    }
}

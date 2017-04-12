package xyz.aornice.tofq;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by drfish on 11/04/2017.
 */
public class ReferenceCounter {
    private final AtomicLong value = new AtomicLong();
    private final Runnable releaseTask;

    public ReferenceCounter(Runnable releaseTask) {
        this.releaseTask = releaseTask;
    }

    public void reserve() throws IllegalStateException {
        while (true) {
            long v = value.get();
            if (v <= 0) {
                throw new IllegalStateException("has Released before reserve");
            }
            if (value.compareAndSet(v, v + 1)) {
                break;
            }
        }
    }

    public void release() throws IllegalStateException {
        while (true) {
            long v = value.get();
            if (v <= 0) {
                throw new IllegalStateException("has Released before release");
            }
            if (value.compareAndSet(v, v - 1)) {
                if (v == 1) {
                    releaseTask.run();
                }
                break;
            }
        }
    }

    public long getCount() {
        return value.get();
    }
}

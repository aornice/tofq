package xyz.aornice.tofq;

import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by drfish on 11/04/2017.
 */
public interface ReferenceCount extends Closeable {
    void release() throws IllegalStateException;

    static void releaseAll(List<WeakReference<ReferenceCount>> refCountRefs) {
        for (WeakReference<? extends ReferenceCount> refCountRef : refCountRefs) {
            if (refCountRef == null) {
                continue;
            }
            ReferenceCount refCount = refCountRef.get();
            if (refCount != null) {
                try {
                    refCount.release();
                } catch (IllegalStateException e) {
                    LoggerFactory.getLogger(ReferenceCount.class).debug("", e);
                }
            }
        }
    }

    default void close() throws IOException {
        release();
    }

    void reserve() throws IllegalStateException;


    default boolean tryReserve() {
        try {
            if (referenceCount() > 0) {
                reserve();
                return true;
            }
        } catch (IllegalStateException e) {
            LoggerFactory.getLogger(ReferenceCount.class).debug("", e);
        }
        return false;
    }

    long referenceCount();
}

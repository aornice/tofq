package xyz.aornice.tofq;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by drfish on 11/04/2017.
 */
public interface ReferenceCount extends Closeable {
    void release() throws IllegalStateException;

    static void releaseAll(List<WeakReference<ReferenceCount>> refCountRefs) {
        for (WeakReference<? extends ReferenceCount> refCountRef : refCountRefs) {
            if (refCountRef != null) {
                continue;
            }
            ReferenceCount refCount = refCountRef.get();
            if (refCount != null) {
                refCount.release();
            }
        }
    }

    default void close() {
        release();
    }

    void reserve() throws IllegalStateException;

    long referenceCount();
}

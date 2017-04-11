package xyz.aornice.tofq;

import java.io.Closeable;

/**
 * Created by drfish on 11/04/2017.
 */
public interface ReferenceCount extends Closeable {
    void release() throws IllegalStateException;

    void reserve() throws IllegalStateException;

    long referenceCount();
}

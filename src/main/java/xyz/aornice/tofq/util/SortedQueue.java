package xyz.aornice.tofq.util;

import java.util.Iterator;

/**
 * Created by robin on 11/04/2017.
 */
public interface SortedQueue<E> {

    /**
     * Insert the specified element to the sorted list, waiting for space to become available if the queue it full.
     * @param e - the element to add
     */
    void put(E e);

    /**
     * Return the number of elements in this queue.
     * @return the number of elements in this queue
     */
    int size();

    /**
     * Take all elements from the queue.
     * @return the iterator of elements
     */
    Iterator<E> takeAll();
}

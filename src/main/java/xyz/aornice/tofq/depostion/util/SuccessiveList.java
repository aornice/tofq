package xyz.aornice.tofq.depostion.util;

import java.util.List;

/**
 * Created by robin on 11/04/2017.
 */
public interface SuccessiveList<E> {

    /**
     * Insert the specified element to the sorted list, waiting for space to become available if the queue it full.
     *
     * @param e - the element to add
     */
    void put(E e);

    /**
     * Return the number of elements in this queue.
     *
     * @return the number of elements in this queue
     */
    int size();

    /**
     * Return the number of successive elements start from head in this queue.
     *
     * @return the number of successive elements start from head in this queue
     */
    int successiveSize();

    /**
     * Take all successive elements start from head in this queue.
     */
    boolean takeAllSuccessive(long head, List<E> list);
}

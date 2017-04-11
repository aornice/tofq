package xyz.aornice.tofq.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by robin on 11/04/2017.
 */
public class ConcurrentLinkedSortedQueue<E> implements SortedQueue<E> {

    private PriorityBlockingQueue<E> queue;

    public ConcurrentLinkedSortedQueue(int initialCapacity, Comparator<? super E> comparator) {
        queue = new PriorityBlockingQueue<E>(initialCapacity, comparator);
    }

    @Override
    public void put(E e) {
        queue.put(e);
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public Iterator<E> takeAll() {
        ArrayList<E> list = new ArrayList<E>(size());
        queue.drainTo(list);
        return list.iterator();
    }
}

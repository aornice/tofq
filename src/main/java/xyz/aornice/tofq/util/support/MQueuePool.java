package xyz.aornice.tofq.util.support;

import xyz.aornice.tofq.util.Pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MQueuePool<E> implements Pool<E> {

    private static final int POOL_SIZE = 1 << 8;

    private BlockingQueue<E> queue;

    public MQueuePool() {
        queue = new ArrayBlockingQueue<E>(POOL_SIZE);
    }

    @Override
    public void put(E o) throws InterruptedException {
            queue.put(o);
    }

    @Override
    public E take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    @Override
    public E poll() {
        return queue.poll();
    }

    @Override
    public void offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        queue.offer(e, timeout, unit);
    }

    @Override
    public boolean offer(E e) {
        return queue.offer(e);
    }
}

package xyz.aornice.tofq.util;


import java.util.concurrent.TimeUnit;

public interface Pool<E> {

    void put(E e) throws InterruptedException;

    E take() throws InterruptedException;

    E poll(long timeout, TimeUnit unit) throws InterruptedException;

    E poll();

    void offer(E e, long timeout, TimeUnit unit) throws InterruptedException;

    boolean offer(E e);
}

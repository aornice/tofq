package xyz.aornice.tofq.persistence;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.out;


/**
 * Aornice team
 * Created by robin on 10/09/2017.
 */

@RunWith(Parameterized.class)
public class Mul2OneTest {
    private int threadNum;
    private int count;
    private int size;
    private Lock lock = new ReentrantLock();
    private FileWriter fileWriter;

    public Mul2OneTest(int threadNum, int count, int size) {
        this.threadNum = threadNum;
        this.count = count;
        this.size = size;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        int count = 1 << 21;
        return Arrays.asList(new Object[][]{
                {2, count, 1 << 7}, {4, count, 1 << 7}, {8, count, 1 << 7}
                // indexoutofbound
//                ,{2, count, 1 << 10}, {4, count, 1 << 10}, {8, count, 1 << 10}
        });
    }

    @Before
    public void setup() {
        String file = "/tmp/test";
        try {
            Files.delete(Paths.get(file));
        } catch (IOException e) {
        }
        fileWriter = new FileWriter(file);
    }

    @Test
    public void lockFileHelper() throws InterruptedException {
        int cnt = count / threadNum;
        Runnable writer = () -> {
            byte[] bytes = new byte[size];
            Arrays.fill(bytes, (byte) 2);
            for (int i = 0; i < cnt; i++) {
                writeWithLock(bytes);
            }
        };

        Thread[] threads = new Thread[threadNum];
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadNum; i++) {
            threads[i] = new Thread(writer);
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
        long end = System.currentTimeMillis();
        long timeCost = end - start;
        int tps = (int) (count / (double) timeCost * 1000);
        out.printf("LockFile [ThreadNum: %d, Count: %d, Size: %d] cost %dms with tps %s\n", threadNum, count, size, timeCost, intToStr(tps));
    }

    @Test
    public void blockingQueue() throws InterruptedException {
        BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(1 << 16);
        int cnt = count / threadNum;
        Runnable producer = () -> {
            byte[] bytes = new byte[size];
            Arrays.fill(bytes, (byte) 2);
            for (int i = 0; i < cnt; i++) {
                try {
                    queue.put(bytes);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable writer = () -> {
            for (int i = 0; i < count; i++) {
                try {
                    write(queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread[] threads = new Thread[threadNum + 1];
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadNum + 1; i++) {
            threads[i] = (i + 1 == threadNum ? new Thread(writer) : new Thread(producer));
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
        long end = System.currentTimeMillis();
        long timeCost = end - start;
        int tps = (int) (count / (double) timeCost * 1000);
        out.printf("BlockingcQueue [ThreadNum: %d, Count: %d, Size: %d] cost %dms with tps %s\n", threadNum, count, size, timeCost, intToStr(tps));
    }

    @Test
    public void disruptor() throws InterruptedException {
        RingBuffer<Event<byte[]>> ringBuffer = RingBuffer.createMultiProducer(new Factory<>(), 1 << 16);

        int cnt = count / threadNum;
        Runnable producer = () -> {
            byte[] bytes = new byte[size];
            Arrays.fill(bytes, (byte) 2);
            for (int i = 0; i < cnt; i++) {
                long seq = ringBuffer.next();
                Event<byte[]> event = ringBuffer.get(seq);
                event.setValue(bytes);
                ringBuffer.publish(seq);
            }
        };

        Sequence consumedSeq = new Sequence();
        consumedSeq.set(ringBuffer.getCursor());
        ringBuffer.addGatingSequences(consumedSeq);
        SequenceBarrier barrier = ringBuffer.newBarrier();
        Runnable writer = () -> {
            long knownPublishedSeq = -1;
            for (int i = 0; i < count; i++) {
                long l = consumedSeq.get() + 1;
                while (knownPublishedSeq < l) {
                    try {
                        knownPublishedSeq = barrier.waitFor(l);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Event<byte[]> eventHolder = ringBuffer.get(l);
                byte[] bytes = eventHolder.getValue();
                consumedSeq.incrementAndGet();
                write(bytes);
            }
        };


        Thread[] threads = new Thread[threadNum + 1];
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadNum + 1; i++) {
            threads[i] = (i + 1 == threadNum ? new Thread(writer) : new Thread(producer));
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }

        long end = System.currentTimeMillis();
        long timeCost = end - start;
        int tps = (int) (count / timeCost * 1000);
        out.printf("Disruptor [ThreadNum: %d, Count: %d, Size: %d] cost %dms with tps %s\n", threadNum, count, size, timeCost, intToStr(tps));
    }

    private void write(byte[] bytes) {
        fileWriter.write(bytes);
    }

    private void writeWithLock(byte[] bytes) {
        lock.lock();
        try {
            write(bytes);
        } finally {
            lock.unlock();
        }
    }

    private static String intToStr(int n) {
        return NumberFormat.getNumberInstance(Locale.US).format(n);
    }

    static class Event<T> {
        private T item;

        public T getValue() {
            T t = item;
            item = null;
            return t;
        }

        public T readValue() {
            return item;
        }

        public void setValue(T event) {
            this.item = event;
        }
    }

    static class Factory<T> implements EventFactory<Event<T>> {
        @Override
        public Event<T> newInstance() {
            return new Event<>();
        }

    }


}
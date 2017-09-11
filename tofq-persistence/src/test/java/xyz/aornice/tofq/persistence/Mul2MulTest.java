package xyz.aornice.tofq.persistence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Aornice team
 * Created by drfish on 11/09/2017.
 */
@RunWith(Parameterized.class)
public class Mul2MulTest {
    private int threadNum;
    private int count;
    private int size;
    private int fileNum;
    private FileWriter[] fileWriters;
    private Lock[] locks;

    public Mul2MulTest(int threadNum, int count, int size, int fileNum) {
        this.threadNum = threadNum;
        this.count = count;
        this.size = size;
        this.fileNum = fileNum;
        fileWriters = new FileWriter[fileNum];
        locks = new Lock[fileNum];
    }


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        int count = 1 << 21;
        return Arrays.asList(new Object[][]{
                {2, count, 1 << 7, 4}, {4, count, 1 << 7, 4}, {8, count, 1 << 7, 4}
                , {2, count, 1 << 10, 4}, {4, count, 1 << 10, 4}, {8, count, 1 << 10, 4}
        });
    }

    @Before
    public void setup() {
        String fileName = "mul2mul";
        for (int i = 0; i < fileNum; i++) {
            fileWriters[i] = new FileWriter(fileName + i);
            locks[i] = new ReentrantLock();
        }
    }

    @Test
    public void lockFile() throws InterruptedException {
        int cnt = count / (threadNum * fileNum);
        Runnable writer = () -> {
            byte[] bytes = new byte[size];
            Arrays.fill(bytes, (byte) 2);
            for (int i = 0; i < cnt; i++) {
                for (int j = 0; j < fileNum; j++) {
                    writeWithLock(j, bytes);
                }
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
        System.out.printf("LockFile [ThreadNum: %d, Count: %d, Size: %d] cost %dms with tps %s\n", threadNum, count,
                size, timeCost, intToStr(tps));
    }

    @Test
    public void blockingQueue() throws InterruptedException {
        BlockingQueue<byte[]>[] queues = new ArrayBlockingQueue[fileNum];
        for (int i = 0; i < fileNum; i++) {
            queues[i] = new ArrayBlockingQueue<>(1 << 16);
        }
        int cnt = count / (threadNum * fileNum);
        Runnable producer = () -> {
            byte[] bytes = new byte[size];
            Arrays.fill(bytes, (byte) 2);
            for (int i = 0; i < cnt; i++) {
                for (int j = 0; j < fileNum; j++) {
                    try {
                        queues[j].put(bytes);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Runnable writer = () -> {
            for (int i = 0; i < count / fileNum; i++) {
                for (int j = 0; j < fileNum; j++) {
                    try {
                        fileWriters[j].write(queues[j].take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
        System.out.printf("BlockingcQueue [ThreadNum: %d, Count: %d, Size: %d] cost %dms with tps %s\n", threadNum,
                count, size, timeCost, intToStr(tps));
    }

    private static String intToStr(int n) {
        return NumberFormat.getNumberInstance(Locale.US).format(n);
    }

    private void writeWithLock(int index, byte[] bytes) {
        locks[index].lock();
        try {
            fileWriters[index].write(bytes);
        } finally {
            locks[index].unlock();
        }
    }
}
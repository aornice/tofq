/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package xyz.aornice.tofq.persistence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class One2OneTest {
    private int dataSize;
    private String fileName;
    private int totalCount;
    private int threadNum;
    private Thread[] threads;

    private static final int K_SHIFT=10;

    public One2OneTest(int dataSize, String fileName, int totalCount, int threadNum){
        this.dataSize = dataSize;
        this.fileName = fileName;
        this.totalCount = totalCount;
        this.threadNum = threadNum;
    }

    @Parameterized.Parameters
    public static Collection dataSizes(){
        return Arrays.asList(new Object[][]{
                {1, "B_file", 10_000_000, 10},           // 1B
                {1<<K_SHIFT, "KB_file", 1_000_000, 10},   // 1KB
                {(1<<K_SHIFT)<<K_SHIFT,"MB_file", 1_000,10},    // 1MB
                {(10<<K_SHIFT)<<K_SHIFT,"10MB_file", 100,10}    // 10MB

        });
    }

    @Before
    public void init(){
        threads = new Thread[threadNum];
        byte[] data = new byte[dataSize];
        int loopCount = totalCount / threadNum;
        for (int i=0;i<threadNum;i++){
            FileWriter writer = new FileWriter(fileName+i);
            threads[i] = new Thread(() -> {
                    for (int j=0;j<loopCount;j++) {
                        writer.write(data);
                    }
                }
            );
        }
    }

    @Test
    public void testWriteFile() throws InterruptedException {
        long start = System.currentTimeMillis();
        for (Thread t: threads){
            t.start();
        }
        for (Thread t: threads){
            t.join();
        }
        long end = System.currentTimeMillis();
        double timeCost =(end-start)/1000.0;
        int tps = (int)(totalCount /timeCost);
        System.out.println(fileName+" tps: "+tps+","+threadNum+" threads writes "+ totalCount +" times cost "+(end-start)+" milli seconds");
    }
}
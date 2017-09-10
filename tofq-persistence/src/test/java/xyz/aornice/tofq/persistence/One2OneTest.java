/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package xyz.aornice.tofq.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

@RunWith(Parameterized.class)
public class One2OneTest {
    private int dataSize;
    private String fileName;
    private int loopCount;

    private static final int K_SHIFT=10;

    public One2OneTest(int dataSize, String fileName, int loopCount){
        this.dataSize = dataSize;
        this.fileName = fileName;
        this.loopCount = loopCount;
    }

    @Parameterized.Parameters
    public static Collection dataSizes(){
        return Arrays.asList(new Object[][]{
                {1, "B_file", 10_000_000},           // 1B
                {1<<K_SHIFT, "KB_file", 1_000_000},   // 1KB
                {(1<<K_SHIFT)<<K_SHIFT,"MB_file", 1_000},    // 1MB
                {(10<<K_SHIFT)<<K_SHIFT,"10MB_file", 100}    // 10MB

        });
    }

    public static byte[] getRandomByteArray(int size){
        byte[] result= new byte[size];
        Random random= new Random();
        random.nextBytes(result);
        return result;
    }

    @Test
    public void testWriteFile(){
        FileWriter writer = new FileWriter(fileName);
        byte[] randomBytes = getRandomByteArray(dataSize);

        long start = System.currentTimeMillis();
        for (int i=0;i<loopCount;i++) {
            writer.write(randomBytes);
        }
        long end = System.currentTimeMillis();
        double timeCost =(end-start)/1000.0;
        int tps = (int)(loopCount/timeCost);
        System.out.println(fileName+" tps: "+tps+",write "+loopCount+" times cost "+(end-start)+" milli seconds");
    }
}
package impl;

import xyz.aornice.tofq.harbour.Harbour;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cat on 2017/4/12.
 */
public class HarbourMock implements Harbour{
    @Override
    public byte[] get(String fileName, long offset) {
        byte[] bs = new byte[1];
        bs[0] = 0x03;
        return bs;
    }

    @Override
    public byte[] get(String fileName, long offsetFrom, long offsetTo) {
        byte[]  bs = new byte[2];
        bs[0]=0x00;
        bs[1] = 0x01;
        return bs;
    }

    @Override
    public void put(String fileName, byte[] data) {
    }

    @Override
    public void put(String fileName, byte[] data, long offset) {

    }

    @Override
    public void flush(String fileName) {

    }

    @Override
    public long getLong(String fileName, long offset) {
        return 0;
    }

    @Override
    public int getInt(String fileName, long offset) {
        return 0;
    }

    @Override
    public void put(String fileName, long val, long offset) {

    }

    @Override
    public void put(String fileName, int val, long offset) {

    }
}

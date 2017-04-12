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
    public List<byte[]> get(String fileName, long offsetFrom, long offsetTo) {
        List<byte[]> result = new ArrayList<>((int)(offsetTo-offsetFrom));
        byte[]  bs = new byte[2];
        bs[0]=0x00;
        bs[1] = 0x01;
        for (long i=offsetFrom; i<offsetTo; i++){
            result.add(bs);
        }
        return result;
    }

    @Override
    public void put(String fileName, byte[] data) {
    }
}

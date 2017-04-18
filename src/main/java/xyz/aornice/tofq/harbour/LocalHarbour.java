package xyz.aornice.tofq.harbour;

import java.util.List;

/**
 * Created by drfish on 12/04/2017.
 */

public class LocalHarbour implements Harbour {
    private String location;
    private MappedFile mappedFile;

    public LocalHarbour(String location) {
        this.location = location;
    }

    @Override
    public byte[] get(String fileName, long offset) {
        return new byte[0];
    }

    @Override
    public byte[] get(String fileName, long offsetFrom, long offsetTo) {
        return new byte[0];
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
    public void put(String fileName, byte[] data) {

    }

    @Override
    public void put(String fileName, byte[] data, long offset) {

    }

    @Override
    public void put(String fileName, long val, long offset) {

    }

    @Override
    public void put(String fileName, int val, long offset) {

    }

    @Override
    public void flush(String fileName) {

    }

    @Override
    public void create(String fileName) {

    }
}

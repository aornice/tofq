package xyz.aornice.tofq.harbour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by drfish on 12/04/2017.
 */

public class LocalHarbour implements Harbour {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalHarbour.class);
    private String location;

    private MappedFile mappedFile;

    public LocalHarbour(String location) {
        this.location = location;
    }

    @Override
    public byte[] get(String fileName, long offsetFrom, long offsetTo) {
        return new byte[0];
    }

    @Override
    public long[] getLongs(String fileName, long offset, long count) {
        return new long[0];
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
    public boolean create(String fileName) {
        return false;
    }
}

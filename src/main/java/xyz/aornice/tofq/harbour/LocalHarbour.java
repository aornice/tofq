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
    public List<byte[]> get(String fileName, long offsetFrom, long offsetTo) {
        return null;
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
}

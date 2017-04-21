package xyz.aornice.tofq.harbour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by drfish on 12/04/2017.
 */

public class LocalHarbour implements Harbour {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalHarbour.class);
    private static final long DEFAULT_FILE_SIZE = 1000000;
    private static final long DEFAULT_BLOCK_SIZE = 4048;
    private static final int BYTE_BITS = 8;
    private String location;

    public LocalHarbour() {
    }

    public LocalHarbour(String location) {
        this.location = location;
    }


    private MappedBytes getMappedBytes(String fileName) {
        return getMappedBytes(fileName, DEFAULT_FILE_SIZE);
    }

    private MappedBytes getMappedBytes(String fileName, long fileSize) {
        MappedBytes mappedBytes = null;
        try {
            MappedFile mappedFile = MappedFile.getMappedFile(fileName, DEFAULT_BLOCK_SIZE);
            mappedBytes = mappedFile.acquireBytes(fileSize);
        } catch (IllegalAccessException | IOException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return mappedBytes;
    }

    @Override
    public byte[] get(String fileName, long offsetFrom, long offsetTo) {
        MappedBytes mappedBytes = getMappedBytes(fileName);
        // TODO change API to sovle this unsafe cast
        long size = offsetTo - offsetFrom;
        int count = (int) (size / BYTE_BITS);
        byte[] bytes = new byte[count];
        for (int i = 0; i < count; i++) {
            bytes[i] = mappedBytes.readByte(offsetFrom + i * BYTE_BITS);
        }
        return bytes;
    }

    @Override
    public List<Long> getLongs(String fileName, long offset, long count) {
        return null;
    }

    @Override
    public long getLong(String fileName, long offset) {
        MappedBytes mappedBytes = getMappedBytes(fileName);
        return mappedBytes.readLong(offset);
    }

    @Override
    public int getInt(String fileName, long offset) {
        MappedBytes mappedBytes = getMappedBytes(fileName);
        return mappedBytes.readInt(offset);
    }

    @Override
    public void put(String fileName, byte[] data, long offset) {
        long fileSize = offset + data.length * BYTE_BITS;
        MappedBytes mappedBytes = getMappedBytes(fileName, fileSize);
        for (int i = 0; i < data.length; i++) {
            mappedBytes.writeByte(offset + i * BYTE_BITS, data[i]);
        }
    }

    @Override
    public void put(String fileName, long val, long offset) {
        MappedBytes mappedBytes = getMappedBytes(fileName);
        mappedBytes.writeLong(offset, val);
    }

    @Override
    public void put(String fileName, int val, long offset) {
        MappedBytes mappedBytes = getMappedBytes(fileName);
        mappedBytes.writeInt(offset, val);
    }

    @Override
    public void flush(String fileName) {

    }

    @Override
    public boolean create(String fileName) {
        return false;
    }
}

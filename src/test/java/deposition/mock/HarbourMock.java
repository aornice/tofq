package deposition.mock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.aornice.tofq.harbour.Harbour;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Created by robin on 18/04/2017.
 */
public class HarbourMock implements Harbour{

    private static final Logger logger = LogManager.getLogger(HarbourMock.class);

    private byte[] file = new byte[100000];

    {
        byte[] count = ByteBuffer.allocate(4).putInt(0).array();
        byte[] startId = ByteBuffer.allocate(8).putLong(10).array();
        int i = 0;
        for (byte b: count) file[i++] = b;
        for (byte b: startId) file[i++] = b;

    }

    @Override
    public byte[] get(String fileName, long offsetFrom, long offsetTo) {
        return new byte[0];
    }

    @Override
    public List<Long> getLongs(String fileName, long offset, long count) {
        return null;
    }

    @Override
    public long getLong(String fileName, long offset) {
        return ByteBuffer.wrap(Arrays.copyOfRange(file, (int)offset, (int)(offset + 8))).getLong();
    }

    @Override
    public int getInt(String fileName, long offset) {
        return ByteBuffer.wrap(Arrays.copyOfRange(file, (int)offset, (int)(offset + 4))).getInt();
    }

    private void putHelper(String fileName, byte[] data, long offset) {
        System.arraycopy(data, 0, file, (int)offset, data.length);
    }

    @Override
    public void put(String fileName, byte[] data, long offset) {
        logger.debug("Put into {} [Offset {}] {}", fileName, offset, new String(data));
        putHelper(fileName, data, offset);
    }

    @Override
    public void put(String fileName, long val, long offset) {
        logger.debug("Put into {} [Offset {}] {}", fileName, offset, val);
        putHelper(fileName, ByteBuffer.allocate(8).putLong(val).array(), offset);
    }

    @Override
    public void put(String fileName, int val, long offset) {
        logger.debug("Put into {} [Offset {}] {}", fileName, offset, val);
        putHelper(fileName, ByteBuffer.allocate(4).putInt(val).array(), offset);
    }

    @Override
    public void flush(String fileName) {
        logger.debug("Flush data from memory to disk");
    }

    @Override
    public boolean create(String fileName) {
        file = new byte[100000];
        logger.debug("Create topic file {}", fileName);
        return true;
    }
}

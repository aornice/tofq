package xyz.aornice.tofq.harbour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.Setting;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by drfish on 12/04/2017.
 */

public class LocalHarbour implements Harbour {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalHarbour.class);
    private static final long DEFAULT_FILE_SIZE = 1000000;
    private static final long DEFAULT_BLOCK_SIZE = 4048;
    private static final int BYTE_BITS = 8;
    private static final String TEMP_FILE_PRIFIX = "tmp_";
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
        // TODO change API to solve this unsafe cast
        long size = offsetTo - offsetFrom;
        int count = (int) size;
        byte[] bytes = new byte[count];
        for (int i = 0; i < count; i++) {
            bytes[i] = mappedBytes.readByte(offsetFrom + i);
        }
        return bytes;
    }

    @Override
    public List<Long> getLongs(String fileName, long offset, long count) {
        MappedBytes mappedBytes = getMappedBytes(fileName);
        List<Long> data = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            data.add(mappedBytes.readLong(offset + i * 8));
        }
        return data;
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
        // TODO review file size set
        long fileSize = offset + data.length * BYTE_BITS;
        MappedBytes mappedBytes = getMappedBytes(fileName, fileSize);
        for (int i = 0; i < data.length; i++) {
            mappedBytes.writeByte(offset + i, data[i]);
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
        // TODO flush the file to disk
    }

    @Override
    public boolean create(String fileName) {
        return true;
    }

    @Override
    public boolean remove(String fileName) {
        if (fileName == null)
            return false;
        File file = new File(fileName);
        if (file.exists()) {
            if (Setting.LAZY_DELETE) {
                String newName = generateNewName(fileName);
                return file.renameTo(new File(newName));
            } else {
                deleteDir(file);
                return true;
            }
        }
        return false;
    }

    private boolean deleteDir(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                deleteDir(f);
            }
            if (!file.delete()) {
                return false;
            }
        } else {
            if (!file.delete()) {
                return false;
            }
        }
        return true;
    }


    private String generateNewName(String fileName) {
        String newName;
        if (fileName.endsWith(File.separator)) {
            fileName = fileName.substring(0, fileName.length() - 1);
        }
        int index = fileName.lastIndexOf(File.separator);
        if (index == -1) {
            newName = TEMP_FILE_PRIFIX + fileName;
        } else {
            newName = fileName.substring(0, index + 1) + TEMP_FILE_PRIFIX + fileName.substring(index + 1);
        }
        return newName;
    }

}

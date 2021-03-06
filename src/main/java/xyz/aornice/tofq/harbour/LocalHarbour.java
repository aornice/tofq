package xyz.aornice.tofq.harbour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.Setting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by drfish on 12/04/2017.
 */

public class LocalHarbour implements Harbour {
    private static final Logger logger = LoggerFactory.getLogger(LocalHarbour.class);
    private static final long DEFAULT_FILE_SIZE = 1000000;
    private static final long DEFAULT_CHUNK_SIZE = DEFAULT_FILE_SIZE + 1;
    private static final int BYTE_BITS = 8;
    private static final String TEMP_FILE_PRIFIX = "tmp_";
    private static final ConcurrentMap<String, List<MappedBytes>> bytesMap = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, MappedFile> mappedFilesMap = new ConcurrentHashMap<>();
    private String location;

    public LocalHarbour() {
    }

    public static void TEST_InitFields(){
        bytesMap.clear();
        mappedFilesMap.clear();
    }

    public LocalHarbour(String location) {
        this.location = location;
    }


    private MappedBytes getMappedBytes(String fileName) {
        return getMappedBytes(fileName, DEFAULT_FILE_SIZE);
    }

    private MappedBytes getMappedBytes(String fileName, long fileSize) {
        // get mapped file
        MappedFile mappedFile = getMappedFile(fileName);
        // get mapped bytes from cache
        int index = (int) (fileSize / mappedFile.getChunkSize());
        if (bytesMap.containsKey(fileName)) {
            List<MappedBytes> bytes = bytesMap.get(fileName);
            if (index < bytes.size() && bytes.get(index) != null) {
                return bytes.get(index);
            }
        } else {
            bytesMap.put(fileName, new ArrayList<>());
        }
        // acquire new mapped bytes from mapped file
        MappedBytes mappedBytes = null;
        try {
            mappedBytes = mappedFile.acquireBytes(fileSize);
            List<MappedBytes> bytesList = bytesMap.get(fileName);
            while (bytesList.size() <= index) {
                bytesList.add(null);
            }
            bytesList.set(index, mappedBytes);
        } catch (IOException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return mappedBytes;
    }

    private MappedFile getMappedFile(String fileName) {
        MappedFile mappedFile = null;
        if (mappedFilesMap.containsKey(fileName)) {
            mappedFile = mappedFilesMap.get(fileName);
        } else {
            try {
                mappedFile = MappedFile.getMappedFile(fileName, DEFAULT_CHUNK_SIZE);
                mappedFilesMap.put(fileName, mappedFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mappedFile;
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
        MappedFile mappedFile = getMappedFile(fileName);
        try {
            mappedFile.force(false);
        } catch (IOException e) {
            logger.debug("Flush file content of {} failed", fileName);
        }
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

    @Override
    public void put(String fileName, ByteBuffer buf, long offset) {
        put(fileName, buf.array(), offset);
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

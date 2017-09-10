package xyz.aornice.tofq.persistence;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Aornice team
 * Created by drfish on 10/09/2017.
 */
class MappedFile {
    private final RandomAccessFile memoryMappedFile;
    private MappedByteBuffer writer;
    private int mappedSize;
    private static final int DEFAULT_MAPPED_SIZE = 16 * 1024 * 1024;
    private long start = 0;

    public MappedFile(String fileName) throws IOException {
        memoryMappedFile = new RandomAccessFile(fileName, "rw");
        mappedSize = DEFAULT_MAPPED_SIZE;
        writer = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, mappedSize);
    }

    public MappedFile(String fileName, int mappedSize) throws IOException {
        memoryMappedFile = new RandomAccessFile(fileName, "rw");
        this.mappedSize = mappedSize;
        writer = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, this.mappedSize);
    }

    private void checkPosition(int index) {
        if (index >= (mappedSize - 1024)) {
            try {
                reMap(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(byte[] src, int offset, int length) {
        checkPosition(offset + length);
        for (int i = 0; i < length; i++) {
            write(src[i], offset + i);
        }
    }

    public void write(long value, int offset) {
        checkPosition(offset);
        writer.putLong(offset, value);
    }

    public void write(int value, int offset) {
        checkPosition(offset);
        writer.putInt(offset, value);
    }

    public void write(byte value, int offset) {
        checkPosition(offset);
        writer.put(offset, value);
    }

    public byte getByte(int index) {
        checkPosition(index);
        return writer.get(index);
    }

    public int getInt(int index) {
        checkPosition(index);
        int result = writer.getInt(index);
        return result;
    }

    public long getLong(int index) {
        checkPosition(index);
        long result = writer.getLong(index);
        return result;
    }

    public void reMap(int index) throws IOException {
        int newSize = mappedSize + DEFAULT_MAPPED_SIZE;
        if (index > newSize) {
            newSize = index;
        }
        this.writer = memoryMappedFile.getChannel().map
                (FileChannel.MapMode.READ_WRITE,
                        0,
                        newSize);
        mappedSize = newSize;
    }
}

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
    private static final int DEFAULT_MAPPED_SIZE = 16 * 1024 * 1024;

    public MappedFile(String fileName) throws IOException {
        memoryMappedFile = new RandomAccessFile(fileName, "rw");
        writer = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, DEFAULT_MAPPED_SIZE);
    }

    public MappedFile(String fileName, int mappedSize) throws IOException {
        memoryMappedFile = new RandomAccessFile(fileName, "rw");
        writer = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, mappedSize);
    }


    public void write(byte[] src, int offset, int length) {
        for (int i = 0; i < length; i++) {
            write(src[i], offset + i);
        }
    }

    public void write(byte value, int offset) {
        writer.put(offset, value);
    }
}

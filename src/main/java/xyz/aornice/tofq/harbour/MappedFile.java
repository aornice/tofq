package xyz.aornice.tofq.harbour;

import xyz.aornice.tofq.ReferenceCount;
import xyz.aornice.tofq.harbour.util.OS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by drfish on 11/04/2017.
 */
public class MappedFile implements ReferenceCount {
    public static final long DEFAULT_CAPACITY = 1L << 30;

    private final RandomAccessFile randomFile;
    private final FileChannel fileChannel;
    private final long blockSize;
    private final long overlapSize;
    private final AtomicBoolean isClosed = new AtomicBoolean();
    private final long capacity;
    private final List<WeakReference<MappedBytes>> cache = new ArrayList<>();

    protected MappedFile(RandomAccessFile rFile, long blockSize, long overlapSize, long capacity) {
        this.randomFile = rFile;
        this.fileChannel = rFile.getChannel();
        this.blockSize = blockSize;
        this.overlapSize = overlapSize;
        this.capacity = capacity;
    }

    public static MappedFile getMappedFile(String fileName, long blockSize, long overlapSize) throws FileNotFoundException {
        RandomAccessFile rFile = new RandomAccessFile(fileName, "rw");
        return new MappedFile(rFile, blockSize, overlapSize, DEFAULT_CAPACITY);
    }

    public static MappedFile getMappedFile(String fileName, long blockSize) throws FileNotFoundException {
        return getMappedFile(fileName, blockSize, OS.pageSize());
    }

    public MappedBytes acquireBytes(long position) throws IOException, InvocationTargetException, IllegalAccessException {
        if (isClosed.get()) {
            throw new IOException("MappedFile has closed");
        }
        if (position < 0) {
            throw new IOException("Attempt to access a negative position");
        }
        int blocks = (int) (position / blockSize);
        synchronized (cache) {
            while (cache.size() <= blocks) {
                cache.add(null);
            }
            WeakReference<MappedBytes> mbRef = cache.get(blocks);
            if (mbRef != null) {
                MappedBytes mb = mbRef.get();
                if (mb != null) {
                    return mb;
                }
            }
            long minSize = (blocks + 1) * blockSize + overlapSize;
            long size = fileChannel.size();
            if (size < minSize) {
                try (FileLock lock = fileChannel.lock()) {
                    size = fileChannel.size();
                    if (size < minSize) {
                        randomFile.setLength(minSize);
                    }
                }
            }

            long mappedSize = blockSize + overlapSize;
            long address = OS.map(fileChannel, FileChannel.MapMode.READ_WRITE, blocks * blockSize, mappedSize);
            MappedBytes mb = new MappedBytes(this, blocks * blockSize, address, mappedSize);
            cache.set(blocks, new WeakReference<>(mb));
            return mb;
        }
    }

    @Override
    public void release() throws IllegalStateException {

    }

    @Override
    public void reserve() throws IllegalStateException {

    }

    @Override
    public long referenceCount() {
        return 0;
    }

    @Override
    public void close() throws IOException {

    }
}

package xyz.aornice.tofq.harbour;

import xyz.aornice.tofq.ReferenceCount;
import xyz.aornice.tofq.ReferenceCounter;
import xyz.aornice.tofq.harbour.util.OS;

import java.io.File;
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
    ReferenceCounter refCounter = new ReferenceCounter(this::doRelease);

    protected MappedFile(RandomAccessFile rFile, long blockSize, long overlapSize, long capacity) {
        this.randomFile = rFile;
        this.fileChannel = rFile.getChannel();
        this.blockSize = blockSize;
        this.overlapSize = overlapSize;
        this.capacity = capacity;
    }

    public static MappedFile getMappedFile(String fileName, long blockSize, long overlapSize) throws FileNotFoundException {
        checkDir(fileName);
        RandomAccessFile rFile = new RandomAccessFile(fileName, "rw");
        return new MappedFile(rFile, blockSize, overlapSize, DEFAULT_CAPACITY);
    }

    private static void checkDir(String filename) {
        int index = filename.lastIndexOf(File.separator);
        if (index != -1) {
            File file = new File(filename.substring(0, index));
            if (!file.exists())
                file.mkdirs();
        }
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

//            long mappedSize = blockSize + overlapSize;
//            long address = OS.map(fileChannel, FileChannel.MapMode.READ_WRITE, blocks * blockSize, mappedSize);
            long address = OS.map(fileChannel, FileChannel.MapMode.READ_WRITE, 0, position);
            MappedBytes mb = new MappedBytes(this, blocks * blockSize, address, position);
            cache.set(blocks, new WeakReference<>(mb));
            return mb;
        }
    }


    private void doRelease() {
        for (int i = 0; i < cache.size(); i++) {
            WeakReference<MappedBytes> mbRef = cache.get(i);
            if (mbRef == null) {
                continue;
            }
            MappedBytes mb = mbRef.get();
            if (mb != null) {
                long count = mb.refCount();
                if (count > 0) {
                    mb.release();
                    if (count > 1) {
                        continue;
                    }
                }
            }
            cache.set(i, null);
        }
        try {
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() throws IllegalStateException {
        refCounter.release();
    }

    @Override
    public void reserve() throws IllegalStateException {
        refCounter.reserve();
    }

    @Override
    public long referenceCount() {
        return refCounter.getCount();
    }

    @Override
    public void close() throws IOException {
        if (!isClosed.compareAndSet(false, true)) {
            return;
        }
        synchronized (cache) {
            ReferenceCount.releaseAll((List) cache);
        }
        release();
    }
}

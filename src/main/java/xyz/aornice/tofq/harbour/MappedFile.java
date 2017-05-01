package xyz.aornice.tofq.harbour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.ReferenceCount;
import xyz.aornice.tofq.ReferenceCounter;
import xyz.aornice.tofq.util.OS;

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
    private static final Logger logger = LoggerFactory.getLogger(MappedFile.class);
    public static final long DEFAULT_CAPACITY = 1L << 30;

    private final RandomAccessFile randomFile;
    private final FileChannel fileChannel;
    private final long chunkSize;
    private final long overlapSize;
    private final AtomicBoolean isClosed = new AtomicBoolean();
    private final long capacity;
    private final List<WeakReference<MappedBytes>> cache = new ArrayList<>();
    ReferenceCounter refCounter = new ReferenceCounter(this::doRelease);

    protected MappedFile(RandomAccessFile rFile, long chunkSize, long overlapSize, long capacity) {
        this.randomFile = rFile;
        this.fileChannel = rFile.getChannel();
        this.chunkSize = chunkSize;
        this.overlapSize = overlapSize;
        this.capacity = capacity;
    }

    public static MappedFile getMappedFile(String fileName, long chunkSize, long overlapSize) throws FileNotFoundException {
        checkDir(fileName);
        RandomAccessFile rFile = new RandomAccessFile(fileName, "rw");
        return new MappedFile(rFile, chunkSize, overlapSize, DEFAULT_CAPACITY);
    }

    private static void checkDir(String filename) {
        int index = filename.lastIndexOf(File.separator);
        if (index != -1) {
            File file = new File(filename.substring(0, index));
            if (!file.exists())
                file.mkdirs();
        }
    }

    public static MappedFile getMappedFile(String fileName, long chunkSize) throws FileNotFoundException {
        return getMappedFile(fileName, chunkSize, OS.pageSize());
    }

    public MappedBytes acquireBytes(long position) throws IOException, InvocationTargetException, IllegalAccessException {
        if (isClosed.get()) {
            throw new IOException("MappedFile has closed");
        }
        if (position < 0) {
            throw new IOException("Attempt to access a negative position");
        }
        int chunk = (int) (position / chunkSize);
        synchronized (cache) {
            while (cache.size() <= chunk) {
                cache.add(null);
            }
            WeakReference<MappedBytes> mbRef = cache.get(chunk);
            if (mbRef != null) {
                MappedBytes mb = mbRef.get();
                if (mb != null && mb.tryReserve()) {
                    return mb;
                }
            }
            long minSize = (chunk + 1) * chunkSize + overlapSize;
            long size = fileChannel.size();
            if (size < minSize) {
                try {
                    try (FileLock lock = fileChannel.lock()) {
                        size = fileChannel.size();
                        if (size < minSize) {
                            randomFile.setLength(minSize);
                        }
                    }
                } catch (IOException e) {
                    throw new IOException("Failed to resize to" + minSize, e);
                }
            }
            long mappedSize = chunkSize + overlapSize;
            long address = OS.map(fileChannel, FileChannel.MapMode.READ_WRITE, chunk * chunkSize, mappedSize);
            long safeCapacity = chunkSize + overlapSize / 2;
//            long address = OS.map(fileChannel, FileChannel.MapMode.READ_WRITE, 0, position);
            MappedBytes mb = new MappedBytes(this, chunk * chunkSize, address, safeCapacity);
            cache.set(chunk, new WeakReference<>(mb));
            mb.reserve();
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
                long count = mb.referenceCount();
                if (count > 0) {
                    try {
                        mb.release();
                    } catch (IllegalStateException e) {
                        logger.debug("", e);
                    }
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

    public long getCapacity() {
        return capacity;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public long getOverlapSize() {
        return overlapSize;
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

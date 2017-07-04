package xyz.aornice.tofq.depostion.util.support;

import xyz.aornice.tofq.Identifiable;
import xyz.aornice.tofq.depostion.util.SuccessiveList;
import xyz.aornice.tofq.util.Memory;
import xyz.aornice.tofq.util.UnsafeMemory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Created by robin on 11/04/2017.
 */
public class ConcurrentSuccessiveList<E extends Identifiable> implements SuccessiveList<E> {

    private static final int NORMAL = 1;
    private static final int RESIZING = 2;

    private static final Memory unsafe = UnsafeMemory.INSTANCE;
    private static final long sizeOffset;
    private static final long successiveSizeOffset;
    private static final long stateOffset;

    private final ReentrantReadWriteLock lock;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;

    private long head;
    private volatile int size = 0;
    private int successiveSize = 0;
    private volatile Object[] items;

    private volatile int state = NORMAL;

    static {
        try {
            sizeOffset = unsafe.getFieldOffset(ConcurrentSuccessiveList.class.getDeclaredField("size"));
            successiveSizeOffset = unsafe.getFieldOffset(ConcurrentSuccessiveList.class.getDeclaredField("successiveSize"));
            stateOffset = unsafe.getFieldOffset(ConcurrentSuccessiveList.class.getDeclaredField("state"));
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    public ConcurrentSuccessiveList(int initialCapacity, long head) {
        this.lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
        this.head = head;
        this.items = new Object[resizeStamp(initialCapacity)];
    }

    /**
     * The element must have different ID
     *
     * @param e - the element to add
     */
    @Override
    public void put(E e) {
        if (e == null) throw new NullPointerException();
        if (e.getId() < head) throw new RuntimeException("Cargo has already been deposited");
        final ReentrantReadWriteLock.ReadLock readLock = this.readLock;
        readLock.lock();
        try {
            final int offset = (int) (e.getId() - head);
            while (offset >= items.length && !ensureCapability(offset + 1)) ;
            items[offset] = e;
            int size = this.size;
            while (offset + 1 > size && !unsafe.compareAndSwapInt(this, sizeOffset, size, offset + 1))
                size = this.size;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int successiveSize() {
        final ReentrantReadWriteLock.ReadLock readLock = this.readLock;
        readLock.lock();
        int newSuccessiveSize;
        try {
            newSuccessiveSize = this.successiveSize;
            final Object[] items = this.items;
            for (int i = newSuccessiveSize; i < size; i++) {
                if (items[i] == null) break;
                newSuccessiveSize++;
            }
            int successiveSize = this.successiveSize;
            while (newSuccessiveSize > successiveSize
                    && !unsafe.compareAndSwapInt(this, successiveSizeOffset, successiveSize, newSuccessiveSize))
                successiveSize = this.successiveSize;
        } finally {
            readLock.unlock();
        }
        return this.successiveSize;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean takeAllSuccessive(long head, List<E> list) {
        if (head != this.head) return false;
        final ReentrantReadWriteLock.WriteLock writeLock = this.writeLock;
        final int successSize = successiveSize();
        writeLock.lock();
        try {
            final int size = this.size;
            for (int i = 0; i < successSize; i++) {
                list.add((E) items[i]);
                items[i] = null;
            }
            System.arraycopy(this.items, successiveSize, this.items, 0, size - successSize);
            Arrays.fill(this.items, size - successiveSize, size, null);
            this.head += successSize;
            this.successiveSize = 0;
            this.size = size - successSize;
        } finally {
            writeLock.unlock();
        }
        return true;
    }

    private boolean ensureCapability(int need) {
        if (state == RESIZING
                || !unsafe.compareAndSwapInt(this, stateOffset, NORMAL, RESIZING)
                || items.length > resizeStamp(need))
            return false;
        Object[] newItems = new Object[resizeStamp(need)];
        System.arraycopy(items, 0, newItems, 0, items.length);
        Arrays.fill(items, null);
        items = newItems;
        if (!unsafe.compareAndSwapInt(this, stateOffset, RESIZING, NORMAL))
            throw new RuntimeException("Ensure Capability state error");
        return true;
    }

    static int resizeStamp(int n) {
        return (1 << (32 - Integer.numberOfLeadingZeros(n))) - 1;
    }
}

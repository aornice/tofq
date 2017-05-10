package xyz.aornice.tofq.furnisher.util.support;

import xyz.aornice.tofq.Setting;


public class ArraySortedMap {

    private static final int DEFAULT_CAPACITY = Setting.DEFAULT_BATCH_DEPOSITION_SIZE * 3 / 2;

    long[] keys;
    int[] values;
    int capacity = 0;
    int size = 0;
    int start = 0;
    int ptr = 0;

    public ArraySortedMap() {
        this(DEFAULT_CAPACITY);
    }

    public ArraySortedMap(int initialCapacity) {
        capacity = resizeStamp(initialCapacity);
        keys = new long[capacity];
        values = new int[capacity];
    }

    public void add(long key, int value) {
        keys[ptr] = key;
        values[ptr] = value;

        ptr = (ptr + 1) % capacity;
        size++;
        if (ptr == start) ensureCapacity();
    }

    public int findLEAndClear(long key) {
        int low = 0;
        int high = size() - 1;
        if (high < low) return -1;

        int mid = 0;
        while (low <= high) {
            mid = (low + high) >>> 1;
            long midVal = getKey(mid);
            long cmp = key - midVal;

            if (cmp > 0) low = mid + 1;
            else if (cmp < 0) high = mid - 1;
            else break;
        }

        mid = getKey(mid) > key ? mid - 1 : mid;

        int rst = mid < 0 ? -1 : getValue(mid);
        start = (start + mid + 1) % capacity;
        size = ptr < start ? capacity - start + ptr : ptr - start;

        return rst;
    }

    public int size() {
        return size;
    }

    private long getKey(int index) {
        if (index >= size()) throw new ArrayIndexOutOfBoundsException();
        return keys[(start + index) % capacity];
    }

    private int getValue(int index) {
        if (index >= size()) throw new ArrayIndexOutOfBoundsException();
        return values[(start + index) % capacity];
    }

    private void ensureCapacity() {
        final int nCapacity = capacity << 1;
        final long[] oKeys = keys;
        final int[] oValues = values;
        keys = new long[nCapacity];
        values = new int[nCapacity];

        System.arraycopy(oKeys, start, keys, 0, capacity - start);
        System.arraycopy(oKeys, 0, keys, capacity - start, ptr);

        System.arraycopy(oValues, start, values, 0, capacity - start);
        System.arraycopy(oValues, 0, values, capacity - start, ptr);

        capacity = nCapacity;
        start = 0;
        ptr = size;
    }

    static int resizeStamp(int n) {
        return (1 << (32 - Integer.numberOfLeadingZeros(n))) - 1;
    }
}

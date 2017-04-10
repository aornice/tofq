package xyz.aornice.tofq.harbour.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by drfish on 10/04/2017.
 */
public enum UnsafeMemory implements Memory {
    INSTANCE;

    private static final Unsafe UNSAFE;
    private final AtomicLong nativeMemoryUsed = new AtomicLong();

    static {
        try {
            Field unsafe = Jvm.getField(Unsafe.class, "theUnsafe");
            UNSAFE = (Unsafe) unsafe.get(null);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }


    @Override
    public int pageSize() {
        return UNSAFE.pageSize();
    }

    @Override
    public void setMemory(long address, long size, byte b) {
        UNSAFE.setMemory(address, size, b);
    }

    @Override
    public void freeMemory(long address, long size) {
        if (address != 0) {
            UNSAFE.freeMemory(address);
        }
        nativeMemoryUsed.addAndGet(-size);
    }

    @Override
    public long allocate(long capacity) throws IllegalArgumentException, OutOfMemoryError {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Invalid native memory capacity: " + capacity);
        }
        long address = UNSAFE.allocateMemory(capacity);
        if (address == 0) {
            throw new OutOfMemoryError("Not enough free native memory, capacity tried: " + capacity);
        }
        nativeMemoryUsed.addAndGet(capacity);
        return address;
    }

    @Override
    public long nativeMemoryUsed() {
        return nativeMemoryUsed.get();
    }

    @Override
    public byte readByte(Object object, long offset) {
        return UNSAFE.getByte(object, offset);
    }

    @Override
    public byte readByte(long address) {
        return UNSAFE.getByte(address);
    }

    @Override
    public void writeByte(Object object, long offset, byte i8) {
        UNSAFE.putByte(object, offset, i8);
    }

    @Override
    public void write(long address, byte i8) {
        UNSAFE.putByte(address, i8);
    }

    @Override
    public short readShort(Object object, long offset) {
        return UNSAFE.getShort(object, offset);
    }

    @Override
    public short readShort(long address) {
        return UNSAFE.getShort(address);
    }

    @Override
    public void writeShort(Object object, long offset, short i16) {
        UNSAFE.putShort(object, offset, i16);
    }

    @Override
    public void writeShort(long address, short i16) {
        UNSAFE.putShort(address, i16);
    }

    @Override
    public int readInt(long address) {
        return UNSAFE.getInt(address);
    }

    @Override
    public int readInt(Object object, long offset) {
        return UNSAFE.getInt(object, offset);
    }

    @Override
    public void writeInt(long address, int i32) {
        UNSAFE.putInt(address, i32);
    }

    @Override
    public void writeInt(Object object, long offset, int i32) {
        UNSAFE.putInt(object, offset, i32);
    }

    @Override
    public void writeOrderedInt(long offset, int i32) {
        UNSAFE.putOrderedInt(null, offset, i32);
    }

    @Override
    public void writeOrderedInt(Object object, long offset, int i32) {
        UNSAFE.putOrderedInt(object, offset, i32);
    }

    @Override
    public long readLong(long address) {
        return UNSAFE.getLong(address);
    }

    @Override
    public long readLong(Object object, long offset) {
        return UNSAFE.getLong(object, offset);
    }

    @Override
    public void writeLong(long address, long i64) {
        UNSAFE.putLong(address, i64);
    }

    @Override
    public void writeLong(Object object, long offset, long i64) {
        UNSAFE.putLong(object, offset, i64);
    }

    @Override
    public float readFloat(long address) {
        return UNSAFE.getFloat(address);
    }

    @Override
    public float readFloat(Object object, long offset) {
        return UNSAFE.getFloat(object, offset);
    }

    @Override
    public void writeFloat(long address, float f) {
        UNSAFE.putFloat(address, f);
    }

    @Override
    public void writeFloat(Object object, long offset, float f) {
        UNSAFE.putFloat(object, offset, f);
    }

    @Override
    public double readDouble(long address) {
        return UNSAFE.getDouble(address);
    }

    @Override
    public double readDouble(Object object, long offset) {
        return UNSAFE.getDouble(object, offset);
    }

    @Override
    public void writeDouble(long address, double d) {
        UNSAFE.putDouble(address, d);
    }

    @Override
    public void writeDouble(Object object, long offset, double d) {
        UNSAFE.putDouble(object, offset, d);
    }

    @Override
    public byte readVolatileByte(long address) {
        return UNSAFE.getByteVolatile(null, address);
    }

    @Override
    public byte readVolatileByte(Object object, long offset) {
        return UNSAFE.getByteVolatile(object, offset);
    }

    @Override
    public short readVolatileShort(long address) {
        return UNSAFE.getShortVolatile(null, address);
    }

    @Override
    public short readVolatileShort(Object object, long offset) {
        return UNSAFE.getShortVolatile(object, offset);
    }

    @Override
    public int readVolatileInt(long address) {
        return UNSAFE.getIntVolatile(null, address);
    }

    @Override
    public int readVolatileInt(Object object, long offset) {
        return UNSAFE.getIntVolatile(object, offset);
    }

    @Override
    public float readVolatileFloat(long address) {
        return UNSAFE.getFloatVolatile(null, address);
    }

    @Override
    public float readVolatileFloat(Object object, long offset) {
        return UNSAFE.getFloatVolatile(object, offset);
    }

    @Override
    public long readVolatileLong(long address) {
        return UNSAFE.getLongVolatile(null, address);
    }

    @Override
    public long readVolatileLong(Object object, long offset) {
        return UNSAFE.getLongVolatile(object, offset);
    }

    @Override
    public double readVolatileDouble(long address) {
        return UNSAFE.getLongVolatile(null, address);
    }

    @Override
    public double readVolatileDouble(Object object, long offset) {
        return UNSAFE.getDoubleVolatile(object, offset);
    }

    @Override
    public void writeVolatileByte(long address, byte i8) {
        UNSAFE.putByteVolatile(null, address, i8);
    }

    @Override
    public void writeVolatileByte(Object object, long offset, byte i8) {
        UNSAFE.putByteVolatile(object, offset, i8);
    }

    @Override
    public void writeVolatileShort(long address, short i16) {
        UNSAFE.putShortVolatile(null, address, i16);
    }

    @Override
    public void writeVolatileShort(Object object, long offset, short i16) {
        UNSAFE.putShortVolatile(object, offset, i16);
    }

    @Override
    public void writeVolatileInt(long address, int i32) {
        UNSAFE.putIntVolatile(null, address, i32);
    }

    @Override
    public void writeVolatileInt(Object object, long offset, int i32) {
        UNSAFE.putIntVolatile(object, offset, i32);
    }

    @Override
    public void writeVolatileFloat(long address, float f) {
        UNSAFE.putFloatVolatile(null, address, f);
    }

    @Override
    public void writeVolatileFloat(Object object, long offset, float f) {
        UNSAFE.putFloatVolatile(object, offset, f);
    }

    @Override
    public void writeVolatileLong(long address, long i64) {
        UNSAFE.putLongVolatile(null, address, i64);
    }

    @Override
    public void writeVolatileLong(Object object, long offset, long i64) {
        UNSAFE.putLongVolatile(object, offset, i64);
    }

    @Override
    public void writeVolatileDouble(long address, double d) {
        UNSAFE.putDoubleVolatile(null, address, d);
    }

    @Override
    public void writeVolatileDouble(Object object, long offset, double d) {
        UNSAFE.putDoubleVolatile(object, offset, d);
    }

    @Override
    public int addInt(long address, int increment) {
        return UNSAFE.getAndAddInt(null, address, increment) + increment;
    }

    @Override
    public int addInt(Object object, long offset, int increment) {
        return UNSAFE.getAndAddInt(object, offset, increment) + increment;
    }

    @Override
    public long addLong(long address, long increment) {
        return UNSAFE.getAndAddLong(null, address, increment) + increment;
    }

    @Override
    public long addLong(Object object, long offset, long increment) {
        return UNSAFE.getAndAddLong(object, offset, increment) + increment;
    }

    @Override
    public boolean compareAndSwapInt(long address, int expected, int value) {
        return UNSAFE.compareAndSwapInt(null, address, expected, value);
    }

    @Override
    public boolean compareAndSwapInt(Object object, long offset, int expected, int value) {
        return UNSAFE.compareAndSwapInt(object, offset, expected, value);
    }

    @Override
    public boolean compareAndSwapLong(long address, long expected, long value) {
        return UNSAFE.compareAndSwapLong(null, address, expected, value);
    }

    @Override
    public boolean compareAndSwapLong(Object object, long offset, long expected, long value) {
        return UNSAFE.compareAndSwapLong(object, offset, expected, value);
    }

    @Override
    public <E> E allocateInstance(Class<E> clazz) throws InstantiationException {
        return (E) UNSAFE.allocateInstance(clazz);
    }

    @Override
    public long getFieldOffset(Field field) {
        return UNSAFE.objectFieldOffset(field);
    }

    @Override
    public <T> T getObject(Object o, long offset) {
        return (T) UNSAFE.getObject(o, offset);
    }

}

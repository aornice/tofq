package xyz.aornice.tofq.harbour.util;

import java.lang.reflect.Field;

/**
 * Created by drfish on 10/04/2017.
 */
public interface Memory {
    int pageSize();

    void setMemory(long address, long size, byte b);

    void freeMemory(long address, long size);

    long allocate(long capacity) throws IllegalArgumentException, OutOfMemoryError;

    long nativeMemoryUsed();

    <E> E allocateInstance(Class<E> clazz) throws InstantiationException;

    long getFieldOffset(Field field);

    <T> T getObject(Object o, long offset);

    byte readByte(Object object, long offset);

    byte readByte(long address);

    void writeByte(Object object, long offset, byte i8);

    void writeByte(long address, byte i8);

    short readShort(Object object, long offset);

    short readShort(long address);

    void writeShort(Object object, long offset, short i16);

    void writeShort(long address, short i16);

    int readInt(long address);

    int readInt(Object object, long offset);

    void writeInt(long address, int i32);

    void writeInt(Object object, long offset, int i32);

    void writeOrderedInt(long offset, int i32);

    void writeOrderedInt(Object object, long offset, int i32);

    long readLong(long address);

    long readLong(Object object, long offset);

    void writeLong(long address, long i64);

    void writeLong(Object object, long offset, long i64);

    float readFloat(long address);

    float readFloat(Object object, long offset);

    void writeFloat(long address, float f);

    void writeFloat(Object object, long offset, float f);

    double readDouble(long address);

    double readDouble(Object object, long offset);

    void writeDouble(long address, double d);

    void writeDouble(Object object, long offset, double d);

    byte readVolatileByte(long address);

    byte readVolatileByte(Object object, long offset);

    short readVolatileShort(long address);

    short readVolatileShort(Object object, long offset);

    int readVolatileInt(long address);

    int readVolatileInt(Object object, long offset);

    float readVolatileFloat(long address);

    float readVolatileFloat(Object object, long offset);

    long readVolatileLong(long address);

    long readVolatileLong(Object object, long offset);

    double readVolatileDouble(long address);

    double readVolatileDouble(Object object, long offset);

    void writeVolatileByte(long address, byte i8);

    void writeVolatileByte(Object object, long offset, byte i8);

    void writeVolatileShort(long address, short i16);

    void writeVolatileShort(Object object, long offset, short i16);

    void writeVolatileInt(long address, int i32);

    void writeVolatileInt(Object object, long offset, int i32);

    void writeVolatileFloat(long address, float f);

    void writeVolatileFloat(Object object, long offset, float f);

    void writeVolatileLong(long address, long i64);

    void writeVolatileLong(Object object, long offset, long i64);

    void writeVolatileDouble(long address, double d);

    void writeVolatileDouble(Object object, long offset, double d);

    int addInt(long address, int increment);

    int addInt(Object object, long offset, int increment);

    long addLong(long address, long increment);

    long addLong(Object object, long offset, long increment);

    boolean compareAndSwapInt(long address, int expected, int value);

    boolean compareAndSwapInt(Object object, long offset, int expected, int value);

    boolean compareAndSwapLong(long address, long expected, long value);

    boolean compareAndSwapLong(Object object, long offset, long expected, long value);
}

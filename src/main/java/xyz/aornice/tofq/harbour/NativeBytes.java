package xyz.aornice.tofq.harbour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Cleaner;
import xyz.aornice.tofq.ReferenceCount;
import xyz.aornice.tofq.ReferenceCounter;
import xyz.aornice.tofq.harbour.util.Jvm;
import xyz.aornice.tofq.harbour.util.Memory;
import xyz.aornice.tofq.harbour.util.UnsafeMemory;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * Created by drfish on 13/04/2017.
 */
public class NativeBytes implements ReferenceCount {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeBytes.class);
    //    private static final long MEMORY_MAPPED_SIZE = 128 << 10;
    private static final Field BYTE_BUFFER_ADDRESS, BYTE_BUFFER_CAPACITY;

    static {
        Class directByteBuffer = ByteBuffer.allocateDirect(0).getClass();
        BYTE_BUFFER_ADDRESS = Jvm.getField(directByteBuffer, "address");
        BYTE_BUFFER_CAPACITY = Jvm.getField(directByteBuffer, "capacity");
    }

    protected long address;
    protected Memory memory = UnsafeMemory.INSTANCE;
    protected long maximumLimit;
    private Cleaner cleaner;
    private final ReferenceCounter refCounter = new ReferenceCounter(this::doRelease);

    public NativeBytes(long address, long maximumLimit, Runnable deallocator) {
        setAddress(address);
        this.maximumLimit = maximumLimit;
        cleaner = deallocator == null ? null : Cleaner.create(this, deallocator);
    }

    public NativeBytes(long address, long maximumLimit) {
        this(address, maximumLimit, null);
    }

    public void setAddress(long address) {
        if ((address & ~0x3FFF) == 0) {
            throw new AssertionError("Invalid address " + Long.toHexString(address));
        }
        this.address = address;
    }


    public long capacity() {
        return maximumLimit;
    }

    @SuppressWarnings("ignore boundry check")
    private long offset(long offset) {
//        if (offset < 0 || offset > maximumLimit) {
//            throw new IllegalArgumentException("offset exceeds in NativeBytes");
//        }
        return address + offset;
    }

    public boolean compareAndSwapInt(long offset, int expected, int value) {
        return memory.compareAndSwapInt(offset(offset), expected, value);
    }

    public boolean compareAndSwapLong(long offset, long expected, long value) {
        return memory.compareAndSwapLong(offset(offset), expected, value);
    }

    public byte readByte(long offset) {
        return memory.readByte(offset(offset));
    }

    public short readShort(long offset) {
        return memory.readShort(offset(offset));
    }

    public int readInt(long offset) {
        return memory.readInt(offset(offset));
    }

    public long readLong(long offset) {
        return memory.readLong(offset(offset));
    }

    public float readFloat(long offset) {
        return memory.readFloat(offset(offset));
    }

    public double readDouble(long offset) {
        return memory.readDouble(offset(offset));
    }

    public byte readVolatileByte(long offset) {
        return memory.readVolatileByte(offset(offset));
    }

    public short readVolatileShort(long offset) {
        return memory.readVolatileShort(offset(offset));
    }

    public int readVolatileInt(long offset) {
        return memory.readVolatileInt(offset(offset));
    }

    public long readVolatileLong(long offset) {
        return memory.readVolatileLong(offset(offset));
    }

    public NativeBytes writeByte(long offset, byte i8) {
        memory.writeByte(offset(offset), i8);
        return this;
    }

    public NativeBytes writeShort(long offset, short i16) {
        memory.writeShort(offset(offset), i16);
        return this;
    }

    public NativeBytes writeInt(long offset, int i32) {
        memory.writeInt(offset(offset), i32);
        return this;
    }

    public NativeBytes writeLong(long offset, long i64) {
        memory.writeLong(offset(offset), i64);
        return this;
    }

    public NativeBytes writeFloat(long offset, float f) {
        memory.writeFloat(offset(offset), f);
        return this;
    }

    public NativeBytes writeDouble(long offset, double d) {
        memory.writeDouble(offset(offset), d);
        return this;
    }

    public NativeBytes writeVolatileByte(long offset, byte i8) {
        memory.writeVolatileByte(offset(offset), i8);
        return this;
    }

    public NativeBytes writeVolatileShort(long offset, byte i16) {
        memory.writeVolatileShort(offset(offset), i16);
        return this;
    }

    public NativeBytes writeVolatileInt(long offset, int i32) {
        memory.writeVolatileInt(offset(offset), i32);
        return this;
    }

    public NativeBytes writeVolatileLong(long offset, long i64) {
        memory.writeVolatileLong(offset(offset), i64);
        return this;
    }

    public NativeBytes write(long offsetInTarget, byte[] bytes, int offset, int length) {
        memory.copyMemory(bytes, offset, offset(offsetInTarget), length);
        return this;
    }


    private void doRelease() {
        memory = null;
        if (refCounter.getCount() > 0) {
            LOGGER.warn("NativeBytes discarded without releasing");
        }
        if (cleaner != null) {
            cleaner.clean();
        }
    }

    public long refCount() {
        return refCounter.getCount();
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
}

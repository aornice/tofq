package xyz.aornice.tofq.harbour;

import sun.misc.Cleaner;
import xyz.aornice.tofq.ReferenceCounter;
import xyz.aornice.tofq.harbour.util.Jvm;
import xyz.aornice.tofq.harbour.util.Memory;
import xyz.aornice.tofq.harbour.util.UnsafeMemory;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * Created by drfish on 13/04/2017.
 */
public class NativeBytes {
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

    private void doRelease() {
        memory = null;
        if (refCounter.getCount() > 0) {
            System.out.println("NativeBytes discarded without releasing");
        }
        if (cleaner != null) {
            cleaner.clean();
        }
    }

}

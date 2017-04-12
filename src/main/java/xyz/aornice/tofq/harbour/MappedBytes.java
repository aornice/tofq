package xyz.aornice.tofq.harbour;

import xyz.aornice.tofq.ReferenceCount;
import xyz.aornice.tofq.harbour.util.Memory;
import xyz.aornice.tofq.harbour.util.StringUtils;
import xyz.aornice.tofq.harbour.util.UnsafeMemory;

/**
 * Created by drfish on 12/04/2017.
 */
public class MappedBytes {
    private final long start;
    private final long end;
    private long writePosition;
    private long readPosition;

    protected MappedBytes(ReferenceCount owner, long start, long address, long capacity, long safeCapacity) {
        this.start = start;
        this.end = start + safeCapacity;
    }

    public MappedBytes append8bit(String string, int start, int end) {
        char[] chars = StringUtils.extractChars(string);
        Memory memory = UnsafeMemory.INSTANCE;
        int i = 0;
        long address = writePosition;
        int length = end - start;
        for (; i < length - 3; i++) {
            int c0 = chars[i + start] & 0xff;
            int c1 = chars[i + start + 1] & 0xff;
            int c2 = chars[i + start + 2] & 0xff;
            int c3 = chars[i + start + 3] & 0xff;
            memory.writeInt(address, (c3 << 24) | (c2 << 16) | (c1 << 8) | c0);
            address += 4;
        }
        for (; i < length; i++) {
            char c = chars[i + start];
            memory.writeByte(address++, (byte) c);
        }
        writePosition += length;
        return this;
    }
}

package xyz.aornice.tofq.harbour;

import xyz.aornice.tofq.ReferenceCount;
import xyz.aornice.tofq.harbour.util.Memory;
import xyz.aornice.tofq.harbour.util.OS;
import xyz.aornice.tofq.harbour.util.StringUtils;
import xyz.aornice.tofq.harbour.util.UnsafeMemory;

/**
 * Created by drfish on 12/04/2017.
 */
public class MappedBytes extends NativeBytes {
    private final long start;
    private final long end;
    //    private final String type;
//    private final long count;
//    private final MappedFile mappedFile;


//    protected MappedBytes(long start, long end, String type, long count, MappedFile mappedFile) {
////        this.start = start;
////        this.end = end;
////        this.type = type;
////        this.count = count;
//        this.mappedFile = mappedFile;
//    }

    protected MappedBytes(ReferenceCount owner, long start, long address, long capacity) {
        super(address, address + capacity, new OS.Unmapper(address, capacity, owner));
        this.start = address;
        this.end = address + capacity;
    }

//    protected MappedBytes(MappedFile mappedFile) {
//        this.mappedFile = mappedFile;
//    }
//
//    public static MappedBytes getMappedBytes(String fileName, long blockSize, long overlapSize) throws FileNotFoundException {
//        MappedFile rw = MappedFile.getMappedFile(fileName, blockSize, overlapSize);
//        return new MappedBytes(rw);
//    }

    public MappedBytes writeBits(String string, long start, long end) {
        return write8bit(string, start, end);
    }

    private MappedBytes write8bit(String string, long start, long end) {
        char[] chars = StringUtils.extractChars(string);
        Memory memory = UnsafeMemory.INSTANCE;
        int i = 0;
        long address = this.address + start;
        long length = end - start;
        for (; i < length - 3; i += 4) {
            int c0 = chars[i] & 0xff;
            int c1 = chars[i + 1] & 0xff;
            int c2 = chars[i + 2] & 0xff;
            int c3 = chars[i + 3] & 0xff;
            memory.writeInt(address, (c3 << 24) | (c2 << 16) | (c1 << 8) | c0);
            address += 4;
        }
        for (; i < length; i++) {
            char c = chars[i];
            memory.writeByte(address++, (byte) c);
        }
        return this;
    }

    public MappedBytes readBits(StringBuilder sb, long start, long end) {
        sb.setLength(0);
        Memory memory = UnsafeMemory.INSTANCE;
        for (long offset = start; offset < end; offset++) {
            int c = memory.readByte(address + offset);
            sb.append((char) c);
        }
        return this;
    }
}

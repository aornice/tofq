package xyz.aornice.tofq.harbour.util;

import sun.nio.ch.FileChannelImpl;
import xyz.aornice.tofq.ReferenceCount;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

/**
 * Created by drfish on 10/04/2017.
 */
public enum OS {
    ;
    private static final Memory MEMORY = UnsafeMemory.INSTANCE;
    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int MAP_PV = 2;
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_LINUX = OS.startsWith("linux");
    private static final boolean IS_MAC = OS.contains("mac");
    private static final boolean IS_WIN = OS.startsWith("win");
    private static final int MAP_ALIGNMENT = isWindows() ? 64 << 10 : pageSize();

    public static int pageSize() {
        return MEMORY.pageSize();
    }

    /**
     * Map a file into memory.
     *
     * @param fileChannel file needs to map
     * @param mode        access mode for the file
     * @param start       offset in the file to start
     * @param size        the size of the mapped part
     * @return address of the mapped memory
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static long map(FileChannel fileChannel, FileChannel.MapMode mode, long start, long size) throws InvocationTargetException, IllegalAccessException {
        return map0(fileChannel, imodeFrom(mode), mapAlign(start), pageAlign(size));
    }

    static int imodeFrom(FileChannel.MapMode mode) {
        int imode = -1;
        if (mode == FileChannel.MapMode.READ_ONLY) {
            imode = MAP_RO;
        } else if (mode == FileChannel.MapMode.READ_WRITE) {
            imode = MAP_RW;
        } else if (mode == FileChannel.MapMode.PRIVATE) {
            imode = MAP_PV;
        }
        assert imode >= 0;
        return imode;
    }


    /**
     * Align an offset of a memory mapping in file based on OS.
     *
     * @param offset needs align
     * @return offset aligned
     */
    public static long mapAlign(long offset) {
        int chunkMultiple = MAP_ALIGNMENT;
        return (offset + chunkMultiple - 1) / chunkMultiple * chunkMultiple;
    }

    /**
     * Align size to multi pageSize.
     *
     * @param size needs align
     * @return size aligned
     */
    public static long pageAlign(long size) {
        long mask = pageSize() - 1;
        return (size + mask) & ~mask;
    }

    static long map0(FileChannel fileChannel, int imode, long start, long size) throws InvocationTargetException, IllegalAccessException {
        Method map0 = Jvm.getMethod(fileChannel.getClass(), "map0", int.class, long.class, long.class);
        return (Long) map0.invoke(fileChannel, imode, start, size);
    }


    public static boolean isWindows() {
        return IS_WIN;
    }

    public static boolean isMacOSX() {
        return IS_MAC;
    }

    public static boolean isLinux() {
        return IS_LINUX;
    }


    public static void unmap(long address, long size) throws IOException {
        Method unmap0 = Jvm.getMethod(FileChannelImpl.class, "unmap0", long.class, long.class);
        try {
            unmap0.invoke(null, address, pageAlign(size));
        } catch (Exception e) {
            throw new IOException(e.getCause());
        }
    }

    public static class Unmapper implements Runnable {
        private final long size;

        private final ReferenceCount owner;
        private volatile long address;

        public Unmapper(long address, long size, ReferenceCount owner) throws IllegalStateException {
            owner.reserve();
            this.owner = owner;
            assert (address != 0);
            this.address = address;
            this.size = size;
        }

        public void run() {
            if (address == 0)
                return;

            try {
                unmap(address, size);
                address = 0;

                owner.release();
            } catch (IOException | IllegalStateException e) {
                //TODO Log system
            }
        }
    }
}

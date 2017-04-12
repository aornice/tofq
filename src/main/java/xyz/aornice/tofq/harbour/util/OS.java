package xyz.aornice.tofq.harbour.util;

/**
 * Created by drfish on 10/04/2017.
 */
public enum OS {
    ;
    private static final Memory MEMORY = UnsafeMemory.INSTANCE;

    public static long pagesize() {
        return MEMORY.pageSize();
    }
}

package xyz.aornice.tofq.harbour.util;

import java.lang.reflect.Field;

/**
 * Created by drfish on 12/04/2017.
 */
public enum StringUtils {
    ;
    private static final Field VALUE_FIELD;
    private static final long VALUE_OFFSET;

    static {
        VALUE_FIELD = Jvm.getField(String.class, "value");
        VALUE_OFFSET = UnsafeMemory.INSTANCE.getFieldOffset(VALUE_FIELD);
    }

    public static char[] extractChars(String s) {
        return UnsafeMemory.INSTANCE.getObject(s, VALUE_OFFSET);
    }
}

package xyz.aornice.tofq.harbour.util;

import java.lang.reflect.Field;

/**
 * Created by drfish on 10/04/2017.
 */
public enum Jvm {
    ;

    public static Field getField(Class clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass != null) {
                try {
                    return getField(superClass, name);
                } catch (Exception ignore) {
                }
            }
            throw new AssertionError(e);
        }
    }
}

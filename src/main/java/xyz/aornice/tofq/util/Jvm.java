package xyz.aornice.tofq.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

    public static Method getMethod(Class clazz, String name, Class... params) {
        try {
            Method method = clazz.getDeclaredMethod(name, params);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass != null) {
                try {
                    return getMethod(clazz, name, params);
                } catch (Exception ignore) {
                }
            }
            throw new AssertionError(e);
        }
    }
}

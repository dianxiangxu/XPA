package org.seal.policyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {
    private static Log logger = LogFactory.getLog(ReflectionUtils.class);

    public static Object getField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException
                | NoSuchFieldException | SecurityException e) {
            logger.error(e);
            throw new RuntimeException("reflection failed");
        }
    }

    public static Object invokeMethod(Object obj, String methodName,
                                      Object[] args, Class<?>[] parameterTypes) {
        Method method = null;
        Class<?> clazz = obj.getClass();
        // Class.getDeclaredMethod() only look for the method in the methods that is declared in 
        // that class; if the method is inherited from a super class and is not overridden by this
        // class, it will throw a NoSuchMethodException. So we need to go up the class hierarchy
        // until we find the method.
        while (clazz != Object.class) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                break;
            } catch (NoSuchMethodException ex) {
                clazz = clazz.getSuperclass();
            }
        }
        try {
            assert method != null;
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (SecurityException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            logger.error(e);
            e.printStackTrace();
            throw new RuntimeException("reflection failed");
        }

    }
}

package org.homunculusframework.lang;

import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * Provides a static and non-defensive copy cache for methods
 */
class Methods {
    private final static IdentityHashMap<Class, List<Method>> allDeclaredMethods = new IdentityHashMap<>();
    private final static IdentityHashMap<Method, Annotation[][]> parameterAnnotations = new IdentityHashMap<>();
    private final static IdentityHashMap<Method, Class[]> parameterTypes = new IdentityHashMap<>();

    private Methods() {

    }


    /**
     * Returns a non-defensive copy from cache. Returns all declared methods recursivly. Super methods are last in list, otherwise
     * order is in reflection order (not in any particular order)
     */
    public static List<Method> getMethods(Class clazz) {
        synchronized (allDeclaredMethods) {
            List<Method> res = allDeclaredMethods.get(clazz);
            if (res == null) {
                res = new ArrayList<>();
                Class root = clazz;
                while (root != null) {
                    try {
                        for (Method m : root.getDeclaredMethods()) {
                            res.add(m);
                        }
                    } catch (NoClassDefFoundError cfe) {
                        LoggerFactory.getLogger(Methods.class).warn("failed to acquire methods from '{}' - {}({})", root, cfe.getClass(), cfe.getMessage());
                    }
                    root = root.getSuperclass();
                }
                allDeclaredMethods.put(clazz, res);
            }
            return res;
        }
    }

    /**
     * Returns a non-defensive copy from cache.
     */
    public static Annotation[][] getParameterAnnotations(Method method) {
        synchronized (parameterAnnotations) {
            Annotation[][] res = parameterAnnotations.get(method);
            if (res == null) {
                res = method.getParameterAnnotations();
                parameterAnnotations.put(method, res);
            }
            return res;
        }
    }

    /**
     * Returns a non-defensive copy from cache.
     */
    public static Class[] getParameterTypes(Method method) {
        synchronized (parameterTypes) {
            Class[] res = parameterTypes.get(method);
            if (res == null) {
                res = method.getParameterTypes();
                parameterTypes.put(method, res);
            }
            return res;
        }
    }


}

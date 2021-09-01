/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.homunculusframework.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A helper class of reflection things which may be useful if they would have been in the default sdk.
 * Improves performance on various Android versions, which provide inconsistent performance across still alive devices.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Reflection {

    private Reflection() {

    }

    /**
     * See {@link Methods#getParameterTypes(Method)}
     */
    public static Class[] getParameterTypes(Method method) {
        return Methods.getParameterTypes(method);
    }

    /**
     * See {@link Methods#getParameterAnnotations(Method)}
     */
    public static Annotation[][] getParameterAnnotations(Method method) {
        return Methods.getParameterAnnotations(method);
    }

    /**
     * See {@link Methods#getMethods(Class)}
     */
    public static List<Method> getMethods(Class<?> clazz) {
        return Methods.getMethods(clazz);
    }

    /**
     * Returns the method from the class or any super class which matches the given signature
     *
     * @param clazz
     * @return
     */
    @Nullable
    public static Method getMethod(Class<?> clazz, String name, Class[] parameterTypes) {
        for (Method m : getMethods(clazz)) {
            if (m.getName().equals(name)) {
                Class[] paramA = m.getParameterTypes();
                if (Arrays.equals(parameterTypes, paramA)) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * See {@link Clazz#getFields(Class)}
     */
    public static List<Field> getFields(Class<?> clazz) {
        return Clazz.getFields(clazz);
    }

    /**
     * See {@link Clazz#getFields(Class)}
     */
    public static java.util.Map<String, Field> getFieldsMap(Class<?> clazz) {
        return Clazz.getFieldsMap(clazz);
    }

    @Nullable
    public static Field getField(Class<?> clazz, String name) {
        return getFieldsMap(clazz).get(name);
    }

    /**
     * See {@link Clazz#getName(Class)}
     */
    public static String getName(Class<?> clazz) {
        return Clazz.getName(clazz);
    }

    /**
     * Performs a duck cast, either by applying just a cast or by performing an expensive (copy) type conversion.
     * Is able to transform various things like
     * <ul>
     * <li>In general if out is assignable to the type of in, just a java cast is performed</li>
     * <li>Primitives are always casted to their Autoboxing types (this how Java works)</li>
     * <li>Casting a null/inconvertible to a primitive will be treated as the java default value (0 for number, false for boolean)</li>
     * <li>Casting a null to string, will be null</li>
     * <li>Casting any primitive to a string will use their default corresponding toString conversion</li>
     * <li>Casting any string which has been previously generated from another primitive to their new primitive will use the appropriate default parse method</li>
     * <li>In general, if a cast was not possible, null is returned</li>
     * </ul>
     */
    @Nullable
    public static <In, Out> Out castDuck(@Nullable In in, Class<Out> out) {

        if (in != null && out.isAssignableFrom(in.getClass())) {
            return (Out) in;
        }


        if (out.isPrimitive()) {
            if (in == null) {
                return getDefaultPrimitiveValue(out);
            }
            Class cin = in.getClass();
            if (in instanceof Number) {
                return primitiveCastDuck((Number) in, out);
            }
            if (in instanceof Boolean && out == boolean.class || out == Boolean.class) {
                return (Out) in;
            }
            if (in instanceof String) {
                String sin = (String) in;

                if (out == boolean.class) {
                    boolean isTrue = sin.equalsIgnoreCase("true");
                    boolean isFalse = sin.equalsIgnoreCase("false");
                    if (isTrue) {
                        return (Out) Boolean.valueOf(true);
                    }
                    if (isFalse) {
                        return (Out) Boolean.valueOf(false);
                    }
                }

                try {
                    long val = Long.parseLong(sin);
                    return primitiveCastDuck(val, out);
                } catch (NumberFormatException e) {
                    //intentionally ignored
                }

                try {
                    double val = Double.parseDouble(sin);
                    return primitiveCastDuck(val, out);
                } catch (NumberFormatException e) {
                    //intentionally ignored
                }

            }

        }
        return null;

    }


    @Nullable
    private static <Out> Out primitiveCastDuck(@Nullable Number nin, Class<Out> out) {
        if (nin == null) {
            return null;
        }
        if (out == int.class) {
            return (Out) Integer.valueOf(nin.intValue());
        }
        if (out == short.class) {
            return (Out) Short.valueOf(nin.shortValue());
        }
        if (out == byte.class) {
            return (Out) Byte.valueOf(nin.byteValue());
        }
        if (out == long.class) {
            return (Out) Long.valueOf(nin.longValue());
        }
        if (out == int.class) {
            return (Out) Integer.valueOf(nin.intValue());
        }
        if (out == float.class) {
            return (Out) Float.valueOf(nin.floatValue());
        }
        if (out == double.class) {
            return (Out) Double.valueOf(nin.doubleValue());
        }
        if (out == char.class) {
            return (Out) Character.valueOf((char) nin.intValue());
        }
        if (out == boolean.class) {
            return (Out) Boolean.valueOf(nin.intValue() != 0);
        }
        if (out == String.class) {
            return (Out) nin.toString();
        }
        return null;
    }

    /**
     * Returns a null for non-primitive types, otherwise returns the default primitive value according to the java specification.
     */
    private static <T> T getDefaultPrimitiveValue(Class<T> type) {
        if (type.isPrimitive()) {
            if (type == int.class) {
                return (T) Integer.valueOf(0);
            }
            if (type == float.class) {
                return (T) Float.valueOf(0);
            }
            if (type == long.class) {
                return (T) Long.valueOf(0);
            }
            if (type == double.class) {
                return (T) Double.valueOf(0);
            }
            if (type == boolean.class) {
                return (T) Boolean.valueOf(false);
            }
            if (type == char.class) {
                return (T) Character.valueOf((char) 0);
            }
            if (type == byte.class) {
                return (T) Byte.valueOf((byte) 0);
            }

            if (type == short.class) {
                return (T) Short.valueOf((short) 0);
            }
        }
        return null;
    }
}

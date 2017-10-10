package org.homunculusframework.lang;

import javax.annotation.Nullable;

/**
 * A helper class of reflection things which may be useful if they would have been in the default sdk.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Reflection {

    private Reflection() {

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

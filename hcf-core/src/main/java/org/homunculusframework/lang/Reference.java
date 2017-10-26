package org.homunculusframework.lang;

public interface Reference<T> {

    /**
     * Returns the value
     *
     * @return the value
     */
    T get();

    /**
     * Sets the value
     *
     * @param value the value
     */
    void set(T value);
}

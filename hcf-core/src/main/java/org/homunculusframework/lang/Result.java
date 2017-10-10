package org.homunculusframework.lang;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple tagged result.
 *
 * @param <T>
 */
public final class Result<T> {
    @Nullable
    private T value;
    @Nullable
    private Throwable throwable;

    @Nullable
    private Result<?> parent;

    private final Map<String, Object> tags = new TreeMap<>();

    public static <T> Result<T> create() {
        return new Result<>();
    }

    @Nullable
    public T get() {
        return value;
    }

    public void set(@Nullable T value) {
        this.value = value;
    }

    public void setThrowable(@Nullable Throwable throwable) {
        this.throwable = throwable;
    }

    @Nullable
    public Throwable getThrowable() {
        return throwable;
    }

    public void setParent(@Nullable Result<?> parent) {
        this.parent = parent;
    }

    @Nullable
    public Result<?> getParent() {
        return parent;
    }

    public Result<T> put(String key, Object value) {
        tags.put(key, value);
        return this;
    }

}

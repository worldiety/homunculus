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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple tagged result. Kind of a tuple type.
 *
 * @author Torben Schinke
 * @since 1.0
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

    public static <T> Result<T> create(@Nullable T value) {
        Result<T> r = new Result<>();
        r.set(value);
        return r;
    }

    public static <T> Result<T> nullValue(Result<?> other) {
        Result<T> res = Result.create(null);
        res.throwable = other.throwable;
        res.parent = other.parent;
        res.tags.putAll(other.tags);
        return res;
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

    public Result<T> putTag(String key, Object value) {
        tags.put(key, value);
        return this;
    }

}

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

import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * A simple tagged result. Kind of a tuple type.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class Result<T> extends Ref<T> {

    /**
     * A conventional tag to indicate a cancelled task
     */
    public final static String TAG_CANCELLED = "task.cancelled";

    /**
     * A conventional tag to indicate an outdated task result. Definition of outdated:
     * <p>
     * Referring to a request on an instance of a task provider, invoking an asynchronous
     * method multiple times before any result is available, the order of execution is not defined. Therefore the
     * task provider must ensure that all results but the last one are tagged as outdated.
     */
    public final static String TAG_OUTDATED = "task.outdated";

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

    /**
     * A copy constructor which nulls out the value, so that any cast is allowed.
     *
     * @param other the other result
     * @param <T>   target type
     * @return a new result instance with throwable, parent and tags from other but a null value
     */
    public static <T> Result<T> nullValue(Result<?> other) {
        Result<T> res = Result.create(null);
        res.throwable = other.throwable;
        res.parent = other.parent;
        res.tags.putAll(other.tags);
        return res;
    }


    public Result<T> setThrowable(@Nullable Throwable throwable) {
        this.throwable = throwable;
        return this;
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

    public boolean hasTag(String key) {
        return tags.containsKey(key);
    }

    /**
     * Checks if the value exists (is not null)
     *
     * @return true if {@link #get()} will not return true
     */
    public boolean exists() {
        return get() != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("value").append("=").append(get()).append("\n");
        for (Entry<String, Object> entry : tags.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        if (throwable != null) {
            StringWriter writer = new StringWriter();
            PrintWriter pwriter = new PrintWriter(writer);
            throwable.printStackTrace(pwriter);
            pwriter.flush();
            sb.append(writer.toString());
        }
        return sb.toString();
    }

    /**
     * Prints into the log, if the value is null or a throwable is set
     */
    public Result<T> log() {
        if (get() == null || throwable != null) {
            LoggerFactory.getLogger(getClass()).error(toString());
        }
        return this;
    }

    /**
     * Returns true if the result has been cancelled or interrupted. However a result may still be present.
     * See also {@link #TAG_CANCELLED}
     */
    public boolean isCancelled() {
        return hasTag(TAG_CANCELLED);
    }

    /**
     * Returns true if the result has been outdated. See {@link #TAG_OUTDATED}
     *
     * @return true if outdated
     */
    public boolean isOutdated() {
        return hasTag(TAG_OUTDATED);
    }

}

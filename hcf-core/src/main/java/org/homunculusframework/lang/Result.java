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
import java.util.*;
import java.util.Map.Entry;

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

    /**
     * A conventional tag to provide a message
     */
    public final static String TAG_MESSAGE = "message";


    @Nullable
    private Throwable throwable;

    @Nullable
    private Result<?> parent;

    private final Map<String, Object> tags = new TreeMap<>();

    private List<Result<?>> suppressed = Collections.emptyList();

    /**
     * Creates a new null value result with the according throwable set. Tries to determine some default tags, from the given exception for
     * easier inspection.
     *
     * @param t   the throwable
     * @param <T> the type
     * @return a new instance
     */
    public static <T> Result<T> auto(@Nullable Throwable t) {
        //TODO the current implementation does not provide default tags
        Result<T> res = Result.create(null);
        if (t != null) {
            res.put(TAG_MESSAGE, t.getMessage());
            res.put(t.getClass().getName());
        }
        return res;
    }

    /**
     * Creates an empty result instance, with a null value
     *
     * @param <T> the type
     * @return a new instance
     */
    public static <T> Result<T> create() {
        return new Result<>();
    }

    /**
     * Creates a new instance of a result with the given value
     *
     * @param value the value
     * @param <T>   the type
     * @return a new instance
     */
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


    /**
     * Sets a throwable into this result. It is still valid to provide also a result.
     *
     * @param throwable the throwable
     * @return this
     */
    public Result<T> setThrowable(@Nullable Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    /**
     * Returns an attached throwable
     *
     * @return the throwable, or null
     */
    @Nullable
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Sets the parent result
     *
     * @param parent the parent
     */
    public void setParent(@Nullable Result<?> parent) {
        this.parent = parent;
    }

    /**
     * Returns the parent result, if any
     *
     * @return the parent, or null if not defined
     */
    @Nullable
    public Result<?> getParent() {
        return parent;
    }

    /**
     * Puts a tag into this result (not any parent) and replaces any existing tag/value combination.
     *
     * @param tag   the tag
     * @param value the value
     * @return this result
     */
    public Result<T> put(String tag, @Nullable Object value) {
        tags.put(tag, value);
        return this;
    }

    /**
     * Same as {@link #put(String, Object)} with a null value
     *
     * @param tag the tag
     * @return this result
     */
    public Result<T> put(String tag) {
        return put(tag, null);
    }

    /**
     * Checks if this result or any parent has the requested key.
     *
     * @param key the tag to search
     * @return true if any this result or any parent has such
     */
    public boolean containsKey(String key) {
        boolean has = tags.containsKey(key);
        Result p = parent;
        if (!has && p != null) {
            return p.containsKey(key);
        }
        return has;
    }

    /**
     * See {@link #containsKey(String)}
     *
     * @param tag the tag to search
     * @return true if contained
     */
    public boolean has(String tag) {
        return containsKey(tag);
    }

    /**
     * Recursively gets the value for the given tag
     *
     * @param tag the tag (key)
     * @return the value
     */
    @Nullable
    public Object get(String tag) {
        boolean has = tags.containsKey(tag);
        if (has) {
            return tags.get(tag);
        }
        Result p = parent;
        if (p != null) {
            return p.get(tag);
        }
        return null;
    }

    /**
     * Recursively gets the value for the given tag and performs a safe typecast
     *
     * @param tag the tag (key)
     * @return the casted value or null
     */
    @Nullable
    public <T> T get(String tag, Class<T> type) {
        boolean has = tags.containsKey(tag);
        if (has) {
            Object obj = tags.get(tag);
            if (obj != null && type.isAssignableFrom(obj.getClass())) {
                return (T) obj;
            }
            return null;
        }
        Result p = parent;
        if (p != null) {
            return (T) p.get(tag, type);
        }
        return null;
    }

    /**
     * Returns the backing map for the tags, which are modified and inspected by {@link #put(String, Object)} {@link #get(String)}.
     * It does not contains the parent tags.
     *
     * @return the actual tags of this result
     */
    public Map<String, Object> getTags() {
        return tags;
    }


    /**
     * Checks if the value exists (is not null)
     *
     * @return true if {@link #get()} will not return null
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
        return containsKey(TAG_CANCELLED);
    }

    /**
     * Returns true if the result has been outdated. See {@link #TAG_OUTDATED}
     *
     * @return true if outdated
     */
    public boolean isOutdated() {
        return has(TAG_OUTDATED);
    }


    /**
     * Returns other (intermediate) results which have been suppressed but are still available for inspection or use.
     */
    public List<Result<?>> getSuppressed() {
        return suppressed;
    }

    /**
     * Adds another suppressed result
     *
     * @param r the result
     */
    public void addSuppressed(Result<?> r) {
        if (r == null) {
            return;
        }
        synchronized (this) {
            if (r == null) {
                return;
            }
            if (suppressed.isEmpty()) {
                suppressed = new ArrayList<>(1);
            }
            suppressed.add(r);
        }
    }


    /**
     * See also {@link #TAG_MESSAGE}
     *
     * @return the message if such tag is defined (also in any parent)
     */
    @Nullable
    public String getMessage() {
        return get(TAG_MESSAGE, String.class);
    }

    /**
     * Sets the {@link #TAG_MESSAGE} tag
     *
     * @param msg the message
     * @return this
     */
    public Result<T> setMessage(@Nullable String msg) {
        put(TAG_MESSAGE, msg);
        return this;
    }
}

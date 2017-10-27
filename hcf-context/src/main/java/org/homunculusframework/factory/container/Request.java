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
package org.homunculusframework.factory.container;

import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.component.DefaultFactory;
import org.homunculusframework.factory.flavor.hcf.Persistent;
import org.homunculusframework.factory.flavor.hcf.Widget;
import org.homunculusframework.lang.Function;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Result;
import org.homunculusframework.navigation.DefaultNavigation;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Represents the request to a resource. Usually this should refer to an
 * annotated backend controller containing a method annotated with a request mapping
 * but it may also refer to another {@link Widget} (or UIS) directly.
 * Even platform specific resource may be possible and depends on the actual configuration. At the end
 * this mechanism should provide as much freedom as possible.
 * <p>
 * When calling a controller (or when designing a controller) think about the following:
 * <ul>
 * <li>A controller may not run in the same process</li>
 * <li>A controller may not even run on the same machine</li>
 * <li>A controller may not have access to the local file system</li>
 * <li>A controller never has access to the UI</li>
 * <li>A controller should be cross-platform (e.g. shareable between Java SE and Android)</li>
 * <li>A call to a controller may always cause latent I/O (e.g. disk or network)</li>
 * </ul>
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class Request implements org.homunculusframework.lang.Map<String, Object> {
    private final String mapping;
    private final Map<String, Object> requestParams;

    public Request(String mapping) {
        if (mapping.length() == 0) {
            throw new Panic("empty request is not defined");
        }
        if (mapping.charAt(0) != '/' || mapping.charAt(mapping.length() - 1) != '/') {
            StringBuilder normalized = new StringBuilder();
            if (mapping.charAt(0) != '/') {
                normalized.append('/');
            }
            normalized.append(mapping);
            if (mapping.charAt(mapping.length() - 1) != '/') {
                normalized.append('/');
            }
            this.mapping = normalized.toString();
        } else {
            this.mapping = mapping;
        }
        this.requestParams = new TreeMap<>();
    }


    /**
     * Returns the requested controller mapping in various stereotypes.
     */
    public String getMapping() {
        return mapping;
    }

    /**
     * Adds a key and a value. The inserted object MUST be always a defensive copy (or immutable) and SHOULD be
     * serializable (see also {@link Persistent}). Also the amount
     * of occupied memory should be as small as possible, because depending on the {@link Navigation} this
     * can cause a permanent memory leak.
     * <p>
     * Always treat a request as it would issue a call to a remote host and takes
     * a long time, causing I/O on disk or network.
     * <p>
     * Good candidates for parameters:
     * <ul>
     * <li>Primitives like int, String, boolean and float</li>
     * <li>Small lists of primitives</li>
     * <li>Small objects containing only primitives</li>
     * <li>Just stupid serialized data, as you would send in a REST/SOAP/HTTP request</li>
     * </ul>
     * <p>
     * Bad candidates for parameters*:
     * <ul>
     * <li>Large lists of no matter what (instead think about sending a query or token representing the result set)</li>
     * <li>Observer, Listener, Callbacks</li>
     * <li>Controllers or POJOs containing and providing logic or behavior</li>
     * <li>byte arrays</li>
     * <li>Bitmaps</li>
     * <p>
     * * there is no security net which protects you from doing this, but please just don't do that
     * </ul>
     */
    @Override
    public Request put(String key, Object value) {
        requestParams.put(key, value);
        return this;
    }

    @Override
    public boolean has(String s) {
        return requestParams.containsKey(s);
    }

    /**
     * Removes a key and it's according key.
     *
     * @param key the key
     * @return the request
     */
    @Override
    public Request remove(String key) {
        requestParams.remove(key);
        return this;
    }

    /**
     * Returns the value for the given key. See also {@link #get(String, Class)}
     *
     * @param key the key
     * @return the value or null
     */
    @Nullable
    public Object get(String key) {
        return requestParams.get(key);
    }

    /**
     * Returns the value for the given key if assignable from type
     *
     * @param key the key
     * @return the value or null it does not exist or is not assignable
     */
    @Nullable
    public <T> T get(String key, Class<T> type) {
        Object obj = requestParams.get(key);
        if (obj != null && type.isAssignableFrom(obj.getClass())) {
            return (T) obj;
        }
        return null;
    }

    /**
     * Walks over the contained parameters until the closure returns false.
     */
    @Override
    public Request forEachEntry(Function<Entry<String, Object>, Boolean> closure) {
        for (Entry<String, Object> entry : requestParams.entrySet()) {
            if (!closure.apply(entry)) {
                return this;
            }
        }
        return this;
    }

    /**
     * Executes this request using the given scope:
     * <ul>
     * <li>Uses the {@link Container#NAME_CONTAINER} to lookup an instance of {@link Container}</li>
     * <li>If no appropriate executor has been found, executes synchronous and inline (tbd) and should be avoided in general</li>
     * <li>By definition never throws a panic due to a configuration failure.</li>
     * </ul>
     */
    public Task<Result<Object>> execute(Scope scope) {
        Container container = scope.resolve(Container.NAME_CONTAINER, Container.class);
        if (container == null) {
            LoggerFactory.getLogger(getClass()).error("execution not possible: {} missing ({})", Container.class, mapping);
            SettableTask<Result<Object>> task = SettableTask.create(scope, mapping);
            Result<Object> res = Result.create();
            res.put("error", "missing container");
            task.set(res);
            return task;
        } else {
            SettableTask<Result<Object>> task = SettableTask.create(scope, mapping);
            Result<Object> res = Result.create();
            Handler backgroundThread = scope.resolve(Container.NAME_REQUEST_HANDLER, Handler.class);
            StackTraceElement[] callstack = DefaultFactory.getCallStack(3);
            if (true) {
                StackTraceElement elem = callstack[0];
                String cname = elem.getClassName();
                String method = elem.getMethodName();
                String fname = elem.getFileName();
                int no = elem.getLineNumber();
                LoggerFactory.getLogger(getClass()).info("'{}' -> {}.{}({}:{})", getMapping(), cname, method, fname, no);
            }
            Runnable job = () -> {
                try {
                    Object result;
                    switch (container.getRequestType(this)) {
                        case CONTROLLER_ENDPOINT:
                            result = container.invoke(scope, this);
                            res.set(result);
                            task.set(res);
                            break;
                        case WIDGET:
                            Scope widgetScope = DefaultNavigation.createChild(scope, this);
                            container.prepareScope(widgetScope);
                            Task<Component<?>> widgetTask = container.createWidget(widgetScope, this.getMapping());
                            widgetTask.whenDone(component -> {
                                for (Throwable t : component.getFailures()) {
                                    LoggerFactory.getLogger(getClass()).error("failed to create {}", this.getMapping(), t);
                                }
                                res.set(component);
                                task.set(res);
                            });
                            break;
                        case UNDEFINED:
                            RuntimeException e = new RuntimeException("the mapping '" + this.getMapping() + "' is not configured.");
                            e.setStackTrace(callstack);
                            throw e;
                        default:
                            throw new Panic();
                    }

                } catch (Exception e) {
                    LoggerFactory.getLogger(getClass()).error("failed to execute Request '{}':", this.getMapping(), e);
                    res.setThrowable(e);
                    task.set(res);
                }
            };
            if (backgroundThread == null) {
                LoggerFactory.getLogger(getClass()).warn("no background thread defined, executing Request '{}' inline", this.getMapping());
                job.run();
            } else {
                backgroundThread.post(job);
            }
            return task;
        }
    }

    /**
     * Puts all key/value pairs from the given request into this request.
     *
     * @param other the other request
     */
    @Override
    public Request putAll(org.homunculusframework.lang.Map<String, Object> other) {
        other.forEachEntry(entry -> {
            requestParams.put(entry.getKey(), entry.getValue());
            return true;
        });
        return this;
    }

}

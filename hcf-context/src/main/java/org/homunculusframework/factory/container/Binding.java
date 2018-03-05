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
import org.homunculusframework.lang.Function;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Result;
import org.homunculusframework.navigation.DefaultNavigation;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

/**
 * Represents a decoupled binding to a resource. This shall fulfill the following
 * requirements:
 * <ul>
 * <li>A binding is always executed asynchronously</li>
 * <li>A binding can refer to injectable fields and/or a constructor of an object</li>
 * <li>A binding can refer to a method</li>
 * <li>A binding is not thread safe</li>
 * </ul>
 *
 * @author Torben Schinke
 * @since 1.0
 */
public abstract class Binding<Response> implements Serializable {
    /**
     * Used to only init class members once, e.g. reflection fields which only needs to be grabbed once per class.
     * For performance reasons we never want that blocking operations (talking about framedrops in the UI) happens
     * in the constructor.
     */
    private static transient volatile boolean initialized;

    /**
     * The scope is kept as a member and only valid after {@link #onBind(Scope)}.
     * The scope to resolve everything else. This already contains all values from this binding.
     */
    @Nullable
    private transient Scope scope;

    /**
     * This constructor is intentionally package private to force that every subtype is either a {@link MethodBinding} or a {@link ObjectBinding}
     */
    Binding() {
    }


    /**
     * Called only once to init this class. However it is guaranteed to called lazily by {@link #execute(Scope)}
     */
    protected abstract void initStatic();

    /**
     * Typically executed from {@link Container#NAME_REQUEST_HANDLER}
     *
     * @return an instance of whatever the binding refers to
     * @throws Exception any exception
     */
    @Nullable
    protected abstract Response onExecute() throws Exception;

    /**
     * Puts all key/value pairs of this binding into the scope. Later {@link #onExecute()} is called with that scope.
     *
     * @param dst the target scope to bind the variables to
     */
    protected abstract void onBind(Scope dst);

    /**
     * Creates a child scope within the given parent by injecting the binding parameters.
     *
     * @param parent the parent
     * @return the child scope
     */
    private Scope createSubScope(Scope parent) {
        Scope scope = new Scope(toString(), parent);
        onBind(scope);
        this.scope = scope;
        return scope;
    }

    protected Scope getScope() {
        return assertNotNull("scope", scope);
    }

    protected void onPreExecute() {

    }

    protected void onPostExecute(@Nullable Response response, @Nullable Throwable t) {

    }

    /**
     * Executes this request using the given scope:
     * <ul>
     * <li>Uses the {@link Container#NAME_CONTAINER} to lookup an instance of {@link Container}</li>
     * <li>If no appropriate executor has been found, executes synchronous and inline (tbd) and should be avoided in general</li>
     * <li>By definition dispatches any error</li>
     * </ul>
     */
    public Task<Result<Response>> execute(Scope scope) {
        SettableTask<Result<Response>> task = SettableTask.create(scope, toString());
        final Scope actualScope = createSubScope(scope);
        Handler backgroundThread = actualScope.resolve(Container.NAME_REQUEST_HANDLER, Handler.class);
        Runnable job = () -> {
            try {
                if (!initialized) {
                    synchronized (this) {
                        if (!initialized) {
                            initStatic();
                            initialized = true;
                        }
                    }
                }
                onPreExecute();
                Response res = onExecute();
                onPostExecute(res, null);
                task.set(Result.create(res));
            } catch (Throwable e) {
                onPostExecute(null, e);
                task.set(Result.auto(e));
            }
        };
        if (backgroundThread == null) {
            LoggerFactory.getLogger(getClass()).warn("no background thread defined, executing Request '{}' inline", toString());
            job.run();
        } else {
            backgroundThread.post(job);
        }
        return task;
    }


    /**
     * Asserts that the given object is not null. If it is null, this situation is considered to be a programming error.
     */
    protected <T> T assertNotNull(Object msg, @Nullable T obj) throws Panic {
        if (obj == null) {
            throw new Panic(msg + ": null value is not allowed");
        }
        return obj;
    }


    /**
     * Gets or creates an instance for the given field
     *
     * @param field
     * @return
     */
    protected Object get(Field field) {
        return null;
    }

    @Nullable
    protected <T> T get(String name, Class<T> type) {
        return null;
    }

    protected <T> T get(Class<T> type) {
        return null;
    }
}

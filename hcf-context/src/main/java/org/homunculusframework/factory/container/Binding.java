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
import java.util.concurrent.Callable;
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
     * The scope is kept as a member and only valid after {@link #onBind(Scope)}.
     * The scope to resolve everything else. This already contains all values from this binding.
     */
    @Nullable
    private transient Scope scope;

    @Nullable
    private volatile transient Response response;

    @Nullable
    private transient StackTraceElement[] stackTrace;

    /**
     * This constructor is intentionally package private to force that every subtype is either a {@link MethodBinding} or a {@link ObjectBinding}
     */
    Binding() {
        LoggerFactory.getLogger(getClass()).info("create {}", this);
    }


    /**
     * Bindings are executed asynchronously and therefore always loose their creation context.
     * One can set any custom stack trace
     *
     * @return
     */
    @Nullable
    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    /**
     * See {@link #getStackTrace()}
     *
     * @param callstack the custom callstack
     */
    public void setStackTrace(@Nullable StackTraceElement[] callstack) {
        this.stackTrace = callstack;
    }

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
    protected void onBind(Scope dst) {

    }

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

    protected void onPreExecute(SettableTask<Result<Response>> task) {

    }

    /**
     * You have to set the task, to finish the execution
     */
    protected void onPostExecute(SettableTask<Result<Response>> task, @Nullable Response response, @Nullable Throwable t) {
        task.set(Result.create(response).setThrowable(t));
    }


    /**
     * You have to set the task to finish the destruction
     */
    protected void onPreDestroy(SettableTask<Result<Response>> task, @Nullable Response response) {
        task.set(Result.create(response));
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
        LoggerFactory.getLogger(getClass()).info("execute {}", getClass().getSimpleName());
        SettableTask<Result<Response>> task = SettableTask.create(scope, toString());
        final Scope actualScope = createSubScope(scope);
        Handler backgroundThread = actualScope.resolve(Container.NAME_REQUEST_HANDLER, Handler.class);
        Runnable job = () -> {
            try {
                onPreExecute(task);
                response = onExecute();
                onPostExecute(task, response, null);
            } catch (Throwable e) {
                onPostExecute(task, response, e);
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
     * Destroys and tears down the associated object.
     *
     * @return
     */
    public Task<Result<Response>> destroy() {
        LoggerFactory.getLogger(getClass()).info("destroy {}", this);
        SettableTask<Result<Response>> task = SettableTask.create(scope, toString());
        onPreDestroy(task, response);
        response = null;

        //destroy the scope in the next cycle, so that whenDone listeners of the returned task can work happily
        task.whenDone(res -> {
            scope.resolve(Handler.class).post(() -> {
                Scope scope = this.scope;
                if (scope != null) {
                    scope.destroy();
                    this.scope = null;
                }
            });
        });
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
        return scope.resolve(name, type);
    }

    protected <T> T get(Class<T> type) {
        return scope.resolve(type);
    }


    /**
     * Posts the given closure into the named handler bailing out if handler is not configured
     *
     * @param handlerName
     * @param closure     should not throw exception
     */
    protected void post(String handlerName, Runnable closure) {
        //introduce a transparent alias for the main handler vs looper, which is in android the same
        if (handlerName.equals("$mainLooper")) {
            handlerName = Container.NAME_MAIN_HANDLER;
        }
        Handler handler = get(handlerName, Handler.class);
        if (handler == null) {
            throw new Panic("no such handler: " + handlerName);
        }
        handler.post(() -> {
            try {
                closure.run();
            } catch (Exception e) {
                LoggerFactory.getLogger(getClass()).error("failed to post on " + handler, e);
            }
        });
    }

    /**
     * Checks if the target of this and the other are equal. Parameters are intentionally ignored.
     * E.g. a {@link MethodBinding} must denote the same class and method and an {@link ObjectBinding}
     * the same class. The default implementation just checks if the classes are the same, which
     * is in case of generated code always the correct thing.
     *
     * @param other the other binding
     * @return true or false
     */
    public boolean equalsTarget(@Nullable Object other) {
        if (other == null) {
            return false;
        }
        if (other.getClass() == getClass()) {
            return true;
        }
        return false;
    }
}

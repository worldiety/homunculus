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

import org.homunculusframework.factory.ObjectFactory;
import org.homunculusframework.factory.ObjectInjector;
import org.homunculusframework.factory.annotation.PostConstruct;
import org.homunculusframework.factory.annotation.RequestMapping;
import org.homunculusframework.factory.annotation.Widget;
import org.homunculusframework.lang.Classname;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.navigation.ModelAndView;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The container holds the life instances and manages the life cycles of entities and stereotypes in a scope.
 * It is the executive part of a {@link Configuration}.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class Container {
    /**
     * Denotes an array of stack trace elements ({@link StackTraceElement}[]), usually to save some call-site specific trace
     * e.g. to carry information in asynchronous calling situations
     */
    public final static String NAME_CALLSTACK = "$stack";
    /**
     * A {@link Container} instance itself
     */
    public final static String NAME_CONTAINER = "$container";

    /**
     * Denotes a {@link Handler} used to post into the main thread of an application, which is usually the UI thread.
     */
    public final static String NAME_MAIN_HANDLER = "$mainHandler";

    /**
     * Denotes a {@link Handler} used to post into some background thread(s) of an application, which is NEVER the UI thread.
     * Used e.g. for {@link PostConstruct}
     */
    public final static String NAME_BACKGROUND_HANDLER = "$backgroundHandler";

    /**
     * Denotes a {@link Handler} used to perform requests against controllers in a container, which is NEVER the UI thread.
     */
    public final static String NAME_REQUEST_HANDLER = "$requestHandler";

    /**
     * Denotes an optional {@link Handler} used to perform create or inflate views asynchronously. This is mostly an Android feature.
     */
    public final static String NAME_INFLATER_HANDLER = "$inflaterHandler";

    private final Configuration configuration;

    /**
     * Controllers export methods using {@link RequestMapping} and are directly linked here
     */
    private final Map<String, ControllerEndpoint> controllerEndpoints;
    private final List<Object> controllers;
    private boolean running;

    public Container(Configuration configuration) {
        this.configuration = configuration;
        this.controllerEndpoints = new HashMap<>();
        this.controllers = new ArrayList<>();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Starts this container by creating all {@link ControllerEndpoint}s synchronously and inserting them into
     * the {@link Configuration#getRootScope()}. Also inserts all dependencies of the controllers if possible.
     * This also starts all asynchronous lifecycle methods like {@link PostConstruct}.
     * To avoid deadlocks and hickups, a controller is never allowed to use the main thread with PostConstruct.
     * Here you can also inspect exceptions occured while creation.
     */
    public List<Throwable> start() {
        List<Throwable> throwables = new ArrayList<>();
        startInternal((container, failures) -> {
            throwables.addAll(failures);
            synchronized (Container.this) {
                Container.this.notify();
            }
        });
        synchronized (Container.this) {
            try {
                Container.this.wait();
            } catch (InterruptedException e) {
                throw new Panic(e);
            }
        }
        return throwables;
    }


    private void startInternal(@Nullable OnStartCompleteCallback onCompleteClosure) {
        synchronized (controllerEndpoints) {
            if (running) {
                LoggerFactory.getLogger(getClass()).warn("already running");
                return;
            }
            long start = System.currentTimeMillis();
            LoggerFactory.getLogger(getClass()).info("HCF container starting...");
            //create controllers first
            ObjectFactory factory = configuration.getObjectFactory();
            Scope containerScope = configuration.getRootScope();
            for (Class<?> clazz : configuration.getControllers()) {
                Object instance = factory.create(containerScope, clazz);
                if (instance == null) {
                    continue;
                }
                LoggerFactory.getLogger(getClass()).info("created {}", Classname.getName(clazz));
                controllers.add(instance);
                for (ControllerEndpoint endpoint : ControllerEndpoint.list(instance)) {
                    ControllerEndpoint existing = controllerEndpoints.get(endpoint.getRequestMapping());
                    if (existing != endpoint && existing != null) {
                        LoggerFactory.getLogger(getClass()).error("ambiguous @RequestMapping '{}' for endpoints: {} and {}", endpoint.getRequestMapping(), existing.toDebugCallString(), endpoint.toDebugCallString());
                        LoggerFactory.getLogger(getClass()).error("endpoint ignored {}", endpoint.toDebugCallString());
                        continue;
                    }
                    controllerEndpoints.put(endpoint.getRequestMapping(), endpoint);
                    LoggerFactory.getLogger(getClass()).info(" {} -> {}", endpoint.getRequestMapping(), endpoint.getMethod().getName());
                }
            }

            //now perform injection, this also allows cyclic dependencies between controllers
            AtomicInteger completeCounter = new AtomicInteger();
            List<Throwable> exceptions = new ArrayList<>();
            final int s = controllers.size();
            ObjectInjector injector = configuration.getObjectInjector();
            for (Object ctr : controllers) {
                injector.inject(containerScope, ctr, (scope1, instance1, failures) -> {
                    synchronized (exceptions) {
                        exceptions.addAll(failures);
                    }
                    if (completeCounter.incrementAndGet() == s) {
                        LoggerFactory.getLogger(getClass()).info("...startup complete, took {} ms", System.currentTimeMillis() - start);
                        if (onCompleteClosure != null) {
                            onCompleteClosure.onComplete(Container.this, exceptions);
                        }
                    }
                });
            }
            containerScope.putNamedValue(NAME_CONTAINER, this);
            running = true;
        }
    }

    /**
     * Calls a configured endpoint, see also {@link ControllerEndpoint#invoke(Scope, Request)}
     *
     * @throws ExecutionException
     */
    public Object invoke(Scope scope, Request request) throws ExecutionException {
        ControllerEndpoint endpoint = controllerEndpoints.get(request.getRequestMapping());
        if (endpoint == null) {
            throw new ExecutionException("@RequestMapping '" + request.getRequestMapping() + "' is not defined in the current Configuration", null);
        } else {
            return endpoint.invoke(scope, request);
        }
    }

    /**
     * Creates a widget, which is either
     * <ul>
     * <li>Something registered with {@link Widget}</li>
     * <li>An Android view, like: @layout/activity_main (if the platform is android and correctly configured)</li>
     * </ul>
     */
    public Object createWidget(Scope scope, ModelAndView modelAndView) {
        return null;
    }

    public interface OnStartCompleteCallback {
        void onComplete(Container container, List<Throwable> failures);
    }

}

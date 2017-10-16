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

import org.homunculusframework.factory.ObjectCreator;
import org.homunculusframework.factory.ObjectDestroyer;
import org.homunculusframework.factory.ObjectInjector;
import org.homunculusframework.factory.connection.Connection;
import org.homunculusframework.factory.flavor.hcf.Widget;
import org.homunculusframework.factory.component.DefaultFactory;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;

/**
 * The all-in-one configuration for the HCF (HomunCulus Framework) which performs
 * IoC (Inversion Of Control) DI (Dependency Injection).
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Configuration {
    /**
     * Widget id -> widget class
     */
    private final Map<String, Class<?>> widgets;
    private final Map<String, Class<?>> immutableWidgets;


    /**
     * Just the collection of all controllers which will be instantiated
     */
    private final List<Class<?>> controllers;

    /**
     * Controllers may have proxied connections to them to automatically implement various comfort functions
     */
    private final List<Class<Connection>> controllerConnections;

    /**
     * Only used for error tracking, in a configuration all id's should be unique, even they don't share a common mindset (e.g. widgets and controllers)
     */
    private final Map<String, Class<?>> definedIds;


    private final Object lock = new Object();

    private final Scope rootScope;

    private ObjectCreator objectCreator;

    private ObjectInjector objectInjector;

    private ObjectDestroyer objectDestroyer;

    private final ArrayList<AnnotatedFieldProcessor> annotatedFieldProcessors;
    private final ArrayList<AnnotatedMethodsProcessor> onInjectMethodProcessors;
    private final ArrayList<AnnotatedMethodsProcessor> onTearDownProcessors;


    public Configuration(Scope scope) {
        this.widgets = new HashMap<>();
        this.immutableWidgets = Collections.unmodifiableMap(widgets);
        this.controllers = new ArrayList<>();
        this.definedIds = new HashMap<>();
        this.rootScope = scope;
        DefaultFactory factory = new DefaultFactory(this);
        this.objectCreator = factory;
        this.objectInjector = factory;
        this.objectDestroyer = factory;
        this.annotatedFieldProcessors = new ArrayList<>();
        this.onInjectMethodProcessors = new ArrayList<>();
        this.onTearDownProcessors = new ArrayList<>();
        this.controllerConnections = new ArrayList<>();
    }


    public void addFieldProcessor(AnnotatedFieldProcessor proc) {
        annotatedFieldProcessors.add(proc);
    }

    public void addMethodSetupProcessors(AnnotatedMethodsProcessor proc) {
        onInjectMethodProcessors.add(proc);
    }

    public void addMethodTearDownProcessors(AnnotatedMethodsProcessor proc) {
        onTearDownProcessors.add(proc);
    }

    public ArrayList<AnnotatedFieldProcessor> getFieldProcessors() {
        return annotatedFieldProcessors;
    }

    public ArrayList<AnnotatedMethodsProcessor> getMethodSetupProcessors() {
        return onInjectMethodProcessors;
    }

    public ArrayList<AnnotatedMethodsProcessor> getMethodTearDownProcessors() {
        return onTearDownProcessors;
    }


    public List<Class<Connection>> getControllerConnections() {
        return controllerConnections;
    }

    /**
     * Returns the root scope of this configuration
     */
    public Scope getRootScope() {
        return rootScope;
    }

    public void setObjectCreator(ObjectCreator objectCreator) {
        this.objectCreator = objectCreator;
    }


    public ObjectCreator getObjectCreator() {
        return objectCreator;
    }

    public ObjectInjector getObjectInjector() {
        return objectInjector;
    }

    public ObjectDestroyer getObjectDestroyer() {
        return objectDestroyer;
    }

    public void setObjectDestroyer(ObjectDestroyer objectDestroyer) {
        this.objectDestroyer = objectDestroyer;
    }

    public void setObjectInjector(ObjectInjector objectInjector) {
        this.objectInjector = objectInjector;
    }

    /**
     * Adds the given class to the configuration. This will inspect the class and holds it for future processing.
     * Returns only true if the class is useable for the HCF.
     */
    public boolean add(@Nullable Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        if (Connection.class.isAssignableFrom(clazz)) {
            controllerConnections.add((Class<Connection>) clazz);
            LoggerFactory.getLogger(getClass()).info("added @Connection {}", clazz);
            return true;
        }

        Widget widget = clazz.getAnnotation(Widget.class);
        Singleton controller = clazz.getAnnotation(Singleton.class);
        Named requestMapping = clazz.getAnnotation(Named.class);

        if (widget == null && controller == null && requestMapping == null) {
            return false;
        }

        if (widget != null && controller != null) {
            LoggerFactory.getLogger(getClass()).error("{} is not allowed to declare @Widget and @Controller at the same time", clazz);
            return false;
        }

        if (widget != null && requestMapping != null) {
            LoggerFactory.getLogger(getClass()).error("{} is not allowed to declare @Widget and @RequestMapping (for controllers only) at the same time", clazz);
            return false;
        }

        synchronized (lock) {
            if (widget != null) {
                String normalized = widget.value().trim();
                if (normalized.length() == 0) {
                    LoggerFactory.getLogger(getClass()).error("widget id must not be empty: {}", clazz);
                    return false;
                }
                if (normalized.charAt(0) != '/') {
                    normalized = "/" + normalized;
                }
                if (normalized.charAt(normalized.length() - 1) != '/') {
                    normalized = normalized + "/";
                }
                Class<?> other = definedIds.get(normalized);
                if (other != clazz && other != null) {
                    LoggerFactory.getLogger(getClass()).error("{} must be unique: both {} and {} share the same id", widget.value(), clazz, other);
                    return false;
                }

                widgets.put(normalized, clazz);
                definedIds.put(normalized, clazz);
                LoggerFactory.getLogger(getClass()).info("added @Widget {}", clazz);
                return true;
            }

            if (controller != null) {
                if (requestMapping != null) {
                    Class<?> other = definedIds.get(requestMapping.value());
                    if (other != clazz && other != null) {
                        LoggerFactory.getLogger(getClass()).error("{} must be unique: both {} and {} share the same id", requestMapping.value(), clazz, other);
                        return false;
                    }
                    definedIds.put(requestMapping.value(), clazz);
                }
                controllers.add(clazz);
                LoggerFactory.getLogger(getClass()).info("added @Controller {}", clazz);
                return true;
            }
        }
        return false;
    }


    /**
     * Returns the controller classes registered by {@link #add(Class)}
     */
    public List<Class<?>> getControllers() {
        return Collections.unmodifiableList(controllers);
    }

    /**
     * Returns the widget classes registered by {@link #add(Class)}
     */
    public Map<String, Class<?>> getWidgets() {
        return immutableWidgets;
    }
}

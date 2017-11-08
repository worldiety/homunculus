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
import org.homunculusframework.factory.container.AnnotatedComponentProcessor.AnnotatedComponent;
import org.homunculusframework.factory.component.DefaultFactory;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

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
    private final Map<String, Class<?>> beans;
    private final Map<String, Class<?>> immutableWidgets;


    /**
     * Just the collection of all controllers which will be instantiated
     */
    private final List<AnnotatedComponent> controllers;

    /**
     * Controllers may have proxied connections to them to automatically implement various comfort functions
     */
    private final List<Class<Connection>> controllerConnections;

    /**
     * Only used for error tracking, in a configuration all id's should be unique, even they don't share a common mindset (e.g. beans and controllers)
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
    private final ArrayList<AnnotatedComponentProcessor> annotatedComponentProcessors;
    private final ArrayList<AnnotatedRequestMapping> annotatedRequestMappings;
    private final ArrayList<ScopePrepareProcessor> scopePrepareProcessors;

    public Configuration(Scope scope) {
        this.beans = new HashMap<>();
        this.immutableWidgets = Collections.unmodifiableMap(beans);
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
        this.annotatedComponentProcessors = new ArrayList<>();
        this.annotatedRequestMappings = new ArrayList<>();
        this.scopePrepareProcessors = new ArrayList<>();
    }

    public void addScopePrepareProcessor(ScopePrepareProcessor scopePrepareProcessor) {
        scopePrepareProcessors.add(scopePrepareProcessor);
    }

    public void addComponentProcessor(AnnotatedComponentProcessor annotatedComponentProcessor) {
        annotatedComponentProcessors.add(annotatedComponentProcessor);
    }

    public void addRequestMapping(AnnotatedRequestMapping annotatedRequestMapping) {
        annotatedRequestMappings.add(annotatedRequestMapping);
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

        synchronized (lock) {
            for (AnnotatedComponentProcessor componentProcessor : annotatedComponentProcessors) {
                AnnotatedComponent component = componentProcessor.process(rootScope, clazz);
                if (component == null) {
                    continue;
                }
                switch (component.getType()) {
                    case BEAN:
                        Class<?> other = definedIds.get(component.getName());
                        if (other != clazz && other != null) {
                            LoggerFactory.getLogger(getClass()).error("{} must be unique: both {} and {} share the same id", component.getName(), clazz, other);
                            return false;
                        }

                        beans.put(component.getName(), clazz);
                        definedIds.put(component.getName(), clazz);
                        LoggerFactory.getLogger(getClass()).info("added @Widget {}", clazz);
                        continue;//go on and ask other processors, e.g. a controller is also always a bean
                    case CONTROLLER:
                        other = definedIds.get(component.getName());
                        if (other != clazz && other != null) {
                            LoggerFactory.getLogger(getClass()).error("{} must be unique: both {} and {} share the same id", component.getName(), clazz, other);
                            return false;
                        }
                        definedIds.put(component.getName(), clazz);
                        controllers.add(component);
                        beans.put(component.getName(), clazz);
                        LoggerFactory.getLogger(getClass()).info("added @Controller {}", clazz);
                        continue;
                    case CONTROLLER_CONNECTION:
                        controllerConnections.add((Class<Connection>) clazz);
                        LoggerFactory.getLogger(getClass()).info("added @Connection {}", clazz);
                        return true;//cancel early, this one cannot be a bean
                    default:
                        throw new Panic();
                }
            }
        }

        return false;
    }

    public List<AnnotatedRequestMapping> getAnnotatedRequestMappings() {
        return Collections.unmodifiableList(annotatedRequestMappings);
    }

    /**
     * Returns the controller classes registered by {@link #add(Class)}
     */
    public List<AnnotatedComponent> getControllers() {
        return Collections.unmodifiableList(controllers);
    }

    /**
     * Returns the bean classes registered by {@link #add(Class)}
     */
    public Map<String, Class<?>> getBeans() {
        return immutableWidgets;
    }

    /**
     * Returns the processors to prepare a scope and fill it with custom objects ({@link Container#prepareScope(Scope)})
     *
     * @return the list of processors, never null
     */
    public ArrayList<ScopePrepareProcessor> getScopePrepareProcessors() {
        return scopePrepareProcessors;
    }
}

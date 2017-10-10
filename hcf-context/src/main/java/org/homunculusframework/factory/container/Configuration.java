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
import org.homunculusframework.factory.annotation.RequestMapping;
import org.homunculusframework.factory.annotation.Widget;
import org.homunculusframework.factory.component.DefaultFactory;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.stereotype.Controller;
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
    private final Map<String, Class<?>> widgets;

    /**
     * Just the collection of all controllers which will be instantiated
     */
    private final List<Class<?>> controllers;

    /**
     * Only used for error tracking, in a configuration all id's should be unique, even they don't share a common mindset (e.g. widgets and controllers)
     */
    private final Map<String, Class<?>> definedIds;


    private final Object lock = new Object();

    private final Scope rootScope;

    private ObjectFactory objectFactory;

    private ObjectInjector objectInjector;

    public Configuration(Scope scope) {
        this.widgets = new HashMap<>();
        this.controllers = new ArrayList<>();
        this.definedIds = new HashMap<>();
        this.rootScope = scope;
        this.objectFactory = new DefaultFactory();
        this.objectInjector = new DefaultFactory();
    }

    /**
     * Returns the root scope of this configuration
     */
    public Scope getRootScope() {
        return rootScope;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }


    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ObjectInjector getObjectInjector() {
        return objectInjector;
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
        Widget widget = clazz.getAnnotation(Widget.class);
        Controller controller = clazz.getAnnotation(Controller.class);
        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);

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
                Class<?> other = definedIds.get(widget.value());
                if (other != clazz && other != null) {
                    LoggerFactory.getLogger(getClass()).error("{} must be unique: both {} and {} share the same id", widget.value(), clazz, other);
                    return false;
                }

                widgets.put(widget.value(), clazz);
                definedIds.put(widget.value(), clazz);
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

}

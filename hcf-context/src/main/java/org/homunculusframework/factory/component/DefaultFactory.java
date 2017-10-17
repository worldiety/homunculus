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
package org.homunculusframework.factory.component;

import org.homunculusframework.factory.*;
import org.homunculusframework.factory.container.AnnotatedFieldProcessor;
import org.homunculusframework.factory.container.AnnotatedMethodsProcessor;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the default "universal" factory to create instances and perform the injection (IoC = inversion of control).
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class DefaultFactory implements ObjectCreator, ObjectInjector, ObjectDestroyer {

    //intentionally an arraylist to lower GC looping performance for bad VMs like Android
    private final ArrayList<AnnotatedFieldProcessor> annotatedFieldProcessors;
    private final ArrayList<AnnotatedMethodsProcessor> onInjectMethodProcessors;
    private final ArrayList<AnnotatedMethodsProcessor> onTearDownProcessors;

    public DefaultFactory(Configuration configuration) {
        this.annotatedFieldProcessors = configuration.getFieldProcessors();
        this.onInjectMethodProcessors = configuration.getMethodSetupProcessors();
        this.onTearDownProcessors = configuration.getMethodTearDownProcessors();
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

    @Override
    public <T> T create(Scope scope, Class<T> type) throws FactoryException {
        try {
            T res = createEmptyConstructor(scope, type);
            return res;
        } catch (FactoryException e) {
            //try again with the shortest constructor which is not empty
            try {
                T res = createShortestConstructor(scope, type);
                return res;
            } catch (FactoryException e2) {
                //suppressed and retrow the first exception
                e.addSuppressed(e2);
                throw e;
            }
        }
    }

    @Override
    public void inject(Scope scope, Object instance, ProcessingCompleteCallback injectionCompleteCallback) throws FactoryException {
        for (Field field : getFields(instance.getClass())) {
            final int s = annotatedFieldProcessors.size();
            for (int i = 0; i < s; i++) {
                AnnotatedFieldProcessor fieldInjector = annotatedFieldProcessors.get(i);
                fieldInjector.process(scope, instance, field);
            }
        }

        invokeMethods(scope, instance, onInjectMethodProcessors, injectionCompleteCallback);

    }


    @Override
    public void destroy(Scope scope, Object instance, ProcessingCompleteCallback tearDownCompleteCallback) throws FactoryException {
        invokeMethods(scope, instance, onTearDownProcessors, tearDownCompleteCallback);
    }


    private static void invokeMethods(Scope scope, Object instance, List<AnnotatedMethodsProcessor> processors, ProcessingCompleteCallback callback) {
        List<Method> methods = getMethods(instance.getClass());
        final int s = processors.size();
        AtomicInteger completeCounter = new AtomicInteger();
        List<Throwable> exceptions = new ArrayList<>();
        if (s == 0) {
            callback.onComplete(scope, instance, exceptions);
        } else {
            for (int i = 0; i < s; i++) {
                processors.get(i).process(scope, instance, methods, (scope1, instance1, failures) -> {
                    synchronized (exceptions) {
                        exceptions.addAll(failures);
                    }
                    if (completeCounter.incrementAndGet() == s) {
                        callback.onComplete(scope1, instance1, exceptions);
                    }
                });
            }
        }
    }

    private <T> T createEmptyConstructor(Scope scope, Class<T> type) throws FactoryException {
        try {
            Constructor<T> c = type.getConstructor();
            c.setAccessible(true);
            T res = c.newInstance();
            return res;
        } catch (Exception e) {
            throw new FactoryException("unable to create instance of " + scope, e);
        }
    }

    private <T> T createShortestConstructor(Scope scope, Class<T> type) throws FactoryException {
        Constructor<T> shortestNotEmpty = null;
        for (Constructor c : type.getConstructors()) {
            int parameterCount = c.getParameterTypes().length;
            if (parameterCount == 0) {
                continue;
            }
            if (shortestNotEmpty == null || parameterCount > shortestNotEmpty.getParameterTypes().length) {
                shortestNotEmpty = c;
            }
        }
        if (shortestNotEmpty == null) {
            throw new FactoryException("no non-empty constructor found on " + type);
        }
        return create(scope, shortestNotEmpty);
    }

    private <T> T create(Scope scope, Constructor<T> constructor) throws FactoryException {
        try {
            constructor.setAccessible(true);
            Class[] paramTypes = constructor.getParameterTypes();
            Object[] params = new Object[paramTypes.length];
            for (int i = 0; i < params.length; i++) {
                params[i] = scope.resolve(paramTypes[i]);
                if (params[i] == null) {
                    LoggerFactory.getLogger(constructor.getDeclaringClass()).error("missing parameter in constructor: {}. parameter -> type {} (check your scope environment)", i, paramTypes[i]);
                }
            }
            return constructor.newInstance(params);
        } catch (Exception e) {
            throw new FactoryException(e);
        }
    }

    public static StackTraceElement[] getCallStack(int offset) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        return Arrays.copyOfRange(trace, offset, trace.length);
    }

    public static List<Field> getFields(Class clazz) {
        List<Field> res = new ArrayList<>();
        Class root = clazz;
        while (root != null) {
            for (Field m : root.getDeclaredFields()) {
                res.add(m);
            }
            root = root.getSuperclass();
        }
        return res;
    }

    public static List<Method> getMethods(Class clazz) {
        List<Method> res = new ArrayList<>();
        Class root = clazz;
        while (root != null) {
            for (Method m : root.getDeclaredMethods()) {
                res.add(m);
            }
            root = root.getSuperclass();
        }
        return res;
    }

    public static String stripToString(Object value) {
        if (value == null) {
            return "null";
        } else {
            String r = value.toString();
            if (r.length() > 120) {
                return r.substring(0, 120);
            }
            return r;
        }
    }

}

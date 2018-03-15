///*
// * Copyright 2017 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.homunculusframework.factory.container;
//
//import org.homunculusframework.factory.container.AnnotatedRequestMapping.AnnotatedMethod;
//import org.homunculusframework.lang.Reflection;
//import org.homunculusframework.scope.Scope;
//import org.slf4j.LoggerFactory;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//
///**
// * A configured endpoint, used to get invoked by reflection. Is applied on {@link org.homunculusframework.factory.container.AnnotatedComponentProcessor.ComponentType#CONTROLLER}
// * classes and identifies endpoints using {@link AnnotatedRequestMapping} and methods parameter names using {@link }
// *
// * @author Torben Schinke
// * @since 1.0
// */
//public final class ControllerEndpoint {
//    private final String requestMapping;
//    private final Object instance;
//    private final Method method;
//    private final Class[] parameterTypes;
//    private final Object[] callTmp;
//    //this is ugly: by default and below java 8 parameter names cannot be read reliably. A name is null, if undefined, which should be avoid by the developer
//    private final String[] parameterNames;
//    private final AnnotatedMethod annotatedMethod;
//
//    public ControllerEndpoint(String requestMapping, Object instance, AnnotatedMethod annotatedMethod) {
//        this.requestMapping = requestMapping;
//        this.annotatedMethod = annotatedMethod;
//        this.instance = instance;
//        this.method = annotatedMethod.getMethod();
//        this.parameterTypes = annotatedMethod.getParameterTypes();
//        this.callTmp = new Object[this.parameterTypes.length];
//        this.parameterNames = annotatedMethod.getParameterNames();
//    }
//
//    /**
//     * Returns the normalized request mapping (begins and ends with a / )
//     */
//    public String getRequestMapping() {
//        return requestMapping;
//    }
//
//    /**
//     * The according method to invoke
//     */
//    public Method getMethod() {
//        return method;
//    }
//
//    /**
//     * The controller instance to invoke this endpoint on.
//     */
//    public Object getInstance() {
//        return instance;
//    }
//
//
//    /**
//     * Invokes this method by scope and request data. First tries to resolve all relevant things by request and then with the scope.
//     * All checked and unchecked exceptions are captured and stuffed into an {@link ExecutionException}
//     *
//     * @param scope
//     * @param request
//     */
//    public Object invoke(Scope scope, Request request) throws ExecutionException {
//        Scope requestScope = createChild(scope, request);
//        try {
//            for (int i = 0; i < callTmp.length; i++) {
//                Class type = parameterTypes[i];
//                String name = parameterNames[i];
//                Object value = null;
//                if (name == null) {
//                    //no name available, try to resolve by type only
//                    value = requestScope.resolve(type);
//                } else {
//                    if (!requestScope.hasResolvable(name)) {
//                        LoggerFactory.getLogger(instance.getClass()).error("{}.{}: required parameter '{}' is undefined in Request", instance.getClass().getSimpleName(), annotatedMethod, name);
//                    } else {
//                        if (!requestScope.hasResolvable(name, type)) {
//                            LoggerFactory.getLogger(instance.getClass()).error("{}.{}: required parameter '{}' is not assignable", instance.getClass().getSimpleName(), annotatedMethod, name);
//                        }
//                    }
//
//                    //this can be null, even if another value could resolve that, but those are intentionally ignored and treated explicitly as null by definition
//                    value = requestScope.resolve(name, type);
//                }
//                callTmp[i] = value;
//            }
//
//            try {
//                return method.invoke(instance, callTmp);
//            } catch (InvocationTargetException e) {
//
//                //don't pollute the trace with irrelevant stuff -> happens only when the method died (may be also a checked one)
//                ExecutionException ee = new ExecutionException(e.getTargetException());
//
//                //grab the original call stack and get married with that, too
//                StackTraceElement[] elems = requestScope.get(Container.NAME_CALLSTACK, StackTraceElement[].class);
//                if (elems != null) {
//                    ee.setStackTrace(elems);
//                }
//
//                throw ee;
//            } catch (Throwable t) {
//                ExecutionException ee = new ExecutionException(t);
//
//                //grab the original call stack and get married with that, too
//                StackTraceElement[] elems = requestScope.get(Container.NAME_CALLSTACK, StackTraceElement[].class);
//                if (elems != null) {
//                    ee.setStackTrace(elems);
//                }
//                throw ee;
//            }
//        } finally {
//            requestScope.destroy();
//        }
//    }
//
//
//    public static Scope createChild(Scope parent, Request request) {
//        Scope scope = new Scope("request:" + request.getMapping(), parent);
//        request.forEachEntry(entry -> {
//            scope.put(entry.getKey(), entry.getValue());
//            return true;
//        });
//        return scope;
//    }
//
//    /**
//     * Grabs all available methods annotated from the given class and normalizes the
//     * mapping path by guaranteeing a slash at the start and and the end.
//     *
//     * @param parentName normalized (starts and ends with /) path
//     */
//    public static List<ControllerEndpoint> list(String parentName, Object instance, List<AnnotatedRequestMapping> mappers) {
//        Class<?> clazz = instance.getClass();
//
//        //find methods, up the class hierarchy
//        List<ControllerEndpoint> res = new ArrayList<>();
//        for (Method method : Reflection.getMethods(clazz)) {
//            for (int i = 0; i < mappers.size(); i++) {
//                AnnotatedMethod mapping = mappers.get(i).process(method);
//                if (mapping == null) {
//                    continue;
//                }
//                //now we have something like /myMethod/ or like /myController/myMethod/
//                ControllerEndpoint epoint = new ControllerEndpoint(parentName + mapping.getName(), instance, mapping);
//                res.add(epoint);
//
//            }
//        }
//        return res;
//    }
//
//
//    private static List<Method> getMethods(Class clazz) {
//        List<Method> res = new ArrayList<>();
//        Class root = clazz;
//        while (root != null) {
//            for (Method m : root.getDeclaredMethods()) {
//                res.add(m);
//            }
//            root = root.getSuperclass();
//        }
//        return res;
//    }
//
//
//    public String toDebugCallString() {
//        return Reflection.getName(instance.getClass()) + "." + method.getName();
//    }
//}

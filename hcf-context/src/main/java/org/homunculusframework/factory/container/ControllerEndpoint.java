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

import org.homunculusframework.lang.Classname;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A configured endpoint, used to get invoked by reflection. Uses {@link javax.inject.Singleton}
 * and {@link Named}.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class ControllerEndpoint {
    private final String requestMapping;
    private final Object instance;
    private final Method method;
    private final Class[] parameterTypes;
    private final Object[] callTmp;
    //this is ugly: by default and below java 8 parameter names cannot be read reliably. A name is null, if undefined, which should be avoid by the developer
    private final String[] parameterNames;

    public ControllerEndpoint(String requestMapping, Object instance, Method method) {
        this.requestMapping = requestMapping;
        this.instance = instance;
        this.method = method;
        this.parameterTypes = method.getParameterTypes();
        this.callTmp = new Object[this.parameterTypes.length];
        this.parameterNames = new String[this.parameterTypes.length];

        Annotation[][] declaredParameterAnnotations = method.getParameterAnnotations();
        NEXT_PARAM:
        for (int i = 0; i < declaredParameterAnnotations.length; i++) {
            Annotation[] annotations = declaredParameterAnnotations[i];
            for (Annotation annotation : annotations) {
                if (annotation instanceof Named) {
                    String name = ((Named) annotation).value();
                    parameterNames[i] = name;
                    continue NEXT_PARAM;
                }
            }
            //if we come here, no annotation was found -> not good!
            LoggerFactory.getLogger(instance.getClass()).error("{}.{}: {}. Parameter is unnamed and may cause invocation failure at runtime ", instance.getClass().getSimpleName(), getDebugNameWithParamTypes(), i);
        }
    }

    /**
     * Returns the normalized request mapping (begins and ends with a / )
     */
    public String getRequestMapping() {
        return requestMapping;
    }

    /**
     * The according method to invoke
     */
    public Method getMethod() {
        return method;
    }

    /**
     * The controller instance to invoke this endpoint on.
     */
    public Object getInstance() {
        return instance;
    }


    /**
     * Invokes this method by scope and request data. First tries to resolve all relevant things by request and then with the scope.
     * All checked and unchecked exceptions are captured and stuffed into an {@link ExecutionException}
     *
     * @param scope
     * @param request
     */
    public Object invoke(Scope scope, Request request) throws ExecutionException {
        Scope requestScope = createChild(scope, request);
        try {
            for (int i = 0; i < callTmp.length; i++) {
                Class type = parameterTypes[i];
                String name = parameterNames[i];
                Object value = null;
                if (name == null) {
                    //no name available, try to resolve by type only
                    value = requestScope.resolve(type);
                } else {
                    if (!requestScope.hasResolvableNamedValue(name)) {
                        LoggerFactory.getLogger(instance.getClass()).error("{}.{}: required parameter '{}' is undefined in Request", instance.getClass().getSimpleName(), getDebugNameWithParamTypes(), name);
                    } else {
                        if (!requestScope.hasResolvableNamedValue(name, type)) {
                            LoggerFactory.getLogger(instance.getClass()).error("{}.{}: required parameter '{}' is not assignable", instance.getClass().getSimpleName(), getDebugNameWithParamTypes(), name);
                        }
                    }

                    //this can be null, even if another value could resolve that, but those are intentionally ignored and treated explicitly as null by definition
                    value = requestScope.resolveNamedValue(name, type);
                }
                callTmp[i] = value;
            }

            try {
                return method.invoke(instance, callTmp);
            } catch (InvocationTargetException e) {

                //don't pollute the trace with irrelevant stuff -> happens only when the method died (may be also a checked one)
                ExecutionException ee = new ExecutionException(e.getTargetException());

                //grab the original call stack and get married with that, too
                StackTraceElement[] elems = requestScope.getNamedValue(Container.NAME_CALLSTACK, StackTraceElement[].class);
                if (elems != null) {
                    ee.setStackTrace(elems);
                }

                throw ee;
            } catch (Throwable t) {
                ExecutionException ee = new ExecutionException(t);

                //grab the original call stack and get married with that, too
                StackTraceElement[] elems = requestScope.getNamedValue(Container.NAME_CALLSTACK, StackTraceElement[].class);
                if (elems != null) {
                    ee.setStackTrace(elems);
                }
                throw ee;
            }
        } finally {
            requestScope.destroy();
        }
    }


    public static Scope createChild(Scope parent, Request request) {
        Scope scope = new Scope("request:" + request.getRequestMapping(), parent);
        request.forEach(entry -> {
            scope.putNamedValue(entry.getKey(), entry.getValue());
            return true;
        });
        return scope;
    }

    /**
     * Grabs all available methods annotated with {@link Named} from the given class and normalizes the
     * mapping path by guaranteeing a slash at the start and and the end.
     */
    public static List<ControllerEndpoint> list(Object instance) {
        Class<?> clazz = instance.getClass();
        Named mapping = clazz.getAnnotation(Named.class);
        StringBuilder path = new StringBuilder();
        if (mapping != null) {
            String tmp = mapping.value().trim();
            if (tmp.length() == 0) {
                path.append('/');
            } else {
                if (tmp.charAt(0) != '/') {
                    path.append('/');
                }
                path.append(tmp);
            }
        }
        if (path.charAt(path.length() - 1) != '/') {
            path.append('/');
        }

        //find methods, up the class hierarchy
        List<ControllerEndpoint> res = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            Named methodMap = method.getAnnotation(Named.class);
            if (methodMap != null) {
                String tmp = methodMap.value().trim();
                if (tmp.length() > 0) {
                    if (tmp.charAt(0) == '/') {
                        path.setLength(path.length() - 1);
                    }
                    path.append(tmp);
                    if (path.charAt(path.length() - 1) != '/') {
                        path.append('/');
                    }

                    //now we have something like /myMethod/ or like /myController/myMethod/
                    ControllerEndpoint epoint = new ControllerEndpoint(path.toString(), instance, method);
                    res.add(epoint);
                } else {
                    LoggerFactory.getLogger(ControllerEndpoint.class).error("invalid RequestMapping on {}.{}", clazz, method.getName());
                }
            }
        }
        return res;
    }


    private static List<Method> getMethods(Class clazz) {
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

    private String getDebugNameWithParamTypes() {
        String str = method.getName() + "(";
        for (int i = 0; i < parameterTypes.length; i++) {
            str += parameterTypes[i].getSimpleName();
            if (i < parameterTypes.length - 1) {
                str += ",";
            }
        }
        str += ")";
        return str;
    }

    public String toDebugCallString() {
        return Classname.getName(instance.getClass()) + "." + method.getName();
    }
}

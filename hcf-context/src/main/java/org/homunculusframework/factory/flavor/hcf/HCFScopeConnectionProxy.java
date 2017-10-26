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
package org.homunculusframework.factory.flavor.hcf;

import org.homunculusframework.factory.connection.Connection;
import org.homunculusframework.factory.connection.ConnectionProxyFactory;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.ScopePrepareProcessor;
import org.homunculusframework.lang.Ref;
import org.homunculusframework.lang.Reflection;
import org.homunculusframework.scope.Scope;

/**
 * Creates all supported {@link org.homunculusframework.factory.connection.Connection} proxies and inserts them into the given scope. The proxies are recycled when the scope
 * dies (and not thrown away). Only the root scope is inspected for {@link Connection}.
 */
public class HCFScopeConnectionProxy implements ScopePrepareProcessor {
    @Override
    public void process(Configuration configuration, Scope scope) {
        configuration.getRootScope().forEachEntry(entry -> {
            if (entry.getValue() instanceof ConnectionProxyFactory) {
                ConnectionProxyFactory factory = (ConnectionProxyFactory) entry.getValue();
                init(scope, factory);
            }
            return true;
        });
    }

    private static Connection init(Scope scope, ConnectionProxyFactory factory) {
        Connection connection = factory.borrowConnection(scope);
        scope.put("$proxy@" + Reflection.getName(factory.getControllerType()), connection);
        scope.addOnBeforeDestroyCallback(s -> factory.returnConnection(connection));
        return connection;
    }

    /**
     * Creates a new proxy instance for the given connection class, bound to the lifetime of the given scope.
     *
     * @param clazz the connection type
     * @param <T>   the type
     * @return null if no connection proxy has been found in the root scope. See also {@link HCFScopeConnectionProxy}
     */
    public static <T extends Connection> T create(Scope scope, Class<T> clazz) {
        Ref<T> ref = new Ref<>();
        Container container = scope.resolve(Container.NAME_CONTAINER, Container.class);
        container.getConfiguration().getRootScope().forEachEntry(entry -> {
            if (entry.getValue() instanceof ConnectionProxyFactory) {
                ConnectionProxyFactory factory = (ConnectionProxyFactory) entry.getValue();
                if (factory.getContract() == clazz) {
                    T t = (T) init(scope, factory);
                    ref.set(t);
                    return false;
                }
            }
            return true;
        });
        return ref.get();
    }
}

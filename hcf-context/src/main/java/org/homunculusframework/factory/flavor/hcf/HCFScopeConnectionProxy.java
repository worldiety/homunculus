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
import org.homunculusframework.factory.container.ScopePrepareProcessor;
import org.homunculusframework.lang.Reflection;
import org.homunculusframework.scope.Scope;

/**
 * Creates all supported {@link org.homunculusframework.factory.connection.Connection} proxies and inserts them into the given scope. The proxies are recycled when the scope
 * dies (and not thrown away).
 */
public class HCFScopeConnectionProxy implements ScopePrepareProcessor {
    @Override
    public void process(Configuration configuration, Scope scope) {
        configuration.getRootScope().forEachEntry(entry -> {
            if (entry.getValue() instanceof ConnectionProxyFactory) {
                ConnectionProxyFactory factory = (ConnectionProxyFactory) entry.getValue();
                Connection connection = factory.borrowConnection(scope);
                scope.putNamedValue("$proxy@" + Reflection.getName(factory.getControllerType()), connection);
                scope.addOnBeforeDestroyCallback(s -> factory.returnConnection(connection));
            }
            return true;
        });
    }
}

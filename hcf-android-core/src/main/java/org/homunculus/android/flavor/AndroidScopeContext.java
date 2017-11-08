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
package org.homunculus.android.flavor;

import android.content.Context;
import org.homunculus.android.core.ContextScope;
import org.homunculus.android.core.Android;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.ScopePrepareProcessor;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

/**
 * Provides a custom wrapper for any context into the given scope, wrapping the actual scope, so that
 * {@link ContextScope#getScope(Context)} returns the according scope.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class AndroidScopeContext implements ScopePrepareProcessor {
    @Override
    public void process(Configuration configuration, Scope scope) {
        Context context = scope.resolve(Android.NAME_CONTEXT, Context.class);
        if (context != null) {
            scope.put(Android.NAME_CONTEXT, new ContextScope(scope, context));
        } else {
            LoggerFactory.getLogger(getClass()).error("cannot provide a ContextScope, because {} does not provide any {}", scope, Android.NAME_CONTEXT);
        }
    }
}

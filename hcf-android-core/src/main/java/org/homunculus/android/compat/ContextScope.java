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
package org.homunculus.android.compat;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import org.homunculusframework.scope.Scope;

import javax.annotation.Nullable;

/**
 * A variant of {@link ContextWrapper} providing a {@link Scope}. If scope is destroyed, the context should not be used anymore, however
 * there is no further relation between both. You can simply provide a ContextScope into any Activity by overriding {@link Activity#attachBaseContext(Context)}
 * and calling super with a wrapping ContextScope.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class ContextScope extends ContextWrapper {
    private final Scope mScope;

    public ContextScope(Scope scope, Context base) {
        super(base);
        mScope = scope;
    }

    /**
     * Returns the scope, potentionally destroyed.
     */
    public Scope getScope() {
        return mScope;
    }

    /**
     * Tries to get the scope from the given context, returning null if no scope has been found.
     * Walks up any {@link ContextWrapper#getBaseContext()} hierarchy to find the next scope.
     */
    @Nullable
    public static Scope getScope(@Nullable Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof ContextScope) {
                return ((ContextScope) context).getScope();
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}

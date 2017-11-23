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
package org.homunculus.android.core;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.ContextWrapper;

import org.homunculus.android.compat.CompatApplication;
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
            if (context instanceof CompatApplication) {
                return ((CompatApplication) context).getScope();
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }


    /**
     * Creates a new scope for the given lifecycle owner (e.g. a Fragment or an Activity).
     * Note: The lifetime is
     * attached to the {@link android.arch.lifecycle.Lifecycle} AND the parent scope. So the returned
     * scope is destroyed if either of them is destroyed.
     */
    public static Scope createScope(@Nullable Scope parent, LifecycleOwner lifecycleOwner) {
        Scope scope = new Scope(lifecycleOwner.toString(), parent);
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Event.ON_DESTROY)
            void onDestroy() {
                scope.destroy();
            }
        });
        if (lifecycleOwner instanceof Context) {
            scope.put(Android.NAME_CONTEXT, lifecycleOwner);
        }
        return scope;
    }

    /**
     * See {@link Scope#resolve(String, Class)}
     *
     * @param context the context to get the scope form
     * @param name    the name to resolve
     * @param type    the type to cast to
     * @param <T>     the target type
     * @return the instance or null
     */
    @Nullable
    public static <T> T resolveNamedValue(@Nullable Context context, String name, Class<T> type) {
        Scope scope = getScope(context);
        if (scope != null) {
            return scope.resolve(name, type);
        }
        return null;
    }

    /**
     * See {@link Scope#resolve(Class)}
     *
     * @param context the context to get the scope form
     * @param type    the type to cast to
     * @param <T>     the target type
     * @return the instance or null
     */
    @Nullable
    public static <T> T resolve(@Nullable Context context, Class<T> type) {
        Scope scope = getScope(context);
        if (scope != null) {
            return scope.resolve(type);
        }
        return null;
    }
}

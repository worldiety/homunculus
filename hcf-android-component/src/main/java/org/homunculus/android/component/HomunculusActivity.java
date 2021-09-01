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
package org.homunculus.android.component;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.module.uncaughtexception.UncaughtException.BindUncaughtException;
import org.homunculus.android.core.ActivityEventDispatcher;
import org.homunculus.android.core.Android;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.flavor.hcf.ScopeElement;
import org.homunculusframework.factory.scope.ContextScope;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.factory.serializer.Serializable;
import org.homunculusframework.factory.serializer.Serializer;
import org.homunculusframework.navigation.Navigation;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;



/**
 * Just like the {@link EventAppCompatActivity} but provides an even more provided and opinionated configuration allowing
 * an easier bootstrapping, which just works out of the box and guides you towards a working application. It has
 * a default configuration with {@link DefaultAndroidNavigation} and {@link SwitchAnimationLayout}.
 * <p>
 * The default behavior is to use {@link #onSaveInstanceState(Bundle)} and {@link #onCreate(Bundle)}.
 * We do not use {@link #onRestoreInstanceState(Bundle)} because it is an entirely redundant method,
 * as defined by the doc in https://developer.android.com/guide/components/activities/activity-lifecycle.html
 * at 2018/01/10:
 * <pre>
 *     Because the onCreate() method is called whether the system is creating a new instance of your activity or recreating a previous one,
 *     you must check whether the state Bundle is null before you attempt to read it. If it is null, then the system is creating a new instance
 *     of the activity, instead of restoring a previous one that was destroyed.
 * </pre>
 *
 * @author Torben Schinke
 * @since 1.0
 */
public abstract class HomunculusActivity<T extends ContextScope<?>> extends EventAppCompatActivity implements UncaughtExceptionHandler {
    private final static String HC_NAVIGATION_STACK = "HC_NAVIGATION_STACK";
    private T scope;
    private Navigation navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);
    }

    /**
     * Intentionally separated from onCreate. Creates a better developer experience for customization purposes
     * e.g. for overriding. Also provides instances in the activity scope for:
     * <ul>
     * <li>{@link Android#NAME_NAVIGATION} by calling {@link #createNavigation()}</li>
     * <li>{@link Android#NAME_LAYOUT_INFLATER}</li>
     * <li>{@link Android#NAME_FRAGMENT_MANAGER}</li>
     * </ul>
     * Detects if the given savedInstanceState is null and potentially calls {@link #restoreStackState(Bundle)} and
     * {@link #onStackRestored(Navigation, Bundle)}.
     */
    protected void init(@Nullable Bundle savedInstanceState) {
        this.scope = createScope();
        this.navigation = createNavigation();
        this.scope.onCreate();
        if (savedInstanceState != null) {
            if (restoreStackState(savedInstanceState)) {
                if (!onStackRestored(navigation, savedInstanceState)) {
                    navigation.reload();
                }
            } else {
                navigation.forward(create());
            }
        } else {
            navigation.forward(create());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Scope scope = this.scope;
        if (scope != null) {
            scope.onDestroy();
        }
    }

    @Override
    public T getScope() {
        return scope;
    }

    protected abstract T createScope();

    /**
     * Called after the stack has been restored due to a saved instance state (see {@link #onSaveInstanceState(Bundle)}.
     * If this method returns false (the default behavior) the last state (top) is {@link Navigation#reload()}ed.
     * <p>
     * You can use this method if you need or want to drive the restoration through e.g. a splash screen or to
     * inject additional instances into the current scope before reloaded the restored state.
     *
     * @param navigation         the navigation instance into which the stack has been restored
     * @param savedInstanceState the bundle
     * @return return false to simply reload the last state from the navigation stack or return true, if you care yourself (e.g. just apply a state which reloads later)
     */
    protected boolean onStackRestored(Navigation navigation, @Nullable Bundle savedInstanceState) {
        return false;
    }

    /**
     * Creates the activity wide navigation implementation, which is by default {@link DefaultAndroidNavigation}
     *
     * @return the navigation to use. Available by name {@link Android#NAME_NAVIGATION} in the scope.
     */
    protected Navigation createNavigation() {
        DefaultHomunculusScope scope = (DefaultHomunculusScope) getScope().getParent();
        return new DefaultAndroidNavigation(getScope(), scope.getBackgroundHandler(), scope.getMainHandler());
    }


    @ScopeElement
    @Override
    public ActivityEventDispatcher<EventAppCompatActivity> getEventDispatcher() {
        return super.getEventDispatcher();
    }


    /**
     * Calls to {@link #saveStackState(Bundle)} to write the navigation stack into the bundle.
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveStackState(outState);
    }

    /**
     * Saves the current stack state into the given bundle by using the serializer in {@link #getInstanceStateSerializer()}
     *
     * @param outState the target
     */
    protected void saveStackState(Bundle outState) {
        Navigation nb = getNavigation();
        if (nb instanceof Navigation) {
            //create a defensive copy into a serializable list
            ArrayList<Binding<?, ?>> tmp = new ArrayList<>(((Navigation) nb).getStack());
            try {
                //serialize simply into a byte array
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                getInstanceStateSerializer().serialize(tmp, bout);
                outState.putByteArray(HC_NAVIGATION_STACK, bout.toByteArray());
            } catch (IOException e) {
                //clean it out, in case of programming error
                outState.putByteArray(HC_NAVIGATION_STACK, null);
                LoggerFactory.getLogger(getClass()).warn("onSaveInstanceState: the navigation stack contains a value which is not serializable by {}. Reason:", getInstanceStateSerializer(), e);
            }

        } else {
            //clean it out, if no navigation is available
            outState.putByteArray(HC_NAVIGATION_STACK, null);
            LoggerFactory.getLogger(getClass()).warn("onSaveInstanceState: getNavigation() does not provide a Navigation instance");
        }
    }


    /**
     * Restores the stack state from the given bundle. If nothing is available the stack is not changed
     *
     * @param savedInstanceState
     * @return true if the stack has been modified
     */
    protected boolean restoreStackState(Bundle savedInstanceState) {
        byte[] serializedStack = savedInstanceState.getByteArray(HC_NAVIGATION_STACK);
        Navigation nb = getNavigation();
        if (serializedStack != null && serializedStack.length > 0) {
            ByteArrayInputStream bin = new ByteArrayInputStream(serializedStack);
            try {
                ArrayList<Binding<?, ?>> tmp = getInstanceStateSerializer().deserialize(bin, ArrayList.class);
                navigation.getStack().clear();
                navigation.getStack().addAll(tmp);
                return true;
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).warn("onRestoreInstanceState: the serialized navigation stack contains a value which is not deserializable by {}. Reason:", getInstanceStateSerializer(), e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns the serializer for the instance state. This implementation returns the {@link Serializable} by
     * default.
     *
     * @return the serializer
     */
    protected Serializer getInstanceStateSerializer() {
        return new Serializable();
    }

    /**
     * Returns the instance of a navigation builder which is a specialized variant of a {@link org.homunculusframework.navigation.Navigation}
     * which is also available for resolving, at by default. Note that this cannot be guaranteed when reconfigured
     * manually.
     *
     * @return the navigation builder, which is null before {@link #onCreate(Bundle)} and after {@link #onDestroy()}
     */
    @ScopeElement
    public Navigation getNavigation() {
        return navigation;
    }

    @Override
    public void onBackPressed() {
        if (!onDispatchNavigationBackPressed()) {
            super.onBackPressed();
        }
    }

    /**
     * Called to create the first UIS, which is often a kind of splash screen, which also performs
     * some bootstrapping logic, like preparing some more configuration etc.
     *
     * @return the request which creates the first state
     */
    abstract protected Binding<?, ?> create();

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Navigation nav = getNavigation();
        if (nav != null) {
            nav.redirect(new BindUncaughtException(e, null));
        } else {
            LoggerFactory.getLogger(getClass()).error("failed to handle uncaught exception, no navigation present. Uncaught Exception: ", e);
        }
    }
}

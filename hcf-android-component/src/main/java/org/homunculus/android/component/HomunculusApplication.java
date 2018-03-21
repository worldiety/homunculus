package org.homunculus.android.component;

import org.homunculus.android.compat.CompatApplication;
import org.homunculus.android.flavor.AndroidBackgroundHandler;
import org.homunculus.android.flavor.AndroidMainHandler;
import org.homunculusframework.factory.container.BackgroundHandler;
import org.homunculusframework.factory.container.MainHandler;
import org.homunculusframework.factory.flavor.hcf.ScopeElement;
import org.homunculusframework.factory.scope.ContextScope;

/**
 * TODO think about workaround like https://medium.com/@andretietz/auto-initialize-your-android-library-2349daf06920
 * <p>
 * Created by Torben Schinke on 08.11.17.
 */

public abstract class HomunculusApplication<T extends ContextScope<?>> extends CompatApplication {


    private T scope;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    protected void init() {
        scope = createScope();
        MaterialFont.init(this);
        new UnbreakableCrashHandler().install(this);
    }


    /**
     * The default main handler posts it's execution tasks always into the main thread of the application.
     * Never post intensive tasks here, because it may cause UI hickups, stalls or even deadlocks.
     */
    @ScopeElement
    public MainHandler createMainHandler() {
        return new AndroidMainHandler();
    }

    /**
     * The default background handler contains at most 32 threads. Override this to supply a custom pool or handler.
     */
    @ScopeElement
    public BackgroundHandler createBackgroundHandler() {
        return new AndroidBackgroundHandler(32, "background", Thread.MIN_PRIORITY);
    }


    @Override
    public T getScope() {
        return scope;
    }

    protected abstract T createScope();

}

package org.homunculus.android.component;

import org.homunculus.android.compat.CompatApplication;
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

    @Override
    public T getScope() {
        return scope;
    }

    protected abstract T createScope();

}

package org.homunculus.android.component;

import android.os.Handler;
import android.os.Looper;

import org.homunculus.android.compat.CompatApplication;
import org.homunculus.android.core.Android;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.scope.Scope;

/**
 * Created by Torben Schinke on 08.11.17.
 */

public abstract class HomunculusApplication extends CompatApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    protected void init() {
        MaterialFont.init(this);

        Configuration cfg = createConfiguration();
        provide(cfg.getRootScope());
        onConfigure(cfg);


        //setup and start the HCF container
        Container container = new Container(cfg);
        container.start();

        new UnbreakableCrashHandler().install(this);
    }

    /**
     * Called to provide additional instances for injection
     *
     * @param scope the root scope
     */
    protected void provide(Scope scope) {
        scope.put(Android.NAME_MAIN_HANDLER, new Handler(Looper.getMainLooper()));
    }

    /**
     * Applies the configuration
     *
     * @param configuration the configuration
     */
    protected abstract void onConfigure(Configuration configuration);

}

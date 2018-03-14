package org.homunculus.android.component;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;

import org.homunculus.android.compat.CompatApplication;
import org.homunculus.android.core.Android;
import org.homunculusframework.concurrent.Async;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.ObjectContainer;
import org.homunculusframework.factory.container.ObjectContainerCallback;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Ref;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO think about workaround like https://medium.com/@andretietz/auto-initialize-your-android-library-2349daf06920
 * <p>
 * Created by Torben Schinke on 08.11.17.
 */

public class HomunculusApplication extends CompatApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    protected void init() {
        MaterialFont.init(this);

        Configuration cfg = createConfiguration();
        onConfigure(cfg);
        onProvide(cfg.getRootScope());


        //setup and start the HCF container
        Container container = new Container(cfg);
        container.start();

        new UnbreakableCrashHandler().install(this);
    }

    /**
     * Called to onProvide additional instances for injection
     *
     * @param scope the root scope
     */
    protected void onProvide(Scope scope) {
//        scope.put(Android.NAME_MAIN_HANDLER, new Handler(Looper.getMainLooper()));
    }

    /**
     * Applies the configuration. The default is to load a generated class named 'Controllers' from the application package by
     * calling {@link #start(String)}. Dispatches to any activity which is an instance of {@link org.homunculusframework.factory.container.ObjectContainerCallback}
     *
     * @param configuration the configuration
     */
    protected void onConfigure(Configuration configuration) {
        try {
            start(getPackageName() + ".Controllers");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }


    /**
     * Starts the class name treating as {@link ObjectContainer}
     *
     * @param classname
     * @return
     * @throws Panic
     */
    protected Task<List<Result<Object>>> start(String classname) throws Panic, IllegalAccessException, InstantiationException {
        try {
            long start = System.currentTimeMillis();
            Class clazz = Class.forName(classname);
            ObjectContainer objectContainer = (ObjectContainer) clazz.newInstance();
            Task<List<Result<Object>>> res = objectContainer.start(getScope());
            res.whenDone(r -> {
                Ref<Boolean> dispatched = new Ref<>(false);
                getScope().forEachScope(childScope -> {
                    ObjectContainerCallback cb = childScope.resolve(ObjectContainerCallback.class);
                    if (cb != null) {
                        cb.onObjectContainerCompleted(objectContainer, r, null);
                        dispatched.set(true);
                    }
                    return true;
                });

                if (!dispatched.get()) {
                    Panic myPanic = null;
                    for (Result<Object> rObj : r) {

                        if (rObj.getThrowable() != null) {
                            if (myPanic == null) {
                                myPanic = new Panic(rObj.getThrowable());
                            } else {
                                if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
                                    myPanic.addSuppressed(rObj.getThrowable());
                                }
                            }
                            LoggerFactory.getLogger(getClass()).info("{} [FAILED]", rObj.getThrowable().getMessage());
                        } else {
                            LoggerFactory.getLogger(getClass()).info("{} [STARTED]", rObj.get().getClass().getName());
                        }
                    }
                    if (myPanic != null) {
                        throw myPanic;
                    }
                }
                LoggerFactory.getLogger(getClass()).info("{} created {} instances in {}ms", classname, r.size(), System.currentTimeMillis() - start);
            });
            return res;

        } catch (ClassNotFoundException e) {
            LoggerFactory.getLogger(getClass()).warn("expected {} to apply AutoConfiguration. Did you apply the HCF code generator plugin properly?", e.getMessage());
            SettableTask task = SettableTask.create("");
            task.set(new ArrayList<>());
            return task;
        }
    }
}

package org.homunculus.android.example.concept;


import android.content.Context;

import org.homunculus.android.component.module.storage.Persistent;
import org.homunculusframework.concurrent.Cancellable;
import org.homunculusframework.concurrent.NotInterruptible;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.container.ModelAndView;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Torben Schinke on 16.03.18.
 */
@Singleton
public class ControllerA {

    @Inject
    MyCustomDatabase myCustomDatabase;

    @Inject
    Persistent<Boolean> testPersistent;

    @Inject
    Persistent<Boolean> testPersistent2;

    @Inject
    Context regression;

    public String sayHelloToA(int x) throws InterruptedException {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            LoggerFactory.getLogger(getClass()).info("interrupted");
            throw e;
        }
        return "hello " + x;
    }

    @NotInterruptible
    public String sayHelloToA1(int x) throws InterruptedException {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            LoggerFactory.getLogger(getClass()).info("interrupted");
            throw e;
        }
        return "hello " + x;
    }

    @Cancellable
    public String sayHelloToA2(int x) throws InterruptedException {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            LoggerFactory.getLogger(getClass()).info("interrupted");
            throw e;
        }
        return "hello " + x;
    }

    public ModelAndView nextUIS(String asdf) {
        return new BindUISB(new UISBModel());
    }
}

package org.homunculus.android.example.concept;


import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.container.ModelAndView;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Torben Schinke on 16.03.18.
 */
@Singleton
public class ControllerA {

    @Inject
    MyCustomDatabase myCustomDatabase;

    public String sayHelloToA(int x) {
        return "hello " + x;
    }

    public ModelAndView doJob1(String param) {
        return new BindUISA();
    }

    public ModelAndView doJob2(String param, int p2, Float p3) {
        return new BindUISA();
    }
}

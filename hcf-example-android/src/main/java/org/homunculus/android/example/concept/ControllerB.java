package org.homunculus.android.example.concept;

import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.container.ModelAndView;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Torben Schinke on 16.03.18.
 */
@Singleton
public class ControllerB {

    @Inject
    ControllerA controllerA;


    public UISBModel queryB(int x) throws Exception {
        return null;
    }


    public ModelAndView queryWithBindingDelegate(String param) {
        return new BindUISB(new UISBModel());
    }
}

package org.homunculus.android.example.module.flow;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;

import org.homunculus.android.component.module.splash.Splash;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.flavor.hcf.ViewComponent;
import org.homunculusframework.navigation.Navigation;

import javax.annotation.PostConstruct;
import javax.inject.Inject;


/**
 * Created by Torben Schinke on 08.11.17.
 */
@ViewComponent
public class MySplash extends Splash {


    public MySplash(Activity activity, Handler main, Navigation navigation, LayoutInflater inflater) {
        super(activity, main, navigation, inflater);
    }

    @Override
    protected Binding<?> getTarget() {
        return new BindUISA();
    }
}

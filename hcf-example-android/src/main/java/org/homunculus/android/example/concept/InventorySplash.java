package org.homunculus.android.example.concept;

import android.app.Activity;
import android.os.Handler;
import android.view.LayoutInflater;

import org.homunculus.android.component.module.splash.Splash;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.flavor.hcf.Bind;
import org.homunculusframework.navigation.Navigation;

@Bind
public class InventorySplash extends Splash {


    public InventorySplash(Activity activity, Handler main, Navigation navigation, LayoutInflater inflater) {
        super(activity, main, navigation, inflater);
    }


    @Override
    protected Binding<?, ?> getTarget() {
        return null;
    }
}
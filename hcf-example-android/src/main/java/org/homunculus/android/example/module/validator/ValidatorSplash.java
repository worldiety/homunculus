package org.homunculus.android.example.module.validator;

import android.app.Activity;
import android.os.Handler;
import android.view.LayoutInflater;

import org.homunculus.android.component.module.splash.Splash;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.navigation.Navigation;

/**
 * Created by aerlemann on 04.02.18.
 */

public class ValidatorSplash extends Splash {
    public ValidatorSplash(Activity activity, Handler main, Navigation navigation, LayoutInflater inflater) {
        super(activity, main, navigation, inflater);
    }

    @Override
    protected Binding<?> getTarget() {
        return new BindValidatorUIS(null, null);
    }


}

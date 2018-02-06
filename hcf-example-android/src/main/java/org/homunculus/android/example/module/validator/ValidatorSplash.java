package org.homunculus.android.example.module.validator;

import org.homunculus.android.component.module.splash.Splash;

/**
 * Created by aerlemann on 04.02.18.
 */

public class ValidatorSplash extends Splash {
    @Override
    protected Class<?> getTarget() {
        return ValidatorUIS.class;
    }
}

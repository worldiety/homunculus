package org.homunculus.android.example.module.validator;

import org.homunculus.android.component.module.splash.Splash;
import org.homunculusframework.factory.container.Request;

/**
 * Created by aerlemann on 04.02.18.
 */

public class ValidatorSplash extends Splash {
    @Override
    protected Request getTarget() {
        return new Request(ValidatorUIS.class);
    }
}

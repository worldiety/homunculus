package org.homunculus.android.example.module.flow;

import org.homunculus.android.component.module.splash.Splash;

/**
 * Created by Torben Schinke on 08.11.17.
 */

public class MySplash extends Splash {

    @Override
    protected Class<?> getTarget() {
        return UISA.class;
    }
}

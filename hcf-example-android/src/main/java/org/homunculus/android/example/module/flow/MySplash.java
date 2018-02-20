package org.homunculus.android.example.module.flow;

import org.homunculus.android.component.module.splash.Splash;
import org.homunculusframework.factory.container.Request;

/**
 * Created by Torben Schinke on 08.11.17.
 */

public class MySplash extends Splash {

    @Override
    protected Request getTarget() {
        return new Request(UISA.class);
    }
}

package org.homunculus.android.component;

import org.homunculusframework.factory.container.BackgroundHandler;
import org.homunculusframework.factory.container.MainHandler;
import org.homunculusframework.factory.scope.Scope;

/**
 * The default hcf scope provides some basic library functions and the implementation is enforced through the code generator.
 * <p>
 * Created by Torben Schinke on 21.03.18.
 */
public interface DefaultHomunculusScope extends Scope {
    MainHandler getMainHandler();

    BackgroundHandler getBackgroundHandler();
}

package org.homunculusframework.factory.scope;

/**
 * Created by tschinke on 17.03.18.
 */

public interface Scope {
    Scope getParent();
    void onCreate();
    void onDestroy();
}

package org.homunculus.android.example.concept;

import android.app.Application;

import org.homunculusframework.factory.flavor.hcf.ScopeElement;

/**
 * Created by Torben Schinke on 16.03.18.
 */

public class ConceptApplication extends Application {


    private ConceptApplicationScope scope;

    @Override
    public void onCreate() {
        super.onCreate();
        scope = new ConceptApplicationScope(this);
    }

    public ConceptApplicationScope getScope() {
        return scope;
    }

    @ScopeElement
    MyCustomDatabase createMyCustomDatabase(){
        return new MyCustomDatabase();
    }
}

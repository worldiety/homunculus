package org.homunculus.android.example.concept;

import org.homunculus.android.example.concept.api.Binding;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Torben Schinke on 16.03.18.
 */
@Singleton
public class ControllerA {

    @Inject
    MyCustomDatabase myCustomDatabase;


    public Binding<?, ?> queryDB(String query) {
//        return new BindUISB(new UISBModel());
        return null;
    }
}

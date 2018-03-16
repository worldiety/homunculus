package org.homunculus.android.example.concept;

import android.content.Context;
import android.view.View;

import org.homunculusframework.factory.flavor.hcf.Bind;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Torben Schinke on 16.03.18.
 */
@Bind
public class UISB extends View {

    @Inject
    ControllerB controllerB;

    @Bind
    UISBModel model;

    public UISB(Context context, MyCustomDatabase fishyDb) {
        super(context);
    }

    @PostConstruct
    void apply() {

    }
}

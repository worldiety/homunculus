package org.homunculus.android.example.concept;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.module.toolbarbuilder.templates.DefaultToolbarConfiguration;
import org.homunculus.android.core.ActivityCallback;
import org.homunculusframework.factory.flavor.hcf.Bind;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.navigation.Navigation;


import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Torben Schinke on 16.03.18.
 */
@Bind
public class UISA extends LinearLayout {
    @Inject
    ControllerA controllerA;

    @Inject
    EventAppCompatActivity activity;


    @Inject
    Navigation navigation;

    @Inject
    FancyPojo fancyPojo;

    @Inject
    FancyPojo2 fancyPojo2;

    @Inject
    Scope scope;

    @Inject
    DefaultToolbarConfiguration toolbarTemplate;

    @Inject
    AsyncControllerA asyncControllerA;

    @Inject
    ActivityCallback<?> activityCallback;

    public UISA(Context context) {
        super(context);
    }


    @PostConstruct
    void apply() {
        activity.setContentView(toolbarTemplate.createToolbar(this));

        Button btn = new Button(getContext());
        btn.setText("hallo welt");
        addView(btn);
    }
}

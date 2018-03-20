package org.homunculus.android.example.concept;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.module.toolbarbuilder.templates.DefaultToolbarConfiguration;
import org.homunculusframework.factory.flavor.hcf.Bind;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.navigation.Navigation;


import javax.inject.Inject;

/**
 * Created by Torben Schinke on 16.03.18.
 */
@Bind
public class UISA extends View {
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

    public UISA(Context context) {
        super(context);

        activity.setContentView(toolbarTemplate.createToolbar(this));

        Button btn = new Button(getContext());
        btn.setOnClickListener(view -> {
//            navigation_forward(new BindUISB(new UISBModel()));
//            navigation_forward(new MethodControllerAQueryDB("select * from x"));
        });
    }


}

package org.homunculus.android.example.concept;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.core.ActivityCallback;
import org.homunculus.android.example.R;
import org.homunculus.android.example.concept.AsyncControllerA.InvokeControllerADoJob1;
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
    ConceptToolbarConfiguration toolbarTemplate;

    @Inject
    AsyncControllerA asyncControllerA;

    @Inject
    ActivityCallback<?> activityCallback;

    public UISA(Context context) {
        super(context);
    }


    @PostConstruct
    void apply() {
        toolbarTemplate.setUpAction(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "Hello from Toolbar", Toast.LENGTH_LONG).show();
            }
        }).setToolbarColor(R.color.toolbarColor).setElevation(25);
        activity.setContentView(toolbarTemplate.createToolbar(this));

        Button btn = new Button(getContext());
        btn.setText("hallo welt");
        btn.setOnClickListener(v -> {
            navigation.forward(new BindUISB(new UISBModel()));
        });
        addView(btn);
    }
}

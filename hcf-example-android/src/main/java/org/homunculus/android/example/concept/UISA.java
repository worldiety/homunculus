package org.homunculus.android.example.concept;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import org.homunculus.android.example.concept.api.Binding;
import org.homunculusframework.factory.flavor.hcf.Bind;
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
    Activity activity;

    @Inject
    Navigation navigation;

    @Inject
    FancyPojo fancyPojo;

    @Inject
    FancyPojo2 fancyPojo2;

    public UISA(Context context) {
        super(context);

        Button btn = new Button(getContext());
        btn.setOnClickListener(view -> {
//            navigation_forward(new BindUISB(new UISBModel()));
//            navigation_forward(new MethodControllerAQueryDB("select * from x"));
        });
    }


    private void navigation_forward(Binding<?, ?> blub) {
        //fakenavigation contract for visual completeness
        //blub.apply(activityScope) <- by definition this would be always the activity scope, however no typesafety here
    }
}

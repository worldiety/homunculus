package org.homunculus.android.example.concept;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import org.homunculus.android.component.ViewTransitionAnimator;
import org.homunculus.android.example.R;
import org.homunculus.android.example.my.packagename.TestView;
import org.homunculus.android.flavor.Resource;
import org.homunculusframework.factory.flavor.hcf.Bind;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

    @Resource(R.string.app_name)
    String myAndroidResource;

    @Resource(R.layout.activity_cart)
    View myView;

    @Resource(R.drawable.ic_launcher)
    Drawable blub;

    @Resource(R.drawable.ic_launcher)
    Bitmap bmp;

    @Inject
    TestView testView;

    @Inject
    ViewTransitionAnimator transitionAnimator;

    @Inject
    Activity activity;

    public UISB(Context context, MyCustomDatabase fishyDb) {
        super(context);
    }

    @PostConstruct
    void apply() {
        transitionAnimator.setActivityContentView(testView);
    }

    @PreDestroy
    void destroy() {

    }
}

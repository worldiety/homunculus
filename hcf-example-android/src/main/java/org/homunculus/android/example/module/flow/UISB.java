package org.homunculus.android.example.module.flow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.widget.Button;
import android.widget.LinearLayout;

import org.homunculusframework.lang.Panic;
import org.homunculusframework.navigation.Navigation;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Torben Schinke on 07.11.17.
 */
@Named("/testa")
public class UISB extends LinearLayout {

    @Inject
    private Navigation navigation;

    @Inject
    private Activity activity;

    public UISB(Context context) {
        super(context);
    }

    @PostConstruct
    private void apply() {
        //throw new RuntimeException("test");
        setBackgroundColor(Color.BLACK);
        activity.setContentView(this);

        Button brokenView = new Button(getContext()) {
            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                throw new Panic("die hard");
            }
        };

        addView(brokenView);

    }

}

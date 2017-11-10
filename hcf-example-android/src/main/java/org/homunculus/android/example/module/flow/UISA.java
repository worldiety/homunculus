package org.homunculus.android.example.module.flow;

import android.content.Context;
import android.widget.LinearLayout;


import org.homunculus.android.component.NavigationBuilder;
import org.homunculus.android.example.R;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Torben Schinke on 07.11.17.
 */
@Named("/testa")
public class UISA extends LinearLayout {

    @Inject
    private NavigationBuilder navigation;

    public UISA(Context context) {
        super(context);
    }

    @PostConstruct
    private void apply() {
        throw new RuntimeException("test");
    }
}

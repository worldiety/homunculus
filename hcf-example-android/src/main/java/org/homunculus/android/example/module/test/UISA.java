package org.homunculus.android.example.module.test;

import android.content.Context;
import android.widget.LinearLayout;


import javax.annotation.PostConstruct;
import javax.inject.Named;

/**
 * Created by Torben Schinke on 07.11.17.
 */
@Named("/testa")
public class UISA extends LinearLayout {

    public UISA(Context context) {
        super(context);
    }

    @PostConstruct
    private void apply() {

    }
}

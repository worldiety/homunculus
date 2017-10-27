package org.homunculus.android.example.module.cart;

import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.homunculusframework.navigation.Navigation;

public class SideMenuView extends LinearLayout {


    public SideMenuView(Context context, Navigation navigation) {
        super(context);

        addEntry("home");
        addEntry("imprint");
    }

    private void addEntry(String cap) {
        TextView tv = new TextView(getContext());
        tv.setText(cap);
        addView(tv, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
}


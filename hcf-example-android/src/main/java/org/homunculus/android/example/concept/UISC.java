package org.homunculus.android.example.concept;

import android.app.Activity;
import android.view.View;

import org.homunculus.android.component.module.toolbarbuilder.templates.DefaultToolbarConfiguration;
import org.homunculus.android.example.R;
import org.homunculusframework.factory.flavor.hcf.Bind;
import org.homunculusframework.navigation.Navigation;

import javax.annotation.PostConstruct;
import javax.inject.Inject;



/**
 * Created by Torben Schinke on 20.04.18.
 */

@Bind
public class UISC {

    @Inject
    DefaultToolbarConfiguration toolbar;

    @Inject
    Activity context;

    @Inject
    Navigation navigation;

    @PostConstruct
    void apply() {
        toolbar.setMenu(R.menu.testmenu2).onItemClick(R.id.entry5, menuItem -> {
            System.out.println("test 5");
            return true;
        });
        toolbar.setUpAction(() -> {
            navigation.backward();
        });
        context.setContentView(toolbar.createToolbar(new View(context)));
    }
}



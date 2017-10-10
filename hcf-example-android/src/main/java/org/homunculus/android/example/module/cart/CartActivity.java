package org.homunculus.android.example.module.cart;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import org.homunculus.android.core.Android;
import org.homunculus.android.example.R;
import org.homunculus.android.example.common.ViewWait;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.navigation.DefaultNavigation;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.scope.Scope;

public class CartActivity extends AppCompatActivity {

    private Scope mScope;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show a progress spinner while waiting for the asynchronous result
        setContentView(new ViewWait(this));

        //setup HCF
        Configuration cfg = Android.getConfiguration(this);
        cfg.add(CartController.class);
        cfg.add(CartView.class);

        //setup HCF container
        Container container = new Container(cfg);
        container.start();

        //get the current scope
        Scope activityScope = Android.getScope(this);

        //do some navigation, which replaces the content view itself. Note: what the target (behind the request mapping) does is undefined by intention
        Navigation nav = new DefaultNavigation(activityScope);
        nav.forward(new Request("/cart/list").put("id", 1234));
    }
}

package org.homunculus.android.example.module.cart;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.core.Android;
import org.homunculus.android.example.common.ViewWait;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.navigation.DefaultNavigation;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

public class CartActivity extends EventAppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show a progress spinner while waiting for the asynchronous result
        setContentView(new ViewWait(this));


        //get the current scope
        getScope().putNamedValue(Android.NAME_NAVIGATION, new DefaultNavigation(getScope()));
        //do some navigation, which replaces the content view itself. Note: what the target (behind the request mapping) does is undefined by intention
        Navigation nav = getScope().resolve(Navigation.class);
        nav.forward(new Request("/cart/list").put("id", 1234));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    public void onBackPressed() {
        Navigation navigation = getScope().resolve(Navigation.class);
        if (navigation != null) {
            if (!navigation.backward()) {
                super.onBackPressed();
            }
        } else {
            LoggerFactory.getLogger(getClass()).error("no navigation available");
            super.onBackPressed();
        }
    }
}

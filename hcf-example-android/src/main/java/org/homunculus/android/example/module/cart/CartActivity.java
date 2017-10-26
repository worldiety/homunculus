package org.homunculus.android.example.module.cart;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.core.Android;
import org.homunculus.android.example.common.ViewWait;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.navigation.DefaultNavigation;
import org.homunculusframework.navigation.Navigation;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;

public class CartActivity extends EventAppCompatActivity implements UncaughtExceptionHandler {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show a progress spinner while waiting for the asynchronous result
        setContentView(new ViewWait(this));


        //get the current scope
        getScope().put(Android.NAME_NAVIGATION, new DefaultNavigation(getScope()));
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

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        new Builder(this).setMessage("caught weired crash: " + throwable.toString()).create().show();
    }
}

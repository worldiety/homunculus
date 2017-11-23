package org.homunculus.android.example.module.cart;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.HomunculusActivity;
import org.homunculus.android.core.Android;
import org.homunculus.android.example.common.ViewWait;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.navigation.DefaultNavigation;
import org.homunculusframework.navigation.Navigation;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;

public class CartActivity extends HomunculusActivity implements UncaughtExceptionHandler {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show a progress spinner while waiting for the asynchronous result
        setContentView(new ViewWait(this));
    }

    @Override
    protected Request create() {
        return new Request("/cart/list").put("id", 1234);
    }


    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        new Builder(this).setMessage("caught weired crash: " + throwable.toString()).create().show();
    }
}

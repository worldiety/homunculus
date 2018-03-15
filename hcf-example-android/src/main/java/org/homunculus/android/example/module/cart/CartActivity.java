package org.homunculus.android.example.module.cart;

import android.app.AlertDialog.Builder;
import android.os.Bundle;

import org.homunculus.android.component.HomunculusActivity;
import org.homunculus.android.example.common.ViewWait;
import org.homunculus.android.example.module.cart.AsyncCartController.BindCartControllerGetCart;
import org.homunculusframework.factory.container.Binding;

import java.lang.Thread.UncaughtExceptionHandler;

public class CartActivity extends HomunculusActivity implements UncaughtExceptionHandler {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show a progress spinner while waiting for the asynchronous result
        setContentView(new ViewWait(this));
    }

    @Override
    protected Binding<?> create() {
        return new BindCartControllerGetCart(1234);
    }


    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        new Builder(this).setMessage("caught weired crash: " + throwable.toString()).create().show();
    }
}

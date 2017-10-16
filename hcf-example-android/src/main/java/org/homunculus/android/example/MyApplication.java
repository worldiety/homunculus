package org.homunculus.android.example;

import android.app.Application;
import org.homunculus.android.core.Android;
import org.homunculus.android.example.module.cart.CartController;
import org.homunculus.android.example.module.cart.CartControllerConnection;
import org.homunculus.android.example.module.cart.CartUIS;
import org.homunculus.android.example.module.cart.CartView;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.flavor.ee.EEFlavor;
import org.homunculusframework.factory.flavor.spring.SpringFlavor;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //configure HCF for Android
        Configuration cfg = Android.getConfiguration(this);
        new EEFlavor().apply(cfg);

        //add each module (== controllers + views), order is unimportant
        cfg.add(CartController.class);
        cfg.add(CartControllerConnection.class);
        cfg.add(CartView.class);
        cfg.add(CartUIS.class);


        //setup and start the HCF container
        Container container = new Container(cfg);
        container.start();
    }
}

package org.homunculus.android.example.module.cart;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import org.homunculus.android.core.Android;
import org.homunculus.android.example.common.ViewWait;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

public class CartActivity extends AppCompatActivity {

    private Scope mScope;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show a progress spinner while waiting for the asynchronous result
        setContentView(new ViewWait(this));


        //get the current scope

        //do some navigation, which replaces the content view itself. Note: what the target (behind the request mapping) does is undefined by intention
        Navigation nav = Android.getScope(this).resolve(Navigation.class);
        nav.forward(new Request("/cart/list").put("id", 1234));
    }

    @Override
    public void onBackPressed() {
        Navigation navigation = Android.getScope(this).resolve(Navigation.class);
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

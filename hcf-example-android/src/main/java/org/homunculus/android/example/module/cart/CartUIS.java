package org.homunculus.android.example.module.cart;

import android.app.Activity;
import android.content.Context;
import org.homunculusframework.factory.annotation.Autowired;
import org.homunculusframework.factory.annotation.PostConstruct;
import org.homunculusframework.factory.annotation.Widget;

@Widget("/cart/uis/list")
public class CartUIS extends CartView {

    @Autowired
    private Activity mActivity;

    public CartUIS(Context context) {
        super(context);
    }


    @PostConstruct(order = 1) //TODO this looks weired -> super methods with the same order should have higher priority
    private void setViewInActivity() {
        mActivity.setContentView(this);
    }
}

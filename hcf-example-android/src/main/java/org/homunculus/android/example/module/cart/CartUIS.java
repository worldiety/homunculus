package org.homunculus.android.example.module.cart;

import android.app.Activity;
import android.content.Context;
import org.homunculusframework.factory.annotation.Priority;
import org.homunculusframework.factory.annotation.Widget;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Widget("/cart/uis/list")
public class CartUIS extends CartView {

    @Inject
    private Activity mActivity;

    public CartUIS(Context context) {
        super(context);
    }


    @PostConstruct()
    @Priority(10)//TODO this looks weired -> super methods with the same order should have higher priority
    private void setViewInActivity() {
        mActivity.setContentView(this);
    }
}

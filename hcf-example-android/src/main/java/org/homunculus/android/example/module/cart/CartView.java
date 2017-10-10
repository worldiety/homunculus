package org.homunculus.android.example.module.cart;

import android.app.Activity;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.homunculus.android.example.module.cart.CartModel.CartEntry;
import org.homunculusframework.factory.annotation.Autowired;
import org.homunculusframework.factory.annotation.PostConstruct;
import org.homunculusframework.factory.annotation.Widget;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

@Widget("/cart/view/list")
public class CartView extends LinearLayout {

    @Autowired("cart")
    private CartModel mCart;



    public CartView(Context context) {
        super(context);
    }

    //TODO this makes this view hard to reuse, because it destroys the context which it should not do??? or would we extend it as a "UIS"?
    @PostConstruct
    private void init() {
        removeAllViews();
        if (mCart == null) {
            return;
        }
        setOrientation(VERTICAL);
        TextView title = new TextView(getContext());
        title.setText("cart no " + mCart.getId());
        addView(title, new LayoutParams(MATCH_PARENT, LayoutParams.WRAP_CONTENT));


        for (CartEntry entry : mCart.getEntries()) {
            TextView eT = new TextView(getContext());
            eT.setText(entry.getName());
            addView(title, new LayoutParams(MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }
}

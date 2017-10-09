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

    @Autowired
    private Activity mActivity;

    public CartView(Context context) {
        super(context);
    }

    @PostConstruct
    private void setViewInActivity() {
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

        mActivity.setContentView(this);
    }
}

package org.homunculus.android.example.module.cart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.homunculus.android.annotation.Resource;
import org.homunculus.android.example.R;
import org.homunculus.android.example.module.cart.CartModel.CartEntry;
import org.homunculusframework.factory.annotation.Widget;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.navigation.Navigation;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

@Widget("/cart/view/list")
public class CartView extends LinearLayout {

    @Inject
    @Named("cart")
    private CartModel mCart;

    @Resource(R.string.app_name)
    private String mMsg;

    @Resource(R.drawable.icon)
    private Drawable mIcon1;

    @Resource(R.drawable.icon)
    private Bitmap mIcon2;

    @Inject
    private Navigation mNav;


    public CartView(Context context) {
        super(context);
    }

    @PostConstruct
    private void init() {
        removeAllViews();
        if (mCart == null) {
            return;
        }
        setOrientation(VERTICAL);
        TextView title = new TextView(getContext());
        title.setText("cart no " + mCart.getId() + " from " + mMsg);
        addView(title, new LayoutParams(MATCH_PARENT, LayoutParams.WRAP_CONTENT));


        for (CartEntry entry : mCart.getEntries()) {
            TextView eT = new TextView(getContext());
            eT.setText(entry.getName());
            addView(eT, new LayoutParams(MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }

        Button button = new Button(getContext());
        button.setText("next");
        button.setOnClickListener(view -> {
            mNav.forward(new Request("/cart/list").put("id", "77777"));
        });
        addView(button, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
    }
}

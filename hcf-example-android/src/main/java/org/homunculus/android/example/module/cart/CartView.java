package org.homunculus.android.example.module.cart;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.homunculus.android.compat.Permissions;
import org.homunculus.android.example.module.company.CompanyController;
import org.homunculus.android.flavor.Resource;
import org.homunculus.android.example.R;
import org.homunculusframework.factory.flavor.hcf.Persistent;
import org.homunculusframework.factory.flavor.hcf.Widget;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.navigation.Navigation;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.Serializable;
import java.util.UUID;

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

    @Inject
    private CartControllerConnection mCartController;

    @Inject
    private CompanyController company;

    @Persistent
    private SomeSettings settings;

    @Inject
    private Permissions permissions;

    public CartView(Context context) {
        super(context);
    }

    @PostConstruct
    private void init() {
        LoggerFactory.getLogger(getClass()).info("{}", settings);
        settings.value = UUID.randomUUID().toString();

        removeAllViews();
        if (mCart == null) {
            LoggerFactory.getLogger(getClass()).info("cart is empty");
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
        button.setText("/cart/list");
        button.setOnClickListener(view -> {
            mNav.forward(new Request("/cart/list").put("id", "111111"));
        });
        addView(button, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));


        Button button5 = new Button(getContext());
        button5.setText("/cart/list2");
        button5.setOnClickListener(view -> {
            mNav.forward(new Request("/cart/list2").put("id", "222222"));
        });
        addView(button5, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));


        Button button2 = new Button(getContext());
        button2.setText("direct invocation");
        button2.setOnClickListener(view -> {
            mCartController.getPoJoCart(4566).whenDone(res -> {
                res.log();
                Builder dlg = new Builder(getContext());
                dlg.setMessage("the result is: " + res);
                dlg.create().show();
            });
        });
        addView(button2, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));


        Button button3 = new Button(getContext());
        button3.setText("non-async (not recommended)");
        button3.setOnClickListener(view -> {
            company.testLoad();
        });
        addView(button3, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        Button button4 = new Button(getContext());
        button4.setText("request UIS");
        button4.setOnClickListener(view -> {
            CartModel model = new CartModel();
            model.setId(7338);
            model.getEntries().add(new CartEntry("direct UIS navigation"));
            mNav.forward(new Request("/cart/uis/list").put("cart", model));
        });
        addView(button4, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        Button button6 = new Button(getContext());
        button6.setText("request unconfigured");
        button6.setOnClickListener(view -> {
            mNav.forward(new Request("/notconfigured/widget/or/controller"));
        });
        addView(button6, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));


        Button crashBackground = new Button(getContext());
        crashBackground.setText("crash in background");
        crashBackground.setOnClickListener(view -> {
            new Thread() {
                @Override
                public void run() {
                    throw new RuntimeException("the thread died");
                }
            }.start();
        });
        addView(crashBackground, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));


        Button crashMain = new Button(getContext());
        crashMain.setText("crash in main");
        crashMain.setOnClickListener(view -> {
            throw new RuntimeException("the main thread is dead, or not?");
        });
        addView(crashMain, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));


    }

    public static class SomeSettings implements Serializable {
        private String value;

        @Override
        public String toString() {
            return value;
        }
    }
}

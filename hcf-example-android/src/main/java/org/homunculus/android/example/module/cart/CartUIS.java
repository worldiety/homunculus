package org.homunculus.android.example.module.cart;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.compat.Str;
import org.homunculus.android.compat.ToolbarBuilder;
import org.homunculus.android.example.R;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.factory.flavor.hcf.Priority;
import org.homunculusframework.factory.flavor.hcf.Widget;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.scope.Scope;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Widget("/cart/uis/list")
public class CartUIS extends CartView {

    @Inject
    private EventAppCompatActivity mActivity;

    @Inject
    private Navigation mNav;

    @Inject
    private Scope scope;

    public CartUIS(Context context) {
        super(context);
    }


    @PostConstruct()
    @Priority(10)//TODO this looks weired -> super methods with the same order should have higher priority
    private void setViewInActivity() {
//        mActivity.setContentView(this);

        View content = ToolbarBuilder.define()
                .setTitle(Str.str(R.string.app_name))
                .setTitleTextColor(R.color.colorAccent)
                .setToolbarColor(R.color.colorPrimary)
                .setShowNavigationIcons(true)
                .setMenuId(R.menu.testmenu)
                .addMenuItemListener(R.id.entry1, menuItem -> {
                    mNav.forward(new Request("/cart/uis/list"));
                    return true;
                })
                .addMenuItemListener(R.id.entry2, menuItem -> true)
                .addMenuItemListener(R.id.entry3, menuItem -> {
                    mNav.forward(new Request("/cart/uis/list2"));
                    return true;
                })
                .create(scope, mActivity, this, new SideMenuView(getContext(), mNav));
        mActivity.setContentView(content);
    }


}

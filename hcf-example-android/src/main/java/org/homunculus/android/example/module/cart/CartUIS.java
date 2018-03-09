package org.homunculus.android.example.module.cart;

import android.content.Context;
import android.support.transition.Explode;
import android.support.transition.Slide;
import android.view.Gravity;
import android.view.ViewGroup;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.ViewTransitionAnimator;
import org.homunculus.android.component.ToolbarBuilder;
import org.homunculus.android.example.R;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.factory.flavor.hcf.Priority;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.scope.Scope;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import static org.homunculus.android.component.Str.str;

@Named("/cart/uis/list")
public class CartUIS extends CartView {

    @Inject
    private EventAppCompatActivity mActivity;

    @Inject
    private ViewTransitionAnimator viewTransitionAnimator;

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
        int x = R.layout.hcf_splash;
        ViewGroup content = ToolbarBuilder.define()
                .setTitle(str(R.string.app_name))
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
//                    mNav.forward(AsyncCartController);
                    return true;
                })
                .create(scope, mActivity, mActivity, this, new SideMenuView(getContext(), mNav));
        viewTransitionAnimator.setForwardTransition(new Explode());
        viewTransitionAnimator.setActivityContentView(content);
    }


}

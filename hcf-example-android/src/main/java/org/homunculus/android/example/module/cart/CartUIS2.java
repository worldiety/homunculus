package org.homunculus.android.example.module.cart;

import android.content.Context;
import android.view.View;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.Str;
import org.homunculus.android.component.ToolbarBuilder;
import org.homunculus.android.example.R;
import org.homunculusframework.factory.flavor.hcf.FactoryParam;
import org.homunculusframework.factory.flavor.hcf.Priority;
import org.homunculusframework.factory.flavor.hcf.ViewComponent;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.scope.Scope;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import static org.homunculus.android.component.Str.str;

@ViewComponent
@Named("/cart/uis/list2")
public class CartUIS2 extends CartView {

    @Inject
    private EventAppCompatActivity mActivity;

    @Inject
    private Navigation mNav;

    @Inject
    private Scope scope;


    public CartUIS2(Context context, @FactoryParam String fancyFlag) {
        super(context);
    }


    @PostConstruct()
    @Priority(10)//TODO this looks weired -> super methods with the same order should have higher priority
    private void setViewInActivity() {

        View content = ToolbarBuilder.define()
                .setTitle(str(R.string.app_name))
                .setTitleTextColor(R.color.colorAccent)
                .setToolbarColor(R.color.colorPrimary)
                .setShowNavigationIcons(true)
                //without menu
                .create(scope, mActivity, mActivity, this, new SideMenuView(getContext(), mNav));
        mActivity.setContentView(content);
    }


}

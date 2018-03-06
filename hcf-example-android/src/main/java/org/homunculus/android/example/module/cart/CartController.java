package org.homunculus.android.example.module.cart;

import org.homunculus.android.example.module.cart.CartControllerConnection.BindCartUIS;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.concurrent.ThreadNotInterruptible;
import org.homunculusframework.factory.container.MethodBinding;
import org.homunculusframework.factory.container.ObjectBinding;
import org.homunculusframework.factory.flavor.hcf.Execute;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.navigation.ModelAndView;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("/cart")
public class CartController {
    public final static String METHOD_LIST2 = "getMyList2";

    @PostConstruct
    @Execute(Container.NAME_BACKGROUND_HANDLER)
    private void init() {
        LoggerFactory.getLogger(getClass()).debug("init complete");
    }

    @Named("/list")
    public ModelAndView getCart(@Named("id") int cartId) throws InterruptedException {
        //do some expensive I/O work
        Thread.sleep(2000);

        CartModel cart = new CartModel();
        cart.setId(cartId);
        cart.getEntries().add(new CartEntry("The wiz in action"));
        cart.getEntries().add(new CartEntry("Jim, he is dead"));

        return new ModelAndView("/cart/uis/list").put("cart", cart);
    }

    @Named(METHOD_LIST2)
    public ModelAndView getCart2(@Named("id") int cartId) throws InterruptedException {
        //do some expensive I/O work
        Thread.sleep(2000);

        CartModel cart = new CartModel();
        cart.setId(cartId);
        cart.getEntries().add(new CartEntry("The wiz in action"));
        cart.getEntries().add(new CartEntry("Jim, he is dead"));

        return new ModelAndView("/cart/uis/list").put("cart", cart);
    }

    public ObjectBinding<?> getCart3(@Named("id") int cartId) throws InterruptedException {
        CartModel cart = new CartModel();
        return new BindCartUIS("blub", null, cart);
    }

    public org.homunculusframework.factory.container.ModelAndView getCart4(@Named("id") int cartId) throws InterruptedException {
        return new BindCartUIS2("ad", "asd", "ads", null);
    }

    /**
     * Requesting backend methods directly is possible as well, recommend is to use the {@link org.homunculusframework.factory.async.AsyncDelegate} pattern
     * as seen in {@link CartControllerConnection}
     */
    @ThreadNotInterruptible
    public CartModel getPoJoCart(int cartId) throws InterruptedException {
        //do some expensive I/O work
        Thread.sleep(2000);

        CartModel cart = new CartModel();
        cart.setId(cartId);
        cart.getEntries().add(new CartEntry("The wiz in action"));
        cart.getEntries().add(new CartEntry("Jim, he is dead"));
        if (true) {
            throw new IllegalArgumentException("yo man");
        }
        return cart;
    }

    public CartModel getPoJoCart2(int cartId, String x, SideMenuView notAllowed) throws InterruptedException {
        return null;
    }
}

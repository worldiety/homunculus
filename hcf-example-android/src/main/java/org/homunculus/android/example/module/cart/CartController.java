package org.homunculus.android.example.module.cart;

import org.homunculus.android.example.module.cart.CartModel.CartEntry;
import org.homunculusframework.factory.annotation.Execute;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.navigation.ModelAndView;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("/cart")
public class CartController {

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
}

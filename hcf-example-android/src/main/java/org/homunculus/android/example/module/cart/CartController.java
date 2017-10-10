package org.homunculus.android.example.module.cart;

import org.homunculus.android.example.module.cart.CartModel.CartEntry;
import org.homunculusframework.factory.annotation.Parameter;
import org.homunculusframework.factory.annotation.PostConstruct;
import org.homunculusframework.factory.annotation.RequestMapping;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.navigation.ModelAndView;
import org.homunculusframework.stereotype.Controller;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/cart")
public class CartController {

    @PostConstruct(in = Container.NAME_BACKGROUND_HANDLER)
    private void init() {
        LoggerFactory.getLogger(getClass()).debug("init complete");
    }

    @RequestMapping("/list")
    public ModelAndView getCart(@Parameter("id") int cartId) throws InterruptedException {
        //do some expensive I/O work
        Thread.sleep(2000);

        CartModel cart = new CartModel();
        cart.setId(cartId);
        cart.getEntries().add(new CartEntry());

        return new ModelAndView("/cart/view/").put("cart", cart);
    }
}

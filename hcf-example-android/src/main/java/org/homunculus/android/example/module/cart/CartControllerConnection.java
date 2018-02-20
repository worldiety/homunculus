package org.homunculus.android.example.module.cart;


import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.async.AsyncDelegate;
import org.homunculusframework.lang.Result;


public class CartControllerConnection extends AsyncDelegate<CartController> {


    public Task<Result<CartModel>> getPoJoCart(int cartId) {
        return async( ctr -> ctr.getPoJoCart(cartId));
    }


}

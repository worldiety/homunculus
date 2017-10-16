package org.homunculus.android.example.module.cart;


import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.connection.Connection;
import org.homunculusframework.lang.Result;


public interface CartControllerConnection extends Connection<CartController> {

    /**
     * Automatically resolves to {@link CartController#getPoJoCart(int)}
     */
    Task<Result<CartModel>> getPoJoCart(int cartId);
}

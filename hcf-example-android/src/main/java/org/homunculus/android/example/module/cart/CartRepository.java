package org.homunculus.android.example.module.cart;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

@Singleton
@Named("/cart")
public class CartRepository {

    @Named
    private EntityManager em;

    public CartModel load() {

        return null;
    }
}

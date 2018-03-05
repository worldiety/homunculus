package org.homunculus.android.example.module.cart;


import android.content.Context;

import org.homunculusframework.concurrent.Async;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.async.AsyncDelegate;
import org.homunculusframework.factory.container.MethodBinding;
import org.homunculusframework.factory.container.ObjectBinding;
import org.homunculusframework.lang.Reflection;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;

import java.lang.reflect.Field;
import java.util.Map;

import javax.annotation.Nullable;


public class CartControllerConnection extends AsyncDelegate<CartController> {


    public Task<Result<CartModel>> getPoJoCart(int cartId) {
        return async(ctr -> ctr.getPoJoCart(cartId));
    }


    public static class BindCartUIS extends ObjectBinding<CartUIS> {

        private static Field fieldSomeString;
        private static Field fieldSomeOptionalString;
        private static Field fieldCartModel;

        private String someString;
        private String someOptionalString;
        private CartModel cartModel;

        public BindCartUIS(String someString, @Nullable String someOptionalString, CartModel cartModel) {
            this.someString = someString;
            this.someOptionalString = someOptionalString;
            this.cartModel = cartModel;
        }

        @Override
        protected void initStatic() {
            Map<String, Field> fields = Reflection.getFieldsMap(CartUIS.class);
            fieldSomeString = fields.get("someString");
            fieldSomeOptionalString = fields.get("someOptionalString");
            fieldCartModel = fields.get("cartModel");
        }

        @Override
        protected void onBind(Scope dst) {
            dst.put("someString", someString);
            dst.put("someOptionalString", someOptionalString);
            dst.put("cartModel", cartModel);
        }

        @Nullable
        @Override
        protected CartUIS onExecute() throws Exception {
            CartUIS obj = new CartUIS(get(Context.class));
            fieldSomeString.set(obj, get("someString", String.class));
            fieldSomeOptionalString.set(obj, get("someString", String.class));
            fieldCartModel.set(obj, get("cartModel", CartModel.class));
            return obj;
        }
    }


    public static class BindCartControllerGetCart2 extends MethodBinding<Object> {

        private int cartId;

        public BindCartControllerGetCart2(int cartId) {
            this.cartId = cartId;
        }

        @Override
        protected void onBind(Scope dst) {
            dst.put("cartId", cartId);
        }

        @Override
        protected ObjectBinding<Object> onExecute() throws Exception {
            CartController ctr = get(CartController.class);
            assertNotNull(CartController.class, ctr);
            return (ObjectBinding<Object>) (ObjectBinding<?>) ctr.getCart3(cartId);
        }

    }


    public static class Singletons {

        private final Scope scope;
        private final CartController cartController;


        public Singletons(Scope scope) {
            this.scope = scope;

            this.cartController = Async.await(new BindCartController().execute(scope)).unwrap();
            this.scope.put("cartController", cartController);
        }


        public static class BindCartController extends ObjectBinding<CartController> {

            @Override
            protected void initStatic() {

            }

            @Nullable
            @Override
            protected CartController onExecute() throws Exception {
                return null;
            }

            @Override
            protected void onBind(Scope dst) {

            }
        }
    }
}

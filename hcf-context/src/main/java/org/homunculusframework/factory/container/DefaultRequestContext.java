package org.homunculusframework.factory.container;

import org.homunculusframework.navigation.Navigation;

import java.util.List;

/**
 * This implementation leaks the stack of the given navigation. This is intentional, so that a controller may
 * modify the stack of the requesting scope.
 * <p>
 * Created by Torben Schinke on 26.04.18.
 */
public class DefaultRequestContext implements RequestContext {

    private List<Binding<?, ?>> reference;
    private volatile boolean cancelled;

    public DefaultRequestContext(Navigation navigation) {
        reference = navigation.getStack();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

    @Override
    public List<Binding<?, ?>> getReferrer() {
        return reference;
    }
}

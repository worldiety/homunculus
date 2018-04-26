package org.homunculusframework.factory.container;

import org.homunculusframework.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Torben Schinke on 26.04.18.
 */

public class DefaultRequestContext implements RequestContext {

    private List<Binding<?, ?>> stackSnapshot;
    private volatile boolean cancelled;

    public DefaultRequestContext(Navigation navigation) {
        stackSnapshot = new ArrayList<>(navigation.getStack());
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
        return stackSnapshot;
    }
}

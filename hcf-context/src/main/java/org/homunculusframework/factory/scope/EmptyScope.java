package org.homunculusframework.factory.scope;

import javax.annotation.Nullable;

public class EmptyScope extends AbsScope {

    @Nullable
    @Override
    public Scope getParent() {
        return null;
    }

    @Nullable
    @Override
    public <T> T resolve(Class<T> type) {
        return null;
    }
}
package org.homunculusframework.factory;

import org.homunculusframework.scope.Scope;

import java.util.List;

public interface ProcessingCompleteCallback {
    void onComplete(Scope scope, Object instance, List<Throwable> failures);
}

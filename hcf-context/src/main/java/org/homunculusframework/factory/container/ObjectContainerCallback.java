package org.homunculusframework.factory.container;

import org.homunculusframework.lang.Result;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by Torben Schinke on 14.03.18.
 */

public interface ObjectContainerCallback {

    /**
     * @param instances the instances of the container
     * @param t         the throwable
     */
    void onObjectContainerCompleted(ObjectContainer container, @Nullable List<Result<Object>> instances, @Nullable Throwable t);
}

package org.homunculus.android.component.module.validator;

import android.view.View;

import org.homunculusframework.annotations.Unfinished;

import java.lang.reflect.Field;

/**
 * Abstract class, which is used by {@link ModelViewPopulator} to fill a {@link View} with information or get information from the {@link View}
 * <p>
 * Created by aerlemann on 15.02.18.
 */
@Unfinished
public abstract class ValidatorViewConnector {
    /**
     * Sets an error text to a given {@link View}
     *
     * @param dst                 the destination {@link View} supported by this connector
     * @param error               the error string, which is to be set to dst
     */
    protected abstract void setErrorToView(View dst, String error);
}

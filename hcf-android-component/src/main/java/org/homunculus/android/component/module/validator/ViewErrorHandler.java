package org.homunculus.android.component.module.validator;

import android.view.View;

import org.homunculusframework.annotations.Unfinished;

/**
 * Interface, which is used by {@link ModelViewPopulator} to fill a specific {@link View} with error-information
 * <p>
 * Created by aerlemann on 15.02.18.
 */
@Unfinished
public interface ViewErrorHandler<T extends View> {
    /**
     * Sets an error text to a given {@link View}
     *
     * @param dst   the destination {@link View} supported by this connector
     * @param error the error string, which is to be set to dst
     */
    void setErrorToView(T dst, String error);
}

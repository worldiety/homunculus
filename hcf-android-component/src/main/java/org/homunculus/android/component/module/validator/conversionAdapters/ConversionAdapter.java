package org.homunculus.android.component.module.validator.conversionAdapters;

import android.view.View;

import org.homunculus.android.component.module.validator.ViewErrorHandler;
import org.homunculusframework.annotations.Unfinished;

/**
 * An adapter for use in {@link org.homunculus.android.component.module.validator.ModelViewPopulator}. It sets a value from type F
 * to a {@link View} from type V
 * <p>
 * Created by aerlemann on 16.02.18.
 */
@Unfinished
public interface ConversionAdapter<V extends View, F> {

    /**
     * Sets the a value to a {@link View}
     *
     * @param value value of type F
     * @param view  {@link View} of type V
     */
    void setFieldValueToView(F value, V view);

    /**
     * Gets a value from a {@link View}
     *
     * @param view {@link View} of type V
     * @return value of type F
     */
    F getFieldValueFromView(V view);

    /**
     * Gets the {@link ViewErrorHandler} for a {@link View} from type V
     *
     * @return
     */
    ViewErrorHandler<V> getErrorHandler();
}

package org.homunculus.android.component.module.validator;

import android.view.View;

import org.homunculusframework.annotations.Unfinished;

import java.lang.reflect.Field;

/**
 * Abstract class, which is used by {@link ModelViewPopulator<T>} to fill a {@link View} with information or get information from the {@link View}
 * <p>
 * Created by aerlemann on 15.02.18.
 */
@Unfinished
public abstract class ValidatorViewConnector<T> {
    /**
     * Checks, if a given {@link View} is an instance of the view supported by this connector
     *
     * @param view a {@link View}
     * @return true, if the {@link View} is supported by this connector, else false
     */
    protected abstract boolean isViewOfThisKind(View view);


    /**
     * Set the value of a field (field) of a given object (src) to a destination {@link View} (dst)
     *
     * @param dst                 the destination {@link View} supported by this connector
     * @param field               the field, which information is to be filled into dst
     * @param src                 the object, which contains the field
     * @param fieldValueAdapter
     */
    void setFieldValueToSpecificView(View dst, Field field, T src, FieldValueAdapter<T> fieldValueAdapter) {
        setTextToView(dst, fieldValueAdapter.getField(field, src));
    }

    /**
     * Sets the value of a {@link View} (src) to a field (field) of a given object (dst)
     *
     * @param src                 the source {@link View} supported by this connector
     * @param field               the field, which information is to be filled from src
     * @param dst                 the object, which contains the field
     * @param fieldValueAdapter
     */
    void setViewValueToField(View src, Field field, T dst, FieldValueAdapter<T> fieldValueAdapter) {
        fieldValueAdapter.setField(getTextFromView(src), field, dst);
    }

    /**
     * Sets an error text to a given {@link View}
     *
     * @param dst                 the destination {@link View} supported by this connector
     * @param error               the error string, which is to be set to dst
     * @param fieldValueAdapter
     */
    protected abstract void setErrorToView(View dst, String error, FieldValueAdapter<T> fieldValueAdapter);

    /**
     * Gets a String out of a given {@link View}
     *
     * @param view the {@link View} supported by this connector
     * @return the String from the {@link View} or null
     */
    protected abstract String getTextFromView(View view);

    /**
     * Sets a String to a given {@link View}
     *
     * @param view the {@link View} supported by this connector
     * @param text the text or null to be set to the view
     */
    protected abstract void setTextToView(View view, String text);
}

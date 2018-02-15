package org.homunculus.android.component.module.validator;

import android.view.View;

import org.homunculusframework.annotations.Unfinished;

import java.lang.reflect.Field;

/**
 * Interface, which is used by {@link ModelViewPopulator<T>} to fill a {@link View} with information or get information from the {@link View}
 * <p>
 * Created by aerlemann on 15.02.18.
 */
@Unfinished
public interface ValidatorViewConnector<T> {
    /**
     * Checks, if a given {@link View} is an instance of the view supported by this connector
     *
     * @param view a {@link View}
     * @return true, if the {@link View} is supported by this connector, else false
     */
    boolean isViewOfThisKind(View view);


    /**
     * Set the value of a field (field) of a given object (src) to a destination {@link View} (dst)
     *
     * @param dst   the destination {@link View} supported by this connector
     * @param field the field, which information is to be filled into dst
     * @param src   the object, which contains the field
     */
    void setFieldValueToSpecificView(View dst, Field field, T src);

    /**
     * Sets the value of a {@link View} (src) to a field (field) of a given object (dst)
     *
     * @param src   the source {@link View} supported by this connector
     * @param field the field, which information is to be filled from src
     * @param dst   the object, which contains the field
     */
    void setViewValueToField(View src, Field field, T dst);

    /**
     * Sets an error text to a given {@link View}
     *
     * @param dst   the destination {@link View} supported by this connector
     * @param error the error string, which is to be set to dst
     */
    void setErrorToView(View dst, String error);
}

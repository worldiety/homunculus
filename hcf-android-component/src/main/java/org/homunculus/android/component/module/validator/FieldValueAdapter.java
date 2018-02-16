package org.homunculus.android.component.module.validator;

import java.lang.reflect.Field;

/**
 * Interface used to convert a String representation from the UI to a field from the model or the other way around
 * <p>
 * Created by aerlemann on 16.02.18.
 */

public interface FieldValueAdapter<T> {

    /**
     * Return true, if field type is supported by this adapter
     *
     * @param field  the field to be converted
     * @param object the object containing the field
     * @return true, if field type is supported by this adapter
     */
    boolean isFieldTypeSupported(Field field, T object);

    /**
     * Converts the field value from an object to a human-readable string representation for showing in the UI
     *
     * @param field the field to be converted
     * @param src   the object containing the field
     * @return a String representation of the field value from an object
     */
    String getField(Field field, T src);

    /**
     * Converts the human-readable string representation from the UI into a field value from an object
     *
     * @param text  the text to be converted
     * @param field the field to be converted
     * @param dst   the object containing the field
     */
    void setField(String text, Field field, T dst);
}

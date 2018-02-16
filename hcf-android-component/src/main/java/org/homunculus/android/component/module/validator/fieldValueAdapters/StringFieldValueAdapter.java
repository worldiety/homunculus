package org.homunculus.android.component.module.validator.fieldValueAdapters;

import org.homunculus.android.component.module.validator.FieldValueAdapter;

import java.lang.reflect.Field;

/**
 * {@link FieldValueAdapter<T>} for Strings
 * <p>
 * Created by aerlemann on 16.02.18.
 */

public class StringFieldValueAdapter<T> implements FieldValueAdapter<T> {

    @Override
    public boolean isFieldTypeSupported(Field field, T object) {
        return field.getType() == String.class;
    }

    @Override
    public String getField(Field field, T src) {
        try {
            return (String) field.get(src);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setField(String text, Field field, T dst) {
        try {
            field.set(dst, text);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

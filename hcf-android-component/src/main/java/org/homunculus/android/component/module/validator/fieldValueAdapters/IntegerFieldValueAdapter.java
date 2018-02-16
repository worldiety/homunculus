package org.homunculus.android.component.module.validator.fieldValueAdapters;

import org.homunculus.android.component.module.validator.FieldValueAdapter;

import java.lang.reflect.Field;

/**
 * {@link FieldValueAdapter<T>} for ints and Integers
 *
 * Created by aerlemann on 16.02.18.
 */

public class IntegerFieldValueAdapter<T> implements FieldValueAdapter<T> {

    @Override
    public boolean isFieldTypeSupported(Field field, T object) {
        try {
            field.getInt(object);
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    @Override
    public String getField(Field field, T src) {
        try {
            return "" + field.getInt(src);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setField(String text, Field field, T dst) {
        try {
            field.setInt(dst, Integer.valueOf(text));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

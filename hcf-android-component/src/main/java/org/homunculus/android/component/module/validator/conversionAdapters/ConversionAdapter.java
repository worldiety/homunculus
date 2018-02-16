package org.homunculus.android.component.module.validator.conversionAdapters;

import android.view.View;

import org.homunculusframework.annotations.Unfinished;

import java.lang.reflect.Field;

/**
 * Created by aerlemann on 16.02.18.
 */
@Unfinished
public abstract class ConversionAdapter<V extends View, F, M> {

    public void transferFieldToView(Field field, V view, M model) {
        setFieldValueToView(getFieldValue(field, model), view);
    }

    public void transferViewToField(V view, Field field, M model) {
        setField(getFieldValueFromView(view), field, model);
    }

    public boolean isFieldTypeSupported(Field field, M model) {
        try {
            F value = (F) field.get(model);
            return isFieldTypeSupported(value);
        } catch (IllegalAccessException | ClassCastException e) {
            return false;
        }
    }

    abstract boolean isFieldTypeSupported(F fieldType);

    abstract void setFieldValueToView(F value, V view);

    abstract F getFieldValueFromView(V view);

    private F getFieldValue(Field field, M src) {
        try {
            return (F) field.get(src);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(F text, Field field, M dst) {
        try {
            field.set(dst, text);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

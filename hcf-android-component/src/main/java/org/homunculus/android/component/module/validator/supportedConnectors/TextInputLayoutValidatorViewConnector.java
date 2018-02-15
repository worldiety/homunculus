package org.homunculus.android.component.module.validator.supportedConnectors;

import android.support.design.widget.TextInputLayout;
import android.view.View;

import org.homunculus.android.component.module.validator.ValidatorViewConnector;

import java.lang.reflect.Field;

/**
 * {@link ValidatorViewConnector<T> for {@link TextInputLayout}}
 * <p>
 * Created by aerlemann on 15.02.18.
 */

public class TextInputLayoutValidatorViewConnector<T> implements ValidatorViewConnector<T> {
    @Override
    public boolean isViewOfThisKind(View view) {
        try {
            return view instanceof TextInputLayout;
        } catch (NoClassDefFoundError e) {
            //dependency missing, so nothing to worry about
            return false;
        }
    }

    @Override
    public void setFieldValueToSpecificView(View dst, Field field, T src) {
        ((TextInputLayout) dst).getEditText().setText(getField(field, src));
    }

    @Override
    public void setViewValueToField(View src, Field field, T dst) {
        setField(((TextInputLayout) src).getEditText().getText().toString(), field, dst);
    }

    @Override
    public void setErrorToView(View dst, String error) {
        ((TextInputLayout) dst).setError(error);
    }

    private CharSequence getField(Field field, T src) {
        try {
            return (CharSequence) field.get(src);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(String text, Field field, T dst) {
        try {
            field.set(dst, text);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

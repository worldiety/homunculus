package org.homunculus.android.component.module.validator.supportedConnectors;

import android.view.View;
import android.widget.EditText;

import org.homunculus.android.component.module.validator.ValidatorViewConnector;

import java.lang.reflect.Field;

/**
 * {@link ValidatorViewConnector<T> for {@link EditText}}
 *
 * Created by aerlemann on 15.02.18.
 */

public class EditTextValidatorViewConnector<T> implements ValidatorViewConnector<T> {
    @Override
    public boolean isViewOfThisKind(View view) {
        return view instanceof EditText;
    }

    @Override
    public void setFieldValueToSpecificView(View dst, Field field, T src) {
        ((EditText) dst).setText(getField(field, src));
    }

    @Override
    public void setViewValueToField(View src, Field field, T dst) {
        setField(((EditText) src).getText().toString(), field, dst);
    }

    @Override
    public void setErrorToView(View dst, String error) {
        ((EditText) dst).setError(error);
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

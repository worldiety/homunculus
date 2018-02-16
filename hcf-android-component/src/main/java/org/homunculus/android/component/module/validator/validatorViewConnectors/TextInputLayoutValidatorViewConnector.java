package org.homunculus.android.component.module.validator.validatorViewConnectors;

import android.support.design.widget.TextInputLayout;
import android.view.View;

import org.homunculus.android.component.module.validator.FieldValueAdapter;
import org.homunculus.android.component.module.validator.ValidatorViewConnector;

/**
 * {@link ValidatorViewConnector<T>} for {@link TextInputLayout}
 * <p>
 * Created by aerlemann on 15.02.18.
 */

public class TextInputLayoutValidatorViewConnector<T> extends ValidatorViewConnector<T> {
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
    public void setErrorToView(View dst, String error, FieldValueAdapter<T> fieldValueAdapter) {
        ((TextInputLayout) dst).setError(error);
    }

    @Override
    protected String getTextFromView(View view) {
        return ((TextInputLayout) view).getEditText().getText().toString();
    }

    @Override
    protected void setTextToView(View view, String text) {
        ((TextInputLayout) view).getEditText().setText(text);
    }
}

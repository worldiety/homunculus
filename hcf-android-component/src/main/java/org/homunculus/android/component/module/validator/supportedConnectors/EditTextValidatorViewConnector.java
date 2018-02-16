package org.homunculus.android.component.module.validator.supportedConnectors;

import android.view.View;
import android.widget.EditText;

import org.homunculus.android.component.module.validator.ValidatorViewConnector;

/**
 * {@link ValidatorViewConnector<T> for {@link EditText}}
 * <p>
 * Created by aerlemann on 15.02.18.
 */

public class EditTextValidatorViewConnector<T> extends ValidatorViewConnector<T> {
    @Override
    public boolean isViewOfThisKind(View view) {
        return view instanceof EditText;
    }

    @Override
    public void setErrorToView(View dst, String error) {
        ((EditText) dst).setError(error);
    }

    @Override
    protected String getTextFromView(View view) {
        return ((EditText) view).getText().toString();
    }

    @Override
    protected void setTextToView(View view, String text) {
        ((EditText) view).setText(text);
    }
}

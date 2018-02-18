package org.homunculus.android.component.module.validator.validatorViewConnectors;

import android.view.View;
import android.widget.EditText;

import org.homunculus.android.component.module.validator.ValidatorViewConnector;

/**
 * {@link ValidatorViewConnector} for {@link EditText}
 * <p>
 * Created by aerlemann on 15.02.18.
 */

public class EditTextValidatorViewConnector extends ValidatorViewConnector {
    @Override
    public void setErrorToView(View dst, String error) {
        ((EditText) dst).setError(error);
    }
}

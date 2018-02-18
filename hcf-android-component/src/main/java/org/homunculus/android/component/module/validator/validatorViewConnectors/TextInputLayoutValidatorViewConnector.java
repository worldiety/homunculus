package org.homunculus.android.component.module.validator.validatorViewConnectors;

import android.support.design.widget.TextInputLayout;
import android.view.View;
import org.homunculus.android.component.module.validator.ValidatorViewConnector;

/**
 * {@link ValidatorViewConnector} for {@link TextInputLayout}
 * <p>
 * Created by aerlemann on 15.02.18.
 */

public class TextInputLayoutValidatorViewConnector extends ValidatorViewConnector {
    @Override
    public void setErrorToView(View dst, String error) {
        ((TextInputLayout) dst).setError(error);
    }
}

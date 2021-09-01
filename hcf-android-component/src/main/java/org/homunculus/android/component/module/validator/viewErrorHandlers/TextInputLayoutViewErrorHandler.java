package org.homunculus.android.component.module.validator.viewErrorHandlers;

import com.google.android.material.textfield.TextInputLayout;

import org.homunculus.android.component.module.validator.ViewErrorHandler;

/**
 * {@link ViewErrorHandler} for {@link TextInputLayout}
 * <p>
 * Created by aerlemann on 15.02.18.
 */

public class TextInputLayoutViewErrorHandler implements ViewErrorHandler<TextInputLayout> {
    @Override
    public void setErrorToView(TextInputLayout dst, String error) {
        dst.setError(error);
    }
}

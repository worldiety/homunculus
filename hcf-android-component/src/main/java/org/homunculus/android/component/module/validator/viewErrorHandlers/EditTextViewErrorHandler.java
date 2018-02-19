package org.homunculus.android.component.module.validator.viewErrorHandlers;

import android.widget.EditText;

import org.homunculus.android.component.module.validator.ViewErrorHandler;

/**
 * {@link ViewErrorHandler} for {@link EditText}
 * <p>
 * Created by aerlemann on 15.02.18.
 */

public class EditTextViewErrorHandler extends ViewErrorHandler<EditText> {
    @Override
    public void setErrorToView(EditText dst, String error) {
        dst.setError(error);
    }
}

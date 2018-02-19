package org.homunculus.android.component.module.validator.viewErrorHandlers;

import android.support.design.widget.TextInputLayout;
import android.view.View;
import org.homunculus.android.component.module.validator.ViewErrorHandler;

/**
 * {@link ViewErrorHandler} for {@link TextInputLayout}
 * <p>
 * Created by aerlemann on 15.02.18.
 */

public class TextInputLayoutViewErrorHandler extends ViewErrorHandler<TextInputLayout> {
    @Override
    public void setErrorToView(TextInputLayout dst, String error) {
        dst.setError(error);
    }
}

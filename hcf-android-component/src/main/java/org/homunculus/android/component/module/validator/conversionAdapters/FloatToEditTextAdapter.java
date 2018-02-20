package org.homunculus.android.component.module.validator.conversionAdapters;

import android.widget.EditText;

import org.homunculus.android.component.module.validator.ViewErrorHandler;
import org.homunculus.android.component.module.validator.viewErrorHandlers.EditTextViewErrorHandler;

import java.text.MessageFormat;

/**
 * {@link ConversionAdapter} for the combination of {@link EditText} and {@link Float}. There is no locale-specific formatting done here. If you need
 * that, you need to implement it yourself.
 * <p>
 * Created by aerlemann on 19.02.18.
 */

public class FloatToEditTextAdapter implements ConversionAdapter<EditText, Float> {
    @Override
    public void setFieldValueToView(Float value, EditText view) {
        view.setText(MessageFormat.format("{0}", value));
    }

    @Override
    public Float getFieldValueFromView(EditText view) {
        if (view.getText() == null)
            return null;
        try {
            return Float.valueOf(view.getText().toString());
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    @Override
    public ViewErrorHandler<EditText> getErrorHandler() {
        return new EditTextViewErrorHandler();
    }
}

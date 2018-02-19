package org.homunculus.android.component.module.validator.conversionAdapters;

import android.support.design.widget.TextInputLayout;

import org.homunculus.android.component.module.validator.ViewErrorHandler;
import org.homunculus.android.component.module.validator.viewErrorHandlers.TextInputLayoutViewErrorHandler;

import java.text.MessageFormat;

/**
 * {@link ConversionAdapter} for the combination of {@link TextInputLayout} and {@link Double}. There is no locale-specific formatting done here. If you need
 * that, you need to implement it yourself.
 * <p>
 * Created by aerlemann on 19.02.18.
 */

public class DoubleToTextInputLayoutAdapter implements ConversionAdapter<TextInputLayout, Double> {
    @Override
    public void setFieldValueToView(Double value, TextInputLayout view) {
        try {
            //noinspection ConstantConditions
            view.getEditText().setText(MessageFormat.format("{0}", value));
        } catch (NullPointerException e) {
            throw new RuntimeException("Cannot set text, because EditText in TextInputLayout is null!: " + view.getId(), e);
        }
    }

    @Override
    public Double getFieldValueFromView(TextInputLayout view) {
        try {
            //noinspection ConstantConditions
            if (view.getEditText().getText() == null)
                return null;
            //noinspection ConstantConditions
            return Double.valueOf(view.getEditText().getText().toString());
        } catch (NullPointerException e) {
            throw new RuntimeException("Cannot get text, because EditText in TextInputLayout is null!: " + view.getId(), e);
        }
    }

    @Override
    public ViewErrorHandler<TextInputLayout> getErrorHandler() {
        return new TextInputLayoutViewErrorHandler();
    }
}

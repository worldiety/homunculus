package org.homunculus.android.component.module.validator.conversionAdapters;

import android.support.design.widget.TextInputLayout;

import org.homunculus.android.component.module.validator.ViewErrorHandler;
import org.homunculus.android.component.module.validator.viewErrorHandlers.TextInputLayoutViewErrorHandler;

/**
 * {@link ConversionAdapter} for the combination of {@link TextInputLayout} and {@link String}
 * <p>
 * Created by aerlemann on 18.02.18.
 */

public class StringToTextInputLayoutAdapter<M> implements ConversionAdapter<TextInputLayout, String> {

    @Override
    public void setFieldValueToView(String value, TextInputLayout view) {
        try {
            //noinspection ConstantConditions
            view.getEditText().setText(value);
        } catch (NullPointerException e) {
            throw new RuntimeException("Cannot set text, because EditText in TextInputLayout is null!: " + view.getId(), e);
        }
    }

    @Override
    public String getFieldValueFromView(TextInputLayout view) {
        try {
            //noinspection ConstantConditions
            return view.getEditText().getText().toString();
        } catch (NullPointerException e) {
            throw new RuntimeException("Cannot get text, because EditText in TextInputLayout is null!: " + view.getId(), e);
        }
    }

    @Override
    public ViewErrorHandler<TextInputLayout> getErrorHandler() {
        return new TextInputLayoutViewErrorHandler();
    }
}

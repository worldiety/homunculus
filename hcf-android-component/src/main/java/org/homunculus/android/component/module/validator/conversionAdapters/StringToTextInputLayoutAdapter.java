package org.homunculus.android.component.module.validator.conversionAdapters;

import android.support.design.widget.TextInputLayout;

import org.homunculus.android.component.module.validator.ViewErrorHandler;
import org.homunculus.android.component.module.validator.viewErrorHandlers.TextInputLayoutViewErrorHandler;

/**
 * Created by alex on 18.02.18.
 */

public class StringToTextInputLayoutAdapter<M> extends ConversionAdapter<TextInputLayout, String, M> {
    @Override
    boolean isFieldTypeSupported(String fieldType) {
        return fieldType != null;
    }

    @Override
    boolean isViewSupported(TextInputLayout view) {
        return view != null;
    }

    @Override
    void setFieldValueToView(String value, TextInputLayout view) {
        view.getEditText().setText(value);
    }

    @Override
    String getFieldValueFromView(TextInputLayout view) {
        return view.getEditText().getText().toString();
    }

    @Override
    public ViewErrorHandler getErrorHandler() {
        return new TextInputLayoutViewErrorHandler();
    }
}

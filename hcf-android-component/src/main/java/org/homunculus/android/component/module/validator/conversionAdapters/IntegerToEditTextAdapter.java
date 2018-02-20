package org.homunculus.android.component.module.validator.conversionAdapters;

import android.widget.EditText;

import org.homunculus.android.component.module.validator.ViewErrorHandler;
import org.homunculus.android.component.module.validator.viewErrorHandlers.EditTextViewErrorHandler;

import java.text.MessageFormat;

/**
 * {@link ConversionAdapter} for the combination of {@link EditText} and {@link Integer}. There is no locale-specific formatting done here. If you need
 * that, you need to implement it yourself.
 * <p>
 * Created by aerlemann on 19.02.18.
 */

public class IntegerToEditTextAdapter implements ConversionAdapter<EditText, Integer> {

    @Override
    public void setFieldValueToView(Integer value, EditText view) {
        view.setText(MessageFormat.format("{0}", value));
    }

    @Override
    public Integer getFieldValueFromView(EditText view) {
        if (view.getText() == null)
            return null;
        try {
            return Integer.valueOf(view.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public ViewErrorHandler<EditText> getErrorHandler() {
        return new EditTextViewErrorHandler();
    }
}

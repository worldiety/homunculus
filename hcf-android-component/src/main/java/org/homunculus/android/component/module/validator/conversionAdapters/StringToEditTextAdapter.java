package org.homunculus.android.component.module.validator.conversionAdapters;

import android.widget.EditText;

import org.homunculus.android.component.module.validator.ViewErrorHandler;
import org.homunculus.android.component.module.validator.viewErrorHandlers.EditTextViewErrorHandler;
import org.homunculusframework.annotations.Unfinished;

/**
 * {@link ConversionAdapter} for the combination of {@link EditText} and {@link String}
 * <p>
 * Created by aerlemann on 16.02.18.
 */
@Unfinished
public class StringToEditTextAdapter implements ConversionAdapter<EditText, String> {

    @Override
    public void setFieldValueToView(String value, EditText view) {
        view.setText(value);
    }

    @Override
    public String getFieldValueFromView(EditText view) {
        return view.getText().toString();
    }

    @Override
    public ViewErrorHandler<EditText> getErrorHandler() {
        return new EditTextViewErrorHandler();
    }
}

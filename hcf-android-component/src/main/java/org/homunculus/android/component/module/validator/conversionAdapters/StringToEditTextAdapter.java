package org.homunculus.android.component.module.validator.conversionAdapters;

import android.widget.EditText;

import org.homunculus.android.component.module.validator.ValidatorViewConnector;
import org.homunculus.android.component.module.validator.validatorViewConnectors.EditTextValidatorViewConnector;
import org.homunculusframework.annotations.Unfinished;

/**
 * Created by aerlemann on 16.02.18.
 */
@Unfinished
public class StringToEditTextAdapter<M> extends ConversionAdapter<EditText, String, M> {

    @Override
    boolean isFieldTypeSupported(String fieldType) {
        return fieldType != null;
    }

    @Override
    boolean isViewSupported(EditText view) {
        return view != null;
    }

    @Override
    void setFieldValueToView(String value, EditText view) {
        view.setText(value);
    }

    @Override
    String getFieldValueFromView(EditText view) {
        return view.getText().toString();
    }

    @Override
    public ValidatorViewConnector getErrorHandler() {
        return new EditTextValidatorViewConnector();
    }
}

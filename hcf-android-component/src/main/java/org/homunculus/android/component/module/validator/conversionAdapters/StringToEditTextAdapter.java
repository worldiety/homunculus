package org.homunculus.android.component.module.validator.conversionAdapters;

import android.widget.EditText;

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
    void setFieldValueToView(String value, EditText view) {
        view.setText(value);
    }

    @Override
    String getFieldValueFromView(EditText view) {
        return view.getText().toString();
    }
}

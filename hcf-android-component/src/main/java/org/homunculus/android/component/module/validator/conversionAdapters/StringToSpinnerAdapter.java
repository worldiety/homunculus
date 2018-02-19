package org.homunculus.android.component.module.validator.conversionAdapters;

import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.homunculus.android.component.module.validator.ViewErrorHandler;
import org.homunculus.android.component.module.validator.viewErrorHandlers.SpinnerViewErrorHandler;

/**
 * Created by alex on 18.02.18.
 */

public class StringToSpinnerAdapter<M> extends ConversionAdapter<Spinner, String, M> {
    @Override
    boolean isFieldTypeSupported(String fieldType) {
        return fieldType != null;
    }

    @Override
    boolean isViewSupported(Spinner view) {
        return view != null;
    }

    @Override
    void setFieldValueToView(String value, Spinner view) {
        SpinnerAdapter adapter = view.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                view.setSelection(i);
            }
        }
    }

    @Override
    String getFieldValueFromView(Spinner view) {
        Object selectedItem = view.getSelectedItem();
        if (selectedItem instanceof String)
            return (String) selectedItem;

        return null;
    }

    @Override
    public ViewErrorHandler getErrorHandler() {
        return new SpinnerViewErrorHandler();
    }
}

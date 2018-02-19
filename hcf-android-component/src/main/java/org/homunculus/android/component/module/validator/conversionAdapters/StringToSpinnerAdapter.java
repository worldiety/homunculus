package org.homunculus.android.component.module.validator.conversionAdapters;

import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.homunculus.android.component.module.validator.ViewErrorHandler;
import org.homunculus.android.component.module.validator.viewErrorHandlers.SpinnerViewErrorHandler;

/**
 * {@link ConversionAdapter} for the combination of {@link Spinner} and {@link String}
 * <p>
 * Created by aerlemann on 18.02.18.
 */

public class StringToSpinnerAdapter<M> implements ConversionAdapter<Spinner, String> {
    @Override
    public void setFieldValueToView(String value, Spinner view) {
        SpinnerAdapter adapter = view.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                view.setSelection(i);
            }
        }
    }

    @Override
    public String getFieldValueFromView(Spinner view) {
        Object selectedItem = view.getSelectedItem();
        if (selectedItem instanceof String)
            return (String) selectedItem;

        return null;
    }

    @Override
    public ViewErrorHandler<Spinner> getErrorHandler() {
        return new SpinnerViewErrorHandler();
    }
}

package org.homunculus.android.component.module.validator.supportedConnectors;

import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.homunculus.android.component.module.validator.ValidatorViewConnector;

/**
 * {@link ValidatorViewConnector<T> for {@link Spinner}}
 * <p>
 * Created by aerlemann on 16.02.18.
 */
public class SpinnerValidatorViewConnector<T> extends ValidatorViewConnector<T> {

    @Override
    protected boolean isViewOfThisKind(View view) {
        return view instanceof Spinner;
    }

    @Override
    protected void setErrorToView(View dst, String error) {
        ViewGroup parent = dst.getParent() != null && dst.getParent() instanceof ViewGroup ? (ViewGroup) dst.getParent() : null;
        if (parent == null)
            return;

        int positionOfChild = -1;

        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i).equals(dst)) {
                positionOfChild = i;
            }
        }

        LinearLayout newParent = new LinearLayout(dst.getContext());
        newParent.setOrientation(LinearLayout.VERTICAL);
        newParent.setLayoutParams(dst.getLayoutParams());
        parent.removeView(dst);
        parent.addView(newParent, positionOfChild);

        AppCompatTextView errorTextView = new AppCompatTextView(dst.getContext());
        errorTextView.setTextColor(0xfff4511e);
        errorTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        errorTextView.setText(error);
        errorTextView.setPadding(7, 7, 7, 7);
        newParent.addView(dst, new LayoutParams(dst.getLayoutParams().width, dst.getLayoutParams().height));
        newParent.addView(errorTextView);
    }

    @Override
    protected String getTextFromView(View view) {
        Spinner spinner = (Spinner) view;
        Object selectedItem = spinner.getSelectedItem();
        if (selectedItem instanceof String)
            return (String) selectedItem;

        return null;
    }

    @Override
    protected void setTextToView(View view, String text) {
        Spinner spinner = (Spinner) view;
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(text)) {
                spinner.setSelection(i);
            }
        }
    }
}

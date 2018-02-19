package org.homunculus.android.component.module.validator.viewErrorHandlers;

import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import org.homunculus.android.component.module.validator.ViewErrorHandler;

/**
 * {@link ViewErrorHandler} for {@link Spinner}
 * <p>
 * Created by aerlemann on 16.02.18.
 */
public class SpinnerViewErrorHandler extends ViewErrorHandler<Spinner> {
    @Override
    protected void setErrorToView(Spinner dst, String error) {
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
}

package org.homunculus.android.example.common;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import org.homunculus.android.component.Display;

public class ViewWait extends FrameLayout {
    public ViewWait(Context context) {
        super(context);
        ProgressBar pb = new ProgressBar(getContext());
        Display dsp = Display.from(getContext());
        addView(pb, new LayoutParams(dsp.dipToPix(64), dsp.dipToPix(64)));
    }
}

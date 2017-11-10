package org.homunculus.android.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.homunculus.android.component.MaterialFont.Icon;

/**
 * Created by Torben Schinke on 10.11.17.
 */
public class MaterialFontView extends View {

    private int mColor;
    private Icon mIcon;
    private Paint mPaint;
    private float mTextSize;
    private float mTWidth;
    private float mTHeight;


    public MaterialFontView(Context context) {
        super(context);
        init();
    }

    public MaterialFontView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaterialFontView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mColor = getResources().getColor(R.color.hcf_material_font_view);
        mIcon = Icon.I_HELP_OUTLINE;
        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setTypeface(MaterialFont.getTypeface());
        mPaint.setSubpixelText(true);
        mPaint.setAntiAlias(true);
        mTextSize = 12;
        mPaint.setTextSize(mTextSize);
    }


    public MaterialFontView setIcon(@Nullable Icon icon) {
        mIcon = icon;
        requestLayout();
        return this;
    }

    public void setColor(@ColorInt int colorRes) {
        mColor = colorRes;
        mPaint.setColor(mColor);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mIcon == null) {
            return;
        }
        float currentTextWidth = mPaint.measureText(mIcon.asText());
        float viewWidth = getMeasuredWidth();

        float textScale = viewWidth / currentTextWidth;
        mPaint.setTextSize(mTextSize * textScale);

        mTWidth = mPaint.measureText(mIcon.asText());
        mTHeight = mTextSize * textScale;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mIcon != null) {
            canvas.drawText(mIcon.asText(), getWidth() / 2 - mTWidth / 2, getHeight() / 2 + mTHeight / 2, mPaint);
        }
    }
}

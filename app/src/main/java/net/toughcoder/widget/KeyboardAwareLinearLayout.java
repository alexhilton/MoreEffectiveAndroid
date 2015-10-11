package net.toughcoder.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

public class KeyboardAwareLinearLayout extends LinearLayout {
    public KeyboardAwareLinearLayout(Context context) {
        super(context);
    }

    public KeyboardAwareLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardAwareLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int proposedHeight = MeasureSpec.getSize(heightMeasureSpec);
        final int actualHeight = getHeight();

        if (actualHeight > proposedHeight) {
            Log.e("keyboard", "guess keyboard is shown");
        } else {
            Log.e("keyboard", "guess keyboard has been hidden");
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

package net.toughcoder.animation;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import net.toughcoder.effectiveandroid.R;

/**
 * Created by alexhilton on 16/8/29.
 */
public class AnimationAnatomyActivity extends Activity {
    private static final String TAG = "Animation Anatomy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation_annotomy);
        setTitle(TAG);
        final TextView translate = (TextView) findViewById(R.id.translate);

        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = new CustomTranslateAnimation(100.0f, 500.0f, 100.f, 500.f);
                anim.setDuration(900);
                v.startAnimation(anim);
            }
        });
    }

    private static class CustomTranslateAnimation extends Animation implements Animation.AnimationListener {
        private int mFromXType = ABSOLUTE;
        private int mToXType = ABSOLUTE;

        private int mFromYType = ABSOLUTE;
        private int mToYType = ABSOLUTE;

        private float mFromXValue = 0.0f;
        private float mToXValue = 0.0f;

        private float mFromYValue = 0.0f;
        private float mToYValue = 0.0f;

        private float mFromXDelta;
        private float mToXDelta;
        private float mFromYDelta;
        private float mToYDelta;

        /**
         * Constructor to use when building a TranslateAnimation from code
         *
         * @param fromXDelta Change in X coordinate to apply at the start of the
         *        animation
         * @param toXDelta Change in X coordinate to apply at the end of the
         *        animation
         * @param fromYDelta Change in Y coordinate to apply at the start of the
         *        animation
         * @param toYDelta Change in Y coordinate to apply at the end of the
         *        animation
         */
        public CustomTranslateAnimation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
            mFromXValue = fromXDelta;
            mToXValue = toXDelta;
            mFromYValue = fromYDelta;
            mToYValue = toYDelta;

            mFromXType = ABSOLUTE;
            mToXType = ABSOLUTE;
            mFromYType = ABSOLUTE;
            mToYType = ABSOLUTE;
        }

        /**
         * Constructor to use when building a TranslateAnimation from code
         *
         * @param fromXType Specifies how fromXValue should be interpreted. One of
         *        Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
         *        Animation.RELATIVE_TO_PARENT.
         * @param fromXValue Change in X coordinate to apply at the start of the
         *        animation. This value can either be an absolute number if fromXType
         *        is ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
         * @param toXType Specifies how toXValue should be interpreted. One of
         *        Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
         *        Animation.RELATIVE_TO_PARENT.
         * @param toXValue Change in X coordinate to apply at the end of the
         *        animation. This value can either be an absolute number if toXType
         *        is ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
         * @param fromYType Specifies how fromYValue should be interpreted. One of
         *        Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
         *        Animation.RELATIVE_TO_PARENT.
         * @param fromYValue Change in Y coordinate to apply at the start of the
         *        animation. This value can either be an absolute number if fromYType
         *        is ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
         * @param toYType Specifies how toYValue should be interpreted. One of
         *        Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
         *        Animation.RELATIVE_TO_PARENT.
         * @param toYValue Change in Y coordinate to apply at the end of the
         *        animation. This value can either be an absolute number if toYType
         *        is ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
         */
        public CustomTranslateAnimation(int fromXType, float fromXValue, int toXType, float toXValue,
                                  int fromYType, float fromYValue, int toYType, float toYValue) {

            mFromXValue = fromXValue;
            mToXValue = toXValue;
            mFromYValue = fromYValue;
            mToYValue = toYValue;

            mFromXType = fromXType;
            mToXType = toXType;
            mFromYType = fromYType;
            mToYType = toYType;
        }

        private long startMark;
        private int frameCount;
        private long lastFrame = -1;
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (lastFrame != -1) {
                long now = System.nanoTime();
                Log.e(TAG, "this frame takes " + (now - lastFrame) + " to come.");
            }
            lastFrame = System.nanoTime();
            frameCount++;
            float dx = mFromXDelta;
            float dy = mFromYDelta;
            if (mFromXDelta != mToXDelta) {
                dx = mFromXDelta + ((mToXDelta - mFromXDelta) * interpolatedTime);
            }
            if (mFromYDelta != mToYDelta) {
                dy = mFromYDelta + ((mToYDelta - mFromYDelta) * interpolatedTime);
            }
            t.getMatrix().setTranslate(dx, dy);
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            mFromXDelta = resolveSize(mFromXType, mFromXValue, width, parentWidth);
            mToXDelta = resolveSize(mToXType, mToXValue, width, parentWidth);
            mFromYDelta = resolveSize(mFromYType, mFromYValue, height, parentHeight);
            mToYDelta = resolveSize(mToYType, mToYValue, height, parentHeight);
            setAnimationListener(this);
        }

        ///// Animaton Listener

        @Override
        public void onAnimationStart(Animation animation) {
            startMark = System.nanoTime();
            frameCount = 0;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            long now = System.nanoTime();
            Log.e(TAG, "frame " + frameCount + ", rate fps " + ((float) frameCount * 1000000000.f / (float) (now - startMark)));
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
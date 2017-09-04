package net.toughcoder.rs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.RenderScript;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import net.toughcoder.effectiveandroid.R;

public class BitmapBlurTestActivity extends ActionBarActivity {
    private static String TAG = "Bitmap fast blur";
    private ImageSwitcher mImageSwitcher;
    private Handler mMainHandler;
    private RenderScript mRS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        mMainHandler = new Handler();
        setContentView(R.layout.activity_bitmap_blur_test);
        mImageSwitcher = (ImageSwitcher) findViewById(R.id.imageswitcher);
        mRS = RenderScript.create(this);
        showOriginalImage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRS.destroy();
    }

    private void showOriginalImage() {
        Bitmap bm = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.kg));
        ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setImageBitmap(bm);
        FrameLayout.LayoutParams lp =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mImageSwitcher.addView(iv, lp);
        startBlurring(bm);
    }

    private void startBlurring(final Bitmap original) {
        new AsyncTask<Void, Bitmap, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                long t1 = System.currentTimeMillis();
                final int radius = 20;
//                final Bitmap blurred = BlurMethod.fastBlur(original, radius);
//                final Bitmap blurred = BlurMethod.BlurWithNative(original, radius);
                final Bitmap blurred = BlurMethod.blurWithRenderScript(mRS, original, radius);
                long t2 = System.currentTimeMillis();
                Log.e("blur", "size h " + original.getHeight() + ", w " + original.getWidth() + ", 20, takes " + (t2 - t1));
                return blurred;
            }

            @Override
            protected void onPostExecute(Bitmap blurred) {
                ImageView iv = new ImageView(BitmapBlurTestActivity.this);
                iv.setImageBitmap(blurred);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                FrameLayout.LayoutParams lp =
                        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                mImageSwitcher.addView(iv, lp);
                mImageSwitcher.showNext();
            }
        }.execute();
    }

}

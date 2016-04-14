package net.toughcoder.widget;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import net.toughcoder.effectiveandroid.R;

public class BitmapBlurTestActivity extends ActionBarActivity {
    private ImageSwitcher mImageSwitcher;
    private Handler mMainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainHandler = new Handler();
        setContentView(R.layout.activity_bitmap_blur_test);
        mImageSwitcher = (ImageSwitcher) findViewById(R.id.imageswitcher);
        showOriginalImage();
    }

    private void showOriginalImage() {
        Bitmap bm = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.kg));
        ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setImageBitmap(bm);
        FrameLayout.LayoutParams lp =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mImageSwitcher.addView(iv, lp);
    }
}

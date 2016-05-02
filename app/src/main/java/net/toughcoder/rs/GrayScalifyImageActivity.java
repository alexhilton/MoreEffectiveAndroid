package net.toughcoder.rs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.view.Display;
import android.widget.ImageView;

import net.toughcoder.effectiveandroid.R;
import net.toughcoder.effectiveandroid.ScriptC_grayscale;

public class GrayScalifyImageActivity extends ActionBarActivity {
    private Bitmap mBitmap;
    private Bitmap mGrayBitmap;
    private ImageView mDisplay;

    private RenderScript mRSContext;
    private Allocation mIn;
    private Allocation mOut;
    private ScriptC_grayscale mScript;

    private int mWidth;
    private int mHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grayscale);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        mBitmap = loadBitmap();
        mGrayBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mDisplay = (ImageView) findViewById(R.id.display);
        mDisplay.setImageBitmap(mBitmap);

        mRSContext = RenderScript.create(this);
        mIn = Allocation.createFromBitmap(mRSContext, mBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        mOut = Allocation.createTyped(mRSContext, mIn.getType());
        mScript = new ScriptC_grayscale(mRSContext);

        mDisplay.getLayoutParams().width = mWidth;
        mDisplay.getLayoutParams().height = mHeight;

        mScript.forEach_root(mIn, mOut);
        mOut.copyTo(mGrayBitmap);
        mDisplay.setImageBitmap(mGrayBitmap);
    }

    private Bitmap loadBitmap() {
        return BitmapFactory.decodeStream(getResources().openRawResource(R.raw.kg));
    }
}

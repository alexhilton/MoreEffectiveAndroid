package net.toughcoder.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.toughcoder.effectiveandroid.R;

public class SurfaceExampleActivity extends ActionBarActivity implements SurfaceHolder.Callback {
    private static final String TAG = "SurfaceView with transparency example";
    private SurfaceView mSurfaceView;
    private Paint mPaint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        setContentView(R.layout.activity_surface_example);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.setZOrderOnTop(true);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setAlpha(199);
        mPaint.setStrokeWidth(2.0f);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        holder.setFormat(PixelFormat.TRANSPARENT);
        startDrawing(holder);
    }

    private void startDrawing(SurfaceHolder holder) {
        final Canvas canvas = holder.lockCanvas();
//        canvas.drawColor(Color.LTGRAY); // The background of canvas
        canvas.drawCircle(100, 100, 50f, mPaint);
        mPaint.setColor(Color.CYAN);
        canvas.drawRect(110, 300, 290, 400, mPaint);
        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}

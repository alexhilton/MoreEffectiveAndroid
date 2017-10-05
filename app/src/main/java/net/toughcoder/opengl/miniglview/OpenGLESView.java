package net.toughcoder.opengl.miniglview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by alex on 10/2/17.
 */

public class OpenGLESView extends SurfaceView implements SurfaceHolder.Callback {
    public OpenGLESView(Context context) {
        super(context);
        init();
    }

    public OpenGLESView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OpenGLESView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}

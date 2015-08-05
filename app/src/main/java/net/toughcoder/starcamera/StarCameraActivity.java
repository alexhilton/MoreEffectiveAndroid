package net.toughcoder.starcamera;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.ViewGroup;

/**
 * Created by alexhilton on 15/8/4.
 */
public class StarCameraActivity extends Activity {
    private StarCameraRender mRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLSurfaceView view = new GLSurfaceView(this);
        ViewGroup.LayoutParams lp =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(view, lp);
        view.setPreserveEGLContextOnPause(true);
        view.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        view.getHolder().setFormat(PixelFormat.RGBA_8888);
        view.setEGLContextClientVersion(2);
        mRender = new StarCameraRender(this);
        view.setRenderer(mRender);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRender.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRender.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

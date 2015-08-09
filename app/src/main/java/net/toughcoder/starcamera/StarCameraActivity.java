package net.toughcoder.starcamera;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;

/**
 * Created by alexhilton on 15/8/4.
 */
public class StarCameraActivity extends Activity {
    private StarCameraRender mRender;
    private Handler mHandler;
    private GLSurfaceView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int width = getResources().getDisplayMetrics().widthPixels;
        view = new GLSurfaceView(this);
        ViewGroup.LayoutParams lp =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(view, lp);
        view.setPreserveEGLContextOnPause(true);
        view.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        view.getHolder().setFormat(PixelFormat.RGBA_8888);
        view.setEGLContextClientVersion(2);
        mRender = new StarCameraRender(this);
        view.setRenderer(mRender);
        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mHandler = new Handler();
        refresh();
    }

    private void refresh() {
        view.requestRender();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }, 300);
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

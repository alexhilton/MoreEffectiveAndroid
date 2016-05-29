package net.toughcoder.camera;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import net.toughcoder.effectiveandroid.R;

public class OpenGLPreviewActivity extends ActionBarActivity {
    private static String TAG = "OpenGL Preview";
    private GLSurfaceView mSurfaceView;
    private OpenGLPreviewRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        setContentView(R.layout.activity_open_glpreview);
        mSurfaceView = (GLSurfaceView) findViewById(R.id.openglview);
        mRenderer = new OpenGLPreviewRenderer();
        setupSurfaceView(mRenderer);
    }

    private void setupSurfaceView(OpenGLPreviewRenderer renderer) {
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mSurfaceView.setRenderer(renderer);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mSurfaceView.requestRender();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
    }
}

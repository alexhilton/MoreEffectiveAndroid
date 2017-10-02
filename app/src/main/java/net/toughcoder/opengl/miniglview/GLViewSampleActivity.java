package net.toughcoder.opengl.miniglview;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.FrameLayout;

import net.toughcoder.effectiveandroid.R;

public class GLViewSampleActivity extends Activity {
    private static final String TAG = "GLViewSample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TAG);
        setContentView(R.layout.activity_glview_sample);
        setupGLView();
    }

    private void setupGLView() {
        GLSurfaceView glview = new GLSurfaceView(this);
        glview.setEGLContextClientVersion(2);
        glview.setPreserveEGLContextOnPause(true);
        glview.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glview.setRenderer(new TriangleRenderer());
        glview.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        FrameLayout content = (FrameLayout) findViewById(android.R.id.content);
        content.addView(glview);
    }
}

package net.toughcoder.opengl.miniglview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.FrameLayout;

import net.toughcoder.effectiveandroid.R;

public class GLViewSampleActivity extends Activity {
    private static final String TAG = "GLViewSample";
    private static final int MSG_RENDER = 0x01;
    static Context sContext;

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RENDER:
                    OpenGLESView view = (OpenGLESView) msg.obj;
                    requestRender(view);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sContext = this;
        setTitle(TAG);
        setContentView(R.layout.activity_glview_sample);
        setupGLView();
    }

    private void setupGLView() {
//        GLSurfaceView glview = new GLSurfaceView(this);
//        glview.setEGLContextClientVersion(2);
//        glview.setPreserveEGLContextOnPause(true);
//        glview.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
//        glview.setRenderer(new TriangleRenderer());
//        glview.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        OpenGLESView glview = new OpenGLESView(this);
        glview.setRenderer(new TriangleRenderer());
//        glview.setRenderMode(OpenGLESView.RenderMode.WHEN_DIRTY);
        FrameLayout content = (FrameLayout) findViewById(android.R.id.content);
        content.addView(glview);
//        requestRender(glview);
    }

    private void requestRender(OpenGLESView view) {
        view.requestRender();
        Message msg = Message.obtain();
        msg.obj = view;
        msg.what = MSG_RENDER;
        mMainHandler.removeMessages(MSG_RENDER);
        mMainHandler.sendMessageDelayed(msg, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainHandler.removeMessages(MSG_RENDER);
        sContext = null;
    }
}

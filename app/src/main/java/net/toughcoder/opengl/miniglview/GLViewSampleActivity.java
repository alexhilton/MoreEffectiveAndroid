package net.toughcoder.opengl.miniglview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.toughcoder.effectiveandroid.R;
import net.toughcoder.rs.GrayScalifyImageActivity;

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
        final OpenGLESView glview = new OpenGLESView(this);
        glview.setRenderer(new TriangleRenderer());
        glview.setRenderMode(OpenGLESView.RenderMode.WHEN_DIRTY);
        FrameLayout content = (FrameLayout) findViewById(android.R.id.content);
        content.addView(glview);
        requestRender(glview);

        // Label
        final TextView label = new TextView(this);
        label.setPadding(20, 20, 20, 20);
        label.setText(status(glview.getRenderMode()));
        label.setTextColor(Color.WHITE);
        FrameLayout.LayoutParams lpt = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpt.setMarginEnd(20);
        lpt.setMarginStart(20);
        lpt.bottomMargin = 20;
        lpt.topMargin = 20;
        lpt.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        content.addView(label, lpt);

        // Button to change the mode
        final Button changeMode = new Button(this);
        changeMode.setText(modeText(glview.getRenderMode()));
        changeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final OpenGLESView.RenderMode oldMode = glview.getRenderMode();
                OpenGLESView.RenderMode newMode =
                        oldMode == OpenGLESView.RenderMode.CONTINUOUSLY ? OpenGLESView.RenderMode.WHEN_DIRTY :
                                OpenGLESView.RenderMode.CONTINUOUSLY;
                changeMode.setText(modeText(newMode));
                glview.setRenderMode(newMode);
                label.setText(status(newMode));
            }
        });
        changeMode.setPadding(20, 20, 20, 20);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginStart(20);
        lp.setMarginEnd(20);
        lp.bottomMargin = 20;
        lp.topMargin = 20;
        lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        content.addView(changeMode, lp);


    }

    private String status(OpenGLESView.RenderMode mode) {
        return mode == OpenGLESView.RenderMode.CONTINUOUSLY ? "Render continuously" : "Render when dirty";
    }

    private String modeText(OpenGLESView.RenderMode mode) {
        return mode == OpenGLESView.RenderMode.CONTINUOUSLY ?  "When dirty" : "Continuously";
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

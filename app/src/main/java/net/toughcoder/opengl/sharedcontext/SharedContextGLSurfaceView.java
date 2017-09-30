package net.toughcoder.opengl.sharedcontext;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Created by alex on 17-9-30.
 */

public class SharedContextGLSurfaceView extends GLSurfaceView {
    private static final String TAG = "SCGLSurfaceView";
    private static EGLContextFactory sSharedEGLContext;
    private static int EGL_VERSION = 2;

    public SharedContextGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public SharedContextGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(EGL_VERSION);
        setPreserveEGLContextOnPause(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        if (sSharedEGLContext == null) {
            sSharedEGLContext = new SharedEGLContextFactory();
        }
        setEGLContextFactory(sSharedEGLContext);
    }

    @Override
    public void setRenderer(Renderer renderer) {
        super.setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public class SharedEGLContextFactory implements GLSurfaceView.EGLContextFactory {
        // Do not ask, copied from GLSurfaceView.
        private int CLIENT_VERSION = 0x3098;

        public EGLContext mSharedContext;

        @Override
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            int[] attrib_list = {CLIENT_VERSION, EGL_VERSION, EGL10.EGL_NONE};
            mSharedContext = egl.eglCreateContext(display,
                    eglConfig, mSharedContext == null ? EGL10.EGL_NO_CONTEXT : mSharedContext, attrib_list);
            return mSharedContext;
        }

        @Override
        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            if (!egl.eglDestroyContext(display, context)) {
                Log.d(TAG, "Failed to destory context");
            }
            Log.d(TAG, "destroyContext " + Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
        }
    }
}

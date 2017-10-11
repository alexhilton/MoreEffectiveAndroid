package net.toughcoder.opengl.miniglview;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by alex on 10/2/17.
 */

public class OpenGLESView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "OpenGLESView";
    private GLSurfaceView.Renderer mRenderer;
    private GLThread mGLThread;

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
        mGLThread = new GLThread();
        getHolder().addCallback(this);
    }

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        mRenderer = renderer;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mGLThread.onSurfaceCreate(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mGLThread.onSurfaceChange(holder, format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mGLThread.onSurfaceDestroy(holder);
    }

    private class GLThread extends Thread {
        // All OpenGL ES API call should happen in this thread.
        private final List<Runnable> mPreJobQueue;
        private final List<Runnable> mPostJobQueue;
        private boolean mQuit;
        private boolean mReadyToDraw;

        private EGLContext mEGLContext;
        private EGLSurface mEGLSurface;
        private EGLDisplay mEGLDisplay;

        public GLThread() {
            mPreJobQueue = new LinkedList<>();
            mPostJobQueue = new LinkedList<>();
            mQuit = false;
            mReadyToDraw = false;

            mEGLContext = EGL14.EGL_NO_CONTEXT;
            mEGLSurface = EGL14.EGL_NO_SURFACE;
            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        }

        @Override
        public void run() {
            while (true) {
                executePreJobs();
                // Check for prerequisite errors
                if (mQuit) {
                    break;
                }
                if (mRenderer != null && mReadyToDraw) {
                    // TODO: this is dangerous, though we know that no one would use GL object.
                    mRenderer.onDrawFrame(null);
                    if (!EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)) {
                        Log.d(TAG, "Failed to swap buffers");
                    }
                }
                executePostJobs();
                // check for termination
                if (mQuit) {
                    break;
                }
            }
        }

        private void executePreJobs() {
            synchronized (mPreJobQueue) {
                while (!mPreJobQueue.isEmpty()) {
                    Runnable job = mPreJobQueue.remove(0);
                    job.run();
                }
            }
        }

        private void executePostJobs() {
            synchronized (mPostJobQueue) {
                while (!mPostJobQueue.isEmpty()) {
                    Runnable job = mPostJobQueue.remove(0);
                    job.run();
                }
                mPostJobQueue.notify();
            }
        }

        public void onSurfaceCreate(SurfaceHolder holder) {
            initialize(holder);
            start();
        }

        private void initialize(final SurfaceHolder holder) {
            // Initialize OpenGL ES context
            final Runnable job = new Runnable() {
                @Override
                public void run() {
                    doInitialize(holder);
                }
            };
            synchronized (mPreJobQueue) {
                mPreJobQueue.add(job);
            }
        }

        private void doInitialize(SurfaceHolder holder) {
            final EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
                Log.d(TAG, "Failed to get egl display");
                mQuit = true;
                return;
            }
            mEGLDisplay = eglDisplay;
            final int[] versions = new int[2];
            if (!EGL14.eglInitialize(eglDisplay, versions, 0, versions, 1)) {
                Log.d(TAG, "Failed to initialize EGL display");
                mQuit = true;
                return;
            }

            final int[] attribList = {
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_NONE, // The attrib list is terminated by EGL_NONE
            };
            final EGLConfig[] configs = new EGLConfig[1];
            final int[] numConfigs = new int[1];
            if (!EGL14.eglChooseConfig(eglDisplay,
                    attribList, 0,
                    configs, 0,
                    configs.length, numConfigs, 0)) {
                Log.d(TAG, "Failed to choose config");
                mQuit = true;
                return;
            }

            final int[] contextAttribList = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE,
            };
            final EGLContext context = EGL14.eglCreateContext(eglDisplay,
                    configs[0], EGL14.EGL_NO_CONTEXT, contextAttribList, 0);
            if (context == EGL14.EGL_NO_CONTEXT) {
                Log.e(TAG, "failed to create context");
                mQuit = true;
                return;
            }
            mEGLContext = context;

            final int[] surfaceAttribs = {
                    EGL14.EGL_NONE,
            };
            final EGLSurface surface = EGL14.eglCreateWindowSurface(eglDisplay,
                    configs[0], holder, surfaceAttribs, 0);
            if (!EGL14.eglMakeCurrent(eglDisplay, surface, surface, context)) {
                Log.d(TAG, "Failed to make current");
                mQuit = true;
                return;
            }
            mEGLSurface = surface;

            mRenderer.onSurfaceCreated(null, null);
        }

        public void onSurfaceChange(SurfaceHolder holder, int format, final int width, final int height) {
            synchronized (mPreJobQueue) {
                final Runnable job = new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.onSurfaceChanged(null, width, height);
                        mReadyToDraw = true;
                    }
                };
                mPreJobQueue.add(job);
            }
        }

        // Surface will be destroyed after this method return.
        // As a result, should not call any GLES methods after this method return.
        // So, should not return before all draw finish.
        public void onSurfaceDestroy(SurfaceHolder holder) {
            Log.d(TAG, "onSurfaceDestroy");
            // clean up and exit the run-loop
            final Runnable exitJob = new Runnable() {
                @Override
                public void run() {
                    doCleanup();
                    mQuit = true;
                }
            };
            synchronized (mPostJobQueue) {
                mPostJobQueue.add(exitJob);
            }
            Thread.yield(); // Let render thread run and we wait.
            synchronized (mPostJobQueue) {
                while (!mPostJobQueue.isEmpty()) {
                    try {
                        mPostJobQueue.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        private void doCleanup() {
            Log.d(TAG, "doCleanup");
            if (mEGLSurface != EGL14.EGL_NO_SURFACE && !EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface)) {
                Log.d(TAG, "Failed to destroy surface");
            }
            if (mEGLContext != EGL14.EGL_NO_CONTEXT && !EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)) {
                Log.d(TAG, "failed to destroy context");
            }
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY && !EGL14.eglTerminate(mEGLDisplay)) {
                Log.d(TAG, "Failed to terminate display");
            }
        }
    }
}

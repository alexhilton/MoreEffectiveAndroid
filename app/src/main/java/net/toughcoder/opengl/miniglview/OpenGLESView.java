package net.toughcoder.opengl.miniglview;

import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
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
    private Renderer mRenderer;
    private GLThread mGLThread;
    private static EGLContext sSharedContext = EGL14.EGL_NO_CONTEXT;
    private static int sSharedContextRefCount = 0;
    private static int sThreadId = 0;

    private static final boolean sDEBUG = true;

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

    public void setRenderer(Renderer renderer) {
        if (renderer == null) {
            throw new IllegalArgumentException("Renderer cannot be null.");
        }
        mRenderer = renderer;
        mGLThread.start();
    }

    public void setRenderMode(RenderMode mode) {
        mGLThread.setRenderMode(mode);
    }

    public RenderMode getRenderMode() {
        return mGLThread.mRenderMode;
    }

    public void requestRender() {
        mGLThread.requestRender();
    }

    public void shareEGLContext() {
        mGLThread.shareEGLContext();
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
        private final List<Runnable> mRenderJobQueue;

        private boolean mQuit;
        private boolean mReadyToDraw;
        private RenderMode mRenderMode;

        private EGLContext mEGLContext;
        private EGLSurface mEGLSurface;
        private EGLDisplay mEGLDisplay;

        private volatile boolean mAlive;

        private boolean mShareEGLContext;

        private final Runnable mRenderJob = new Runnable() {
            @Override
            public void run() {
                if (ableToDraw()) {
                    mRenderer.onDrawFrame();
                    if (!EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)) {
                        logEGLError("Failed to swap buffers");
                    }
                }
            }
        };

        public GLThread() {
            setName("OpenGL ES Renderer Thread #" + sThreadId++);
            mPreJobQueue = new LinkedList<>();
            mPostJobQueue = new LinkedList<>();
            mRenderJobQueue = new LinkedList<>();

            mAlive = false; // Only alive between surface created and surface destroyed.
            mShareEGLContext = false;
            mRenderMode = RenderMode.CONTINUOUSLY;
        }

        private void init() {
            mPreJobQueue.clear();
            mPostJobQueue.clear();
            mRenderJobQueue.clear();
            mQuit = false;
            mReadyToDraw = false;

            initRenderJob();

            mEGLContext = EGL14.EGL_NO_CONTEXT;
            mEGLSurface = EGL14.EGL_NO_SURFACE;
            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        }

        // Must call after init render type
        private void initRenderJob() {
            synchronized (mRenderJobQueue) {
                if (mRenderMode == RenderMode.CONTINUOUSLY) {
                    mRenderJobQueue.add(mRenderJob);
                } else {
                    mRenderJobQueue.clear();
                }
                mRenderJobQueue.notify();
            }
        }

        @Override
        public void run() {
            logDebug(getName() + ": run run starts");
            while (true) {
                executePreJobs();
                // Check for prerequisite errors
                if (mQuit) {
                    break;
                }
                executeRenderJob();
                executePostJobs();
                // check for termination
                if (mQuit) {
                    break;
                }
            }
            logDebug(getName() + ": run run finishes");
        }

        private boolean ableToDraw() {
            return mRenderer != null && mReadyToDraw;
        }

        private void setRenderMode(RenderMode mode) {
            mRenderMode = mode;
            initRenderJob();
        }

        private void requestRender() {
            if (mRenderMode == RenderMode.CONTINUOUSLY || !mAlive) {
                return;
            }
            synchronized (mRenderJobQueue) {
                mRenderJobQueue.add(mRenderJob);
                mRenderJobQueue.notify();
            }
        }

        private void shareEGLContext() {
            mShareEGLContext = true;
        }

        private void logDebug(String msg) {
            if (sDEBUG) {
                Log.d(TAG, msg + ": tid->" + Thread.currentThread().getName() + ":" + this);
            }
        }

        private void executePreJobs() {
            logDebug("executePreJobs starts");
            synchronized (mPreJobQueue) {
                while (!mPreJobQueue.isEmpty()) {
                    Runnable job = mPreJobQueue.remove(0);
                    job.run();
                }
            }
            logDebug("executePreJobs finishes");
        }

        private void executePostJobs() {
            logDebug("executePostJobs starts");
            synchronized (mPostJobQueue) {
                while (!mPostJobQueue.isEmpty()) {
                    Runnable job = mPostJobQueue.remove(0);
                    job.run();
                }
                mPostJobQueue.notify();
            }
            logDebug("executePostJobs finishes");
        }

        private void executeRenderJob() {
            logDebug("executeRenderJob starts");
            synchronized (mRenderJobQueue) {
                if (mRenderJobQueue.isEmpty()) {
                    try {
                        mRenderJobQueue.wait();
                    } catch (InterruptedException e) {
                    }
                }
                if (!mRenderJobQueue.isEmpty()) {
                    final Runnable job = mRenderJobQueue.get(0);
                    job.run();
                    if (mRenderMode == RenderMode.WHEN_DIRTY) {
                        mRenderJobQueue.clear();
                    }
                }
            }
            logDebug("executeRenderJob finishes");
        }

        private void onSurfaceCreate(SurfaceHolder holder) {
            logDebug("onSurfaceCreate starts, holder -> " + holder);
            init();
            mAlive = true;
            initializeEGL(holder);
            if (mRenderer != null && !mGLThread.isAlive()) {
                // Not start over, but resume so starts the thread.
                start();
            }
            logDebug("onSurfaceCreate finishes");
        }

        private void initializeEGL(final SurfaceHolder holder) {
            // Initialize OpenGL ES context
            final Runnable job = new Runnable() {
                @Override
                public void run() {
                    doInitializeEGL(holder);
                }
            };
            synchronized (mPreJobQueue) {
                mPreJobQueue.add(job);
            }
        }

        private void doInitializeEGL(SurfaceHolder holder) {
            final EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
                logEGLError("Failed to get egl display");
                mQuit = true;
                return;
            }
            mEGLDisplay = eglDisplay;
            final int[] versions = new int[2];
            if (!EGL14.eglInitialize(eglDisplay, versions, 0, versions, 1)) {
                logEGLError("Failed to init EGL display");
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
                logEGLError("Failed to choose config");
                mQuit = true;
                return;
            }

            final int[] contextAttribList = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE,
            };
            final EGLContext shared =
                    mShareEGLContext && sSharedContext != EGL14.EGL_NO_CONTEXT ? sSharedContext : EGL14.EGL_NO_CONTEXT;
            final EGLContext context = EGL14.eglCreateContext(eglDisplay,
                    configs[0], shared, contextAttribList, 0);
            if (context == EGL14.EGL_NO_CONTEXT) {
                Log.e(TAG, "failed to create context");
                mQuit = true;
                return;
            }
            mEGLContext = context;
            if (mShareEGLContext && sSharedContext == EGL14.EGL_NO_CONTEXT) {
                sSharedContext = context;
            }

            if (mShareEGLContext) {
                sSharedContextRefCount++;
            }

            final int[] surfaceAttribs = {
                    EGL14.EGL_NONE,
            };
            final EGLSurface surface = EGL14.eglCreateWindowSurface(eglDisplay,
                    configs[0], holder, surfaceAttribs, 0);
            if (!EGL14.eglMakeCurrent(eglDisplay, surface, surface, context)) {
                logEGLError("Failed to make current");
                mQuit = true;
                return;
            }
            mEGLSurface = surface;

            if (mRenderer != null) {
                mRenderer.onContextCreate();
            }
        }

        private void logEGLError(String msg) {
            final int err = EGL14.eglGetError();
            Log.w(TAG, msg + ": " + Integer.toHexString(err));
        }

        private void onSurfaceChange(SurfaceHolder holder, int format, final int width, final int height) {
            logDebug("onSurfaceChange width -> " + width + ", height -> " + height);
            synchronized (mPreJobQueue) {
                final Runnable job = new Runnable() {
                    @Override
                    public void run() {
                        if (mRenderer != null) {
                            mRenderer.onContextChange(width, height);
                        }
                        mReadyToDraw = true;
                    }
                };
                mPreJobQueue.add(job);
            }
            if (mRenderMode == RenderMode.WHEN_DIRTY) {
                synchronized (mRenderJobQueue) {
                    mRenderJobQueue.notify();
                }
            }
            logDebug("onSurfaceChange finishes");
        }

        // Surface will be destroyed after this method return.
        // As a result, should not call any GLES methods after this method return.
        // So, should not return before all draw finish.
        private void onSurfaceDestroy(SurfaceHolder holder) {
            logDebug("onSurfaceDestroy starts, holder -> " + holder);
            mAlive = false;
            // What if the renderer thread never been started? Should quit immediately.
            if (!mGLThread.isAlive()) {
                logDebug("onSurfaceDestroy finish thread not started yet, quit directly");
                return;
            }
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

            if (mRenderMode == RenderMode.WHEN_DIRTY) {
                // Interrupt render queue block
                synchronized (mRenderJobQueue) {
                    mRenderJobQueue.notify();
                }
                Thread.yield();
                // Render queue will never be empty for continuous mode, as a result
                // should never wait for render queue empty for continuous mode.
                synchronized (mRenderJobQueue) {
                    while (!mRenderJobQueue.isEmpty()) {
                        try {
                            mRenderJobQueue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
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
            logDebug("onSurfaceDestroy finishes");
        }

        private void doCleanup() {
            Log.d(TAG, "doCleanup");
            if (mRenderer != null) {
                mRenderer.onContextDestroy();
            }
            if (mEGLSurface != EGL14.EGL_NO_SURFACE && !EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface)) {
                logEGLError("Failed to destroy surface");
            }

            if (mShareEGLContext) {
                sSharedContextRefCount--;
                if (sSharedContextRefCount == 0) {
                    if (sSharedContext != EGL14.EGL_NO_CONTEXT && !EGL14.eglDestroyContext(mEGLDisplay, sSharedContext)) {
                        logEGLError("Failed to destroy shared context");
                    }
                    sSharedContext = EGL14.EGL_NO_CONTEXT;
                }
            } else {
                if (mEGLContext != EGL14.EGL_NO_CONTEXT && !EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)) {
                    logEGLError("failed to destroy context");
                }
            }

            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY && !EGL14.eglTerminate(mEGLDisplay)) {
                logEGLError("Failed to terminate display");
            }
        }
    }

    public enum RenderMode {
        WHEN_DIRTY,
        CONTINUOUSLY,
    }

    public interface Renderer {
        void onContextCreate();
        void onContextChange(int width, int height);
        void onDrawFrame();
        void onContextDestroy();
    }
}

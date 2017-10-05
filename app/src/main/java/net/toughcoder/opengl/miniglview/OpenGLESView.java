package net.toughcoder.opengl.miniglview;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by alex on 10/2/17.
 */

public class OpenGLESView extends SurfaceView implements SurfaceHolder.Callback {
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
        private final List<Runnable> mJobQueue;
        private final Object mQueueLock;
        private boolean mQuite;

        public GLThread() {
            mJobQueue = new LinkedList<>();
            mQueueLock = new Object();
            mQuite = false;
        }

        @Override
        public void run() {
            while (true) {
                executeAllJobs();
                if (mQuite) {
                    break;
                }
                if (mRenderer != null) {
                    // TODO: this is dangerous, though we know that no one would use GL object.
                    mRenderer.onDrawFrame(null);
                }
            }
        }

        private void executeAllJobs() {
            synchronized (mQueueLock) {
                while (!mJobQueue.isEmpty()) {
                    Runnable job = mJobQueue.remove(0);
                    job.run();
                }
            }
        }

        public void onSurfaceCreate(SurfaceHolder holder) {
            initialize(holder);
            start();
        }

        private void initialize(SurfaceHolder holder) {
            // Initialize OpenGL ES context
            final Runnable job = new Runnable() {
                @Override
                public void run() {

                }
            };
            synchronized (mQueueLock) {
                mJobQueue.add(job);
            }
        }

        public void onSurfaceChange(SurfaceHolder holder, int format, int width, int height) {
            //
        }

        public void onSurfaceDestroy(SurfaceHolder holder) {
            // clean up and exit the run-loop
            final Runnable exitJob = new Runnable() {
                @Override
                public void run() {
                    mQuite = true;
                }
            };
            synchronized (mQueueLock) {
                mJobQueue.add(exitJob);
            }
        }
    }
}

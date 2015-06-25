package net.toughcoder.opengl2s;

import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alexhilton on 15/6/11.
 */
public abstract class OpenGLES2Render implements GLSurfaceView.Renderer {
    private static final String TAG = "OpenGLES2Render";
    private boolean mFirstDraw;
    private boolean mSurfaceCreated;
    private int mWidth;
    private int mHeight;
    private long mLastTime;
    private int mFPS;

    public OpenGLES2Render() {
        mFirstDraw = true;
        mSurfaceCreated = false;
        mWidth = -1;
        mHeight = -1;
        mLastTime = System.currentTimeMillis();
        mFPS = 0;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mSurfaceCreated = true;
        mWidth = -1;
        mHeight = -1;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (!mSurfaceCreated && width == mWidth && height == mHeight) {
            Log.e(TAG, "surface changed but already handled");
            return;
        }

        mWidth = width;
        mHeight = height;

        onCreate(mWidth, mHeight, mSurfaceCreated);
        mSurfaceCreated = false;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mFPS++;
        if (System.currentTimeMillis() - mLastTime >= 1000) {
            mFPS = 0;
            mLastTime = System.currentTimeMillis();
        }

        mFirstDraw = false;
    }

    public int getFPS() {
        return mFPS;
    }

    public abstract void onCreate(int width, int height, boolean contextLost);

    public abstract void onDrawFrame(boolean firstDraw);
}

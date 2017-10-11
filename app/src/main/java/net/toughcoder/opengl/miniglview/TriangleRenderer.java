package net.toughcoder.opengl.miniglview;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static net.toughcoder.opengl.sharedcontext.SurfaceTextureRenderer.loadProgram;

/**
 * Created by alex on 10/2/17.
 */

public class TriangleRenderer implements GLSurfaceView.Renderer, OpenGLESView.Renderer {
    private static final String TAG = "TriangleRenderer";
    private static final String VERTEX =
            "attribute vec4 position;\n" +
                    "void main() {\n" +
                    "  gl_Position = position;\n" +
                    "}";
    private static final String FRAGMENT =
            "precision highp float;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = vec4(.5, .5, 0, 1);\n" +
                    "}";

    private int mProgram;
    private int mAttributePosition;
    public final float[] TRIANGLE = {
            -.5f, -.5f,
            .5f, -.5f,
            .0f, .5f,
    };
    private FloatBuffer mTriangle;

    // Move the triangle
    private float mCenterX = 0.0f;
    private float mCenterY = -0.4f;
    private float mXStep = 0.05f;
    private float mYStep = 0.05f;
    private float mRadius = 0.2f;

    private int mTexture;

    private static final boolean sDEBUG = true;
    private static int sFps = 0;
    private static long sLastFps = -1;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");
        onContextCreate();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");
        onContextChange(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        onDrawFrame();
    }

    private void move() {
        mCenterX += mXStep;
        mCenterY += mYStep;
        // close to boundary
        if (mCenterX + mRadius > 1f || mCenterX - mRadius < -1f) {
            mCenterX -= mXStep;
            mXStep *= -1f;
        }
        if (mCenterY + mRadius > 1f || mCenterY - mRadius < -1f) {
            mCenterY -= mYStep;
            mYStep *= -1f;
        }
    }

    private void forgeTriangle() {
        TRIANGLE[0] = mCenterX - mRadius;
        TRIANGLE[1] = mCenterY - mRadius;
        TRIANGLE[2] = mCenterX + mRadius;
        TRIANGLE[3] = mCenterY - mRadius;
        TRIANGLE[4] = mCenterX;
        TRIANGLE[5] = mCenterY + mRadius;
    }

    // Our version renderer methods.

    @Override
    public void onContextCreate() {
        GLES20.glDisable(GLES20.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mProgram = loadProgram(VERTEX, FRAGMENT);
        mAttributePosition = GLES20.glGetAttribLocation(mProgram, "position");
        mTriangle = ByteBuffer.allocateDirect(TRIANGLE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTriangle.put(TRIANGLE).position(0);

        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        mTexture = tex[0];

        sFps = 0;
        sLastFps = -1;
    }

    @Override
    public void onContextChange(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        if (sDEBUG) {
            sFps++;
            if (sLastFps == -1) {
                sLastFps = SystemClock.uptimeMillis();
            } else {
                final long now = SystemClock.uptimeMillis();
                if (now - sLastFps >= 1000) {
                    Log.d(TAG, "FPS -> " + sFps);
                    sFps = 0;
                    sLastFps = now;
                }
            }
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        move();
        forgeTriangle();
        mTriangle.put(TRIANGLE);
        mTriangle.position(0);
        GLES20.glVertexAttribPointer(mAttributePosition, 2, GLES20.GL_FLOAT, false, 0, mTriangle);
        GLES20.glEnableVertexAttribArray(mAttributePosition);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        GLES20.glDisableVertexAttribArray(mAttributePosition);
        final int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            Log.d(TAG, "renderer draw there is error ->" + Integer.toHexString(error));
        }
    }

    @Override
    public void onContextDestroy() {
        GLES20.glDeleteProgram(mProgram);
        GLES20.glDeleteTextures(1, new int[] {mTexture}, 0);
    }
}

package net.toughcoder.opengl.miniglview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.SystemClock;
import android.util.Log;

import net.toughcoder.effectiveandroid.R;

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
                    "attribute vec4 inputTextureCoords;\n" +
                    "varying vec2 textureCoords;\n" +
                    "void main() {\n" +
                    "  textureCoords = inputTextureCoords.xy;\n" +
                    "  gl_Position = position;\n" +
                    "}";
    private static final String FRAGMENT =
            "precision highp float;\n" +
                    "varying highp vec2 textureCoords;\n" +
                    "uniform sampler2D uTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(uTexture, textureCoords);\n" +
                    "}";

    private int mProgram;
    private int mAttributePosition;
    private int mAttribTextureCoords;
    private int mUniformTexture;

    public final float[] TRIANGLE = {
            -.5f, -.5f,
            .5f, -.5f,
            -.5f, .5f,
            .5f, .5f,
    };
    private final float[] TEXTURE_COORDS = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f,
    };
    private FloatBuffer mTriangle;
    private FloatBuffer mTextureBuffer;

    // Move the triangle
    private float mCenterX = 0.0f;
    private float mCenterY = -0.4f;
    private float mXStep = 0.05f;
    private float mYStep = 0.05f;
    private float mRadius = 0.2f;

    private int mTexture;
    private Bitmap mImage;

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
        Log.d(TAG, "onDrawFrame");
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
        TRIANGLE[4] = mCenterX - mRadius;
        TRIANGLE[5] = mCenterY + mRadius;
        TRIANGLE[6] = mCenterX + mRadius;
        TRIANGLE[7] = mCenterY + mRadius;
    }

    // Our version renderer methods.

    @Override
    public void onContextCreate() {
        Log.d(TAG, "onContextCreate");
        GLES20.glDisable(GLES20.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mProgram = loadProgram(VERTEX, FRAGMENT);
        mAttributePosition = GLES20.glGetAttribLocation(mProgram, "position");
        mAttribTextureCoords = GLES20.glGetAttribLocation(mProgram, "inputTextureCoords");
        mUniformTexture = GLES20.glGetUniformLocation(mProgram, "uTexture");

        mTriangle = ByteBuffer.allocateDirect(TRIANGLE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTriangle.put(TRIANGLE).position(0);
        mTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureBuffer.put(TEXTURE_COORDS).position(0);

        mImage = BitmapFactory.decodeResource(GLViewSampleActivity.sContext.getResources(), R.raw.basketball);
        mTexture = loadTexture(mImage);

        sFps = 0;
        sLastFps = -1;
    }

    @Override
    public void onContextChange(int width, int height) {
        Log.d(TAG, "onContextChange");
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        Log.d(TAG, "onDrawFrame");
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
        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mAttribTextureCoords, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(mAttribTextureCoords);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(mUniformTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, TRIANGLE.length / 2);

        GLES20.glDisableVertexAttribArray(mAttributePosition);
        GLES20.glDisableVertexAttribArray(mAttribTextureCoords);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        final int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            Log.d(TAG, "renderer draw there is error ->" + Integer.toHexString(error));
        }
    }

    @Override
    public void onContextDestroy() {
        Log.d(TAG, "onContextDestroy");
        GLES20.glDeleteProgram(mProgram);
        GLES20.glDeleteTextures(1, new int[] {mTexture}, 0);
    }

    private int loadTexture(Bitmap bitmap) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return textures[0];
    }
}

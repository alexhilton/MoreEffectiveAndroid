package net.toughcoder.opengl.sharedcontext;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alex on 17-9-16.
 */

abstract class SurfaceTextureRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "PreviewRenderer";
    public final float[] CUBE = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };
    public final float[] TEXTURE_NO_ROTATION = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };
    private final FloatBuffer mCubeBuffer;
    private final FloatBuffer mTextureBuffer;

    private static final String VERTEX_SHADER =
            "attribute vec4 position;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 inputTextureCoords;\n" +
                    "varying vec2 textureCoords;\n" +
                    "void main() {\n" +
                    "  gl_Position = position;\n" +
                    "  textureCoords = (uSTMatrix * inputTextureCoords).xy;\n" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision highp float;\n" +
                    "uniform samplerExternalOES uTextureSampler;\n" +
                    "varying vec2 textureCoords;\n" +
                    "void main () {\n" +
                    "  vec4 tex = texture2D(uTextureSampler, textureCoords);\n" +
                    "  gl_FragColor = vec4(tex.rgb, 1);\n" +
                    "}";

    private int mProgram;
    private int mAttributePosition;
    protected int mTextureCoords;
    private int mUniformLocation;
    private int mUniformMatrix;

    public SurfaceTextureRenderer() {
        mCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mCubeBuffer.put(CUBE).position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");

        // Initialize GL stuff
        GLES20.glDisable(GLES20.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);

        mCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mAttributePosition, 2, GLES20.GL_FLOAT, false, 0, mCubeBuffer);
        GLES20.glEnableVertexAttribArray(mAttributePosition);

        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoords, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(mTextureCoords);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(mUniformLocation, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getPreviewTexture());

        float[] matrix = new float[16];
        getSurfaceTexture().getTransformMatrix(matrix);
        GLES20.glUniformMatrix4fv(mUniformMatrix, 1, false, matrix, 0);
        final int error = GLES20.glGetError();
        if (error != 0) {
            Log.e("render", "render error " + String.format("0x%x", error));
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mAttributePosition);
        GLES20.glDisableVertexAttribArray(mTextureCoords);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public final void init() {
        mProgram = loadProgram(getVertexShader(), getFragmentShader());
        mAttributePosition = GLES20.glGetAttribLocation(mProgram, "position");
        mTextureCoords = GLES20.glGetAttribLocation(mProgram, "inputTextureCoords");
        mUniformLocation = GLES20.glGetUniformLocation(mProgram, "uTextureSampler");
        mUniformMatrix = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
    }

    public final void destroy() {
        GLES20.glDeleteProgram(mProgram);
    }

    protected abstract SurfaceTexture getSurfaceTexture();

    protected abstract int getPreviewTexture();

    protected String getFragmentShader() {
        return FRAGMENT_SHADER;
    }

    protected String getVertexShader() {
        return VERTEX_SHADER;
    }

    public static int loadProgram(final String vsh, final String fsh) {
        int vshader = loadShader(vsh, GLES20.GL_VERTEX_SHADER);
        if (vshader == 0) {
            Log.e(TAG, "failed to load vertex shader " + vsh);
            return 0;
        }

        int fshader = loadShader(fsh, GLES20.GL_FRAGMENT_SHADER);
        if (fshader == 0) {
            Log.e(TAG, "failed to load fragment shader " + fsh);
            return 0;
        }

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vshader);
        GLES20.glAttachShader(program, fshader);
        GLES20.glLinkProgram(program);

        int[] status = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] <= 0) {
            Log.e(TAG, "failed to link program");
        }

        GLES20.glDeleteShader(vshader);
        GLES20.glDeleteShader(fshader);

        return program;
    }

    public static int loadShader(String source, int type) {
        int[] status = new int[1];
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, "failed to compile shader " + source + ", type " + type);
            return 0;
        }
        return shader;
    }
}

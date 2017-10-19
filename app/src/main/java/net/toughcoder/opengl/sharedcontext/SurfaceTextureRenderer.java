package net.toughcoder.opengl.sharedcontext;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import net.toughcoder.opengl.miniglview.OpenGLESView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by alex on 17-9-16.
 */

public abstract class SurfaceTextureRenderer implements OpenGLESView.Renderer {
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

    // Dimension
    private int mInputWidth = -1;
    private int mInputHeight = -1;
    private int mTargetWidth = -1;
    private int mTargetHeight = -1;

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

    public final void init() {
        mProgram = loadProgram(getVertexShader(), getFragmentShader());
        mAttributePosition = GLES20.glGetAttribLocation(mProgram, "position");
        mTextureCoords = GLES20.glGetAttribLocation(mProgram, "inputTextureCoords");
        mUniformLocation = GLES20.glGetUniformLocation(mProgram, "uTextureSampler");
        mUniformMatrix = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
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

    public void setInputDimension(int inputWidth, int inputHeight) {
        mInputHeight = inputHeight;
        mInputWidth = inputWidth;
    }

    private void adjustDisplayScaling(float[] textureCoords, int inputWidth, int inputHeight,
                                      int targetWidth, int targetHeight) {
        float outputWidth = targetWidth;
        float outputHeight = targetHeight;

        float ratio1 = outputWidth / inputWidth;
        float ratio2 = outputHeight / inputHeight;
        float ratioMax = Math.max(ratio1, ratio2);
        int newInputWidth = Math.round(inputWidth * ratioMax);
        int newInputHeight = Math.round(inputHeight * ratioMax);

        float ratioWidth = newInputWidth / outputWidth;
        float ratioHeight =  newInputHeight / outputHeight;

        float distHorizontal = (1.0f - 1.0f / ratioWidth) / 2.0f;
        float distVertical = (1.0f - 1.0f / ratioHeight) / 2.0f;
        textureCoords = new float[] {
                addDistance(textureCoords[0], distHorizontal), addDistance(textureCoords[1], distVertical),
                addDistance(textureCoords[2], distHorizontal), addDistance(textureCoords[3], distVertical),
                addDistance(textureCoords[4], distHorizontal), addDistance(textureCoords[5], distVertical),
                addDistance(textureCoords[6], distHorizontal), addDistance(textureCoords[7], distVertical),
        };

        mTextureBuffer.clear();
        mTextureBuffer.put(textureCoords).position(0);
    }

    private static float addDistance(float coordiate, float distance) {
        if (coordiate > 0.5f) {
            return coordiate - distance;
        } else if (coordiate < 0.5f) {
            return coordiate + distance;
        } else {
            return coordiate;
        }
    }

    @Override
    public void onContextCreate() {
        // Initialize GL stuff
        GLES20.glDisable(GLES20.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        init();
    }

    @Override
    public void onContextChange(int width, int height) {
        mTargetWidth = width;
        mTargetHeight = height;
        GLES20.glViewport(0, 0, width, height);
        adjustDisplayScaling(TEXTURE_NO_ROTATION, mInputHeight, mInputWidth, width, height);
    }

    @Override
    public void onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);

        mCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mAttributePosition, 2, GLES20.GL_FLOAT, false, 0, mCubeBuffer);
        GLES20.glEnableVertexAttribArray(mAttributePosition);

        adjustDisplayScaling(TEXTURE_NO_ROTATION, mInputHeight, mInputWidth, mTargetWidth, mTargetHeight);
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

    @Override
    public void onContextDestroy() {
        GLES20.glDeleteProgram(mProgram);
    }
}

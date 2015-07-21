package net.toughcoder.opengl2s;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import net.toughcoder.effectiveandroid.R;
import net.toughcoder.oaqs.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alexhilton on 15/7/17.
 */
public class StarRenderer implements GLSurfaceView.Renderer {
    private static final String VERTEX_SHADER =
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoords;\n" +
            "uniform mat4 aMatrix;\n" +
            "varying vec2 textureCoords;\n" +
            "void main() {\n" +
            "  gl_Position = aMatrix * position;\n" +
            "  textureCoords = inputTextureCoords.xy;\n" +
            "}";
    private static final String FRAGMENT_SHADER =
            "varying highp vec2 textureCoords;\n" +
            "uniform sampler2D uTexture;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(uTexture, textureCoords);\n" +
            "}";
    private float width = 0.04f;
    private float[] edges = new float[] {
            -width, -width,
            width, -width,
            -width, width,
            width, width,
    };

    private float[] coords = new float[] {
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f,
    };

    private float[] modelMatrix = new float[16];
    private FloatBuffer mEdgeBuffer;
    private FloatBuffer mCoordBuffer;

    private int mGLProgram;
    private int mAttribPosition;
    private int mAttribTextureCoords;
    private int mAttribMatrix;
    private int mUniformTexture;
    private int mTextureId = -1;

    private Bitmap bitmap;

    private Context mContext;

    private float x0;
    private float y0;
    private int sign = 1;
    private float[] x;
    private float[] y;
    private float[] z;
    private int count = 50;

    public StarRenderer(Context ctx) {
        mContext = ctx;
        mTextureId = -1;
        x0 = 0;
        y0 = 0;
        x = new float[count];
        y = new float[count];
        z = new float[count];
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        mEdgeBuffer = ByteBuffer.allocateDirect(edges.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mEdgeBuffer.put(edges);
        mEdgeBuffer.position(0);

        mCoordBuffer = ByteBuffer.allocateDirect(coords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mCoordBuffer.put(coords);
        mCoordBuffer.position(0);

        int vsh = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vsh, VERTEX_SHADER);
        GLES20.glCompileShader(vsh);
        Log.e("fuck", "v sha " + GLES20.glGetShaderInfoLog(vsh));

        int fsh = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fsh, FRAGMENT_SHADER);
        GLES20.glCompileShader(fsh);
        Log.e("fuck", "f sha " + GLES20.glGetShaderInfoLog(fsh));

        mGLProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mGLProgram, vsh);
        GLES20.glAttachShader(mGLProgram, fsh);
        GLES20.glLinkProgram(mGLProgram);

        if (!ShaderHelper.validateProgram(mGLProgram)) {
            Log.e("fuck", "program has error, do check it ");
        }

        mAttribPosition = GLES20.glGetAttribLocation(mGLProgram, "position");
        mAttribTextureCoords = GLES20.glGetAttribLocation(mGLProgram, "inputTextureCoords");
        mUniformTexture = GLES20.glGetUniformLocation(mGLProgram, "uTexture");
        mAttribMatrix = GLES20.glGetUniformLocation(mGLProgram, "aMatrix");
//        mUniformColor = GLES20.glGetUniformLocation(mGLProgram, "uColor");
        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.star4);

        genDist();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.setIdentityM(modelMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mGLProgram);

        for (int i = 0; i < count; i++) {
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, x[i], y[i], z[i]);

            mEdgeBuffer.position(0);
            GLES20.glVertexAttribPointer(mAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mEdgeBuffer);
            GLES20.glEnableVertexAttribArray(mAttribPosition);
            mCoordBuffer.position(0);
            GLES20.glVertexAttribPointer(mAttribTextureCoords, 2, GLES20.GL_FLOAT, false, 0, mCoordBuffer);
            GLES20.glEnableVertexAttribArray(mAttribTextureCoords);

            GLES20.glUniformMatrix4fv(mAttribMatrix, 1, false, modelMatrix, 0);

            if (mTextureId == -1) {
                mTextureId = loadTexture();
            }

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glUniform1i(mUniformTexture, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
            Log.e("fuck", String.format(" active texture 0x%X", GLES20.glGetError()));

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, edges.length / 2);
            Log.e("fuck", String.format(" draw array 0x%X", GLES20.glGetError()));

            GLES20.glDisableVertexAttribArray(mAttribPosition);
            GLES20.glDisableVertexAttribArray(mAttribTextureCoords);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
    }

    private int loadTexture() {
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

    private void genDist() {
        for (int i = 0; i < count; i++) {
            sign *= -1;
            x[i] = (float) Math.random();
            x[i] *= sign;
            y[i] = (float) Math.random();
            y[i] *= sign;
            z[i] = (float) Math.random();
            z[i] *= -1;
        }
    }
}

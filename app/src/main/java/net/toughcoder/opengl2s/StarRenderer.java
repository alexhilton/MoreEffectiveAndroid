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
import java.util.Random;

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
            "varying vec4 vColor;\n" +
            "attribute vec4 aColor;\n" +
            "uniform vec3 uNormal;\n" +
            "attribute vec3 aLight;\n" +
            "void main() {\n" +
            "  gl_Position = aMatrix * position;\n" +
            "  textureCoords = inputTextureCoords.xy;\n" +
            "  vec3 modelVertex = vec3(aMatrix * position);\n" +
            "  vec3 normal = vec3(aMatrix * vec4(uNormal, 0.0));\n" +
            "  float distance = length(aLight - modelVertex);\n" +
            "  vec3 lightVector = normalize(aLight - modelVertex);\n" +
            "  float diffuse = max(dot(normal, lightVector), 0.1);\n" +
            "  diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));\n" +
            "  vColor = aColor * diffuse;\n" +
            "}";
    private static final String FRAGMENT_SHADER =
            "varying highp vec2 textureCoords;\n" +
            "uniform sampler2D uTexture;\n" +
            "varying vec4 vColor;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(uTexture, textureCoords) * vColor;\n" +
            "  gl_FragColor.rgb *= vColor.a;\n" +
            "}";
    private float width = 0.03f;
    private float[] edges = new float[] {
            -width, -width,
            width, -width,
            -width, width,
            width, width,
    };

    private float[] coords = new float[] {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f,
    };

    private float[] colors = new float[] {
            1f, 0f, 0f, 1f,
            0.5f, 0.5f, 0.5f, 1f,
            0f, 1f, 0f, 1f,
            0f, 0f, 1f, 1f,
    };

    private float[] modelMatrix = new float[16];
    private FloatBuffer mEdgeBuffer;
    private FloatBuffer mCoordBuffer;
    private FloatBuffer mColorBuffer;

    private int mGLProgram;
    private int mAttribPosition;
    private int mAttribTextureCoords;
    private int mAttribMatrix;
    private int mUniformTexture;
    private int mAttribColor;
    private int mAttribLight; // light position
    private int mUniformNormal; // normal of the plane
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
    private Random random;
    private int angle;

    public StarRenderer(Context ctx) {
        mContext = ctx;
        mTextureId = -1;
        x0 = 0;
        y0 = 0;
        x = new float[count];
        y = new float[count];
        z = new float[count];
        random = new Random();
        angle = 0;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

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

        mColorBuffer = ByteBuffer.allocateDirect(colors.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);

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
        mAttribColor = GLES20.glGetAttribLocation(mGLProgram, "aColor");
        mAttribLight = GLES20.glGetAttribLocation(mGLProgram, "aLight");
        mUniformNormal = GLES20.glGetUniformLocation(mGLProgram, "uNormal");

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
//        genDist();
        for (int i = 0; i < count; i++) {
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, x[i], y[i], z[i]);
//            Matrix.rotateM(modelMatrix, 0, angle, 0f, 0f, 1.0f);

            mEdgeBuffer.position(0);
            GLES20.glVertexAttribPointer(mAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mEdgeBuffer);
            GLES20.glEnableVertexAttribArray(mAttribPosition);
            mCoordBuffer.position(0);
            GLES20.glVertexAttribPointer(mAttribTextureCoords, 2, GLES20.GL_FLOAT, false, 0, mCoordBuffer);
            GLES20.glEnableVertexAttribArray(mAttribTextureCoords);

            GLES20.glVertexAttrib4f(mAttribColor, .91f, .97f, .91f, 1f);

            GLES20.glUniformMatrix4fv(mAttribMatrix, 1, false, modelMatrix, 0);

            GLES20.glVertexAttrib3f(mAttribLight, x[i], y[i], random.nextFloat());
            GLES20.glUniform3f(mUniformNormal, 0f, 0f, 1f);

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

            angle += 5;
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
            x[i] = random.nextFloat();
            x[i] *= sign;
            y[i] = random.nextFloat();
            y[i] *= sign;
            z[i] = 0f;
        }
    }
}

package net.toughcoder.opengl2s;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import net.toughcoder.oaqs.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alexhilton on 15/8/10.
 */
public class SimpleRenderer implements GLSurfaceView.Renderer {
    private static final String VERTEX_SHADER =
            "attribute vec4 position;" +
            "void main() {\n" +
            "  gl_Position = position;\n" +
            "}";
    private static final String FRAGMENT_SHADER =
            "void main() {\n" +
            "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);\n" +
            "}";
    private float[] triangle = {
            -0.5f, -0.5f,
            0.5f, -0.5f,
            0.0f, 0.5f,
    };

    private int mGLProgram;
    private int mAttribPosition;

    private FloatBuffer triangleBuffer;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);

        triangleBuffer = ByteBuffer.allocateDirect(triangle.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        triangleBuffer.put(triangle);

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
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mGLProgram);

        GLES20.glEnableVertexAttribArray(mAttribPosition);
        triangleBuffer.position(0);
        GLES20.glVertexAttribPointer(mAttribPosition, 2, GLES20.GL_FLOAT, false, 0, triangleBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

        GLES20.glDisableVertexAttribArray(mAttribPosition);
    }
}

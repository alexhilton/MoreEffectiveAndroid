package net.toughcoder.oaqs;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import net.toughcoder.effectiveandroid.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alexhilton on 15/6/29.
 */
public class AirHockeyRenderer implements GLSurfaceView.Renderer {
    private static final int BYTES_PER_FLOAT = 4;
    private static final String U_COLOR = "u_Color";
    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;
    private int uColorLocation;
    private float[] tableVertices = {
            // the table, first triangle
            -.5f, -.5f,
            .5f, .5f,
            -.5f, .5f,
            // second triangle
            -.5f, -.5f,
            .5f, -.5f,
            .5f, .5f,

            // line
            -.5f, 0f,
            .5f, 0f,
            // mallets
            0f, -.25f,
            0f, .25f,

            // Puck
            0f, 0f,

            // borders
            -.5f, -.5f,
            .5f, -.5f,
            .5f, .5f,
            -.5f, .5f,
    };

    private FloatBuffer vertexData;

    private Context context;

    private int program;

    public AirHockeyRenderer(Context ctx) {
        context = ctx;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(.0f, .0f, .0f, .0f);
        vertexData = ByteBuffer.allocateDirect(tableVertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(tableVertices);
        vertexData.position(0);

        int vertexShader = ShaderHelper.compileVertexShader(Utils.readTextFileFromResource(context, R.raw.simple_vertex_shader));
        int fragmentShader = ShaderHelper.compibleFragmentShader(Utils.readTextFileFromResource(context, R.raw.simple_fragment_shader));
        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);
        ShaderHelper.validateProgram(program);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(program);

        uColorLocation = GLES20.glGetUniformLocation(program, U_COLOR);
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
        vertexData.position(0);

        GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 0, vertexData);
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        GLES20.glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES20.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);

        GLES20.glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1);

        GLES20.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1);

        GLES20.glUniform4f(uColorLocation, .5f, .5f, 0f, 1.f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 10, 1);

        GLES20.glUniform4f(uColorLocation, .0f, 1.f, 1.f, 1.f);
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 11, 4);

        GLES20.glFinish();
    }
}

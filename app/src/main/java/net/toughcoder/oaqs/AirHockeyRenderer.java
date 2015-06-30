package net.toughcoder.oaqs;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

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
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private static final String A_COLOR = "a_Color";
    private static final String A_POSITION = "a_Position";
    private static final String U_MATRIX = "u_Matrix";

    private final float[] projectionMatrix = new float[16];
    private int uMatrixLocation;
    private int aPositionLocation;
    private int aColorLocation;
    private float[] tableVertices = {
            // the table, first triangle
            0, 0, 1f, 1f, 1f,
            -.5f, -.8f, .7f, .7f, .7f,
            .5f, -.8f, .7f, .7f, .7f,
            .5f, .8f, .7f, .7f, .7f,
            -.5f, .8f, .7f, .7f, .7f,
            -.5f, -.8f, .7f, .7f, .7f,

            // line
            -.8f, 0f, 1.f, 0f, 0f,
            .8f, 0f, 0f, 1f, 0.f, 0.f,
            // mallets
            0f, -.4f, .7f, .7f, 0f,
            0f, .4f, 0f, .7f, .7f,

            // Puck
            0f, 0f, .9f, 0, .7f,

            // borders
            -.8f, -.8f, .7f, .2f, .5f,
            .8f, -.8f, .3f, .7f, .3f,
            .8f, .8f, .8f, .4f, .2f,
            -.8f, .8f, .8f, .2f, .5f,
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

        GLES20.glUseProgram(program);

        aColorLocation = GLES20.glGetAttribLocation(program, A_COLOR);
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);

        vertexData.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT,
                GLES20.GL_FLOAT, false, STRIDE, vertexData);
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        vertexData.position(POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GLES20.GL_FLOAT,
                false, STRIDE, vertexData);
        GLES20.glEnableVertexAttribArray(aColorLocation);

        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        final float ratio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;
        if (width > height) {
            Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f);
        } else {
            Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -ratio, ratio, -1f, 1f);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);

        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 10, 1);

        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 11, 4);

        GLES20.glFinish();
    }
}

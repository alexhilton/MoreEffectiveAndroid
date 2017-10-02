package net.toughcoder.opengl.miniglview;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
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

public class TriangleRenderer implements GLSurfaceView.Renderer {
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

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
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
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        mTriangle.position(0);
        GLES20.glVertexAttribPointer(mAttributePosition, 2, GLES20.GL_FLOAT, false, 0, mTriangle);
        GLES20.glEnableVertexAttribArray(mAttributePosition);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        Log.d("lala", "draw " + GLES20.glGetError());
        GLES20.glDisableVertexAttribArray(mAttributePosition);
    }
}

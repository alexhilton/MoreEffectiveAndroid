package net.toughcoder.opengl2s;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.FloatMath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alexhilton on 15/7/28.
 *
 * This is the two dimension example.
 * A shinny sun, with lights.
 */
public class SunRenderer implements GLSurfaceView.Renderer {
    private static final String VERTEX_SHADER =
            "attribute vec4 position;\n" +
            "void main() {\n" +
            "  gl_Position = position;\n" +
            "}";
    private static final String FRAGMENT_SHADER =
            "precision highp float;\n" +
            "void main() {\n" +
            "  gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);\n" +
            "}";

    private Circle mCircle;
    private int mProgram;

    private int mAttribPosition;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);
        mCircle = new Circle(0, 0, 0, 0.5f, 100);

        mProgram = GLES20.glCreateProgram();
        int vsh = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vsh, VERTEX_SHADER);
        GLES20.glCompileShader(vsh);

        int fsh = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fsh, FRAGMENT_SHADER);
        GLES20.glCompileShader(fsh);

        GLES20.glAttachShader(mProgram, vsh);
        GLES20.glAttachShader(mProgram, fsh);
        GLES20.glLinkProgram(mProgram);

        mAttribPosition = GLES20.glGetAttribLocation(mProgram, "position");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        mCircle.draw(mAttribPosition);
    }

    private static class Circle {
        private float x;
        private float y;
        private float z;
        private float radius;
        private final FloatBuffer mVertices;
        private int count;

        public Circle(float x, float y, float z, float radius, int count) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
            this.count = count;
            mVertices = genModel(count);
        }

        private FloatBuffer genModel(int count) {
            float[] vertices = genVertices(count);
            FloatBuffer buf = ByteBuffer.allocateDirect(vertices.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            buf.position(0);
            buf.put(vertices);
            return buf;
        }

        private float[] genVertices(int count) {
            float[] v = new float[(count + 2) * 2];
            int offset = 0;
            v[offset++] = x;
            v[offset++] = y;
            for (int i = 0; i <= count; i++) {
                float angle = ((float) Math.PI * 2.0f * i) / (float) count;
                v[offset++] = x + radius * FloatMath.cos(angle);
                v[offset++] = y + radius * FloatMath.sin(angle);
            }
            return v;
        }

        public void draw(int attribPosition) {
            GLES20.glEnableVertexAttribArray(attribPosition);
            // Remember to position the buffer, otherwise you will get error
            mVertices.position(0);
            GLES20.glVertexAttribPointer(attribPosition, 2, GLES20.GL_FLOAT, false, 0, mVertices);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, count + 2);
            GLES20.glDisableVertexAttribArray(attribPosition);
        }
    }
}

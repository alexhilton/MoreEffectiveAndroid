package net.toughcoder.opengl.opengl2s;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import net.toughcoder.opengl.oaqs.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

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
    private Rays mRay;

    private int mProgram;

    private int mAttribPosition;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);
        mCircle = new Circle(0, 0, 0, 0.3f, 100);
        mRay = new Rays(mCircle, 0.5f, 20);

        mProgram = ShaderHelper.buildProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        GLES20.glUseProgram(mProgram);
        mAttribPosition = GLES20.glGetAttribLocation(mProgram, "position");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glEnableVertexAttribArray(mAttribPosition);
        mCircle.onDraw(mAttribPosition);
        mRay.onDraw(mAttribPosition);
        GLES20.glDisableVertexAttribArray(mAttribPosition);
    }

    private static class Circle extends Model {
        private float x;
        private float y;
        private float z;
        private float radius;
        private final int count;

        public Circle(float x, float y, float z, float radius, int count) {
            super((count + 2) * 2);
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
            this.count = count;
        }

        @Override
        public float[] genVertices() {
            float[] v = new float[getCount() * 2];
            int offset = 0;
            v[offset++] = x;
            v[offset++] = y;
            for (int i = 0; i <= count; i++) {
                float angle = ((float) Math.PI * 2.0f * i) / (float) count;
                v[offset++] = (float) (x + radius * Math.cos(angle));
                v[offset++] = (float) (y + radius * Math.sin(angle));
            }
            return v;
        }

        @Override
        public void onPostDraw() {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, getCount());
        }

        @Override
        public int getCount() {
            return count + 2;
        }
    }

    private static class Rays extends Model {
        private Circle mCircle;
        private float radius;
        private int count;

        private final Random random;

        public Rays(Circle circle, float radius, int count) {
            super(count * 4);
            mCircle = circle;
            this.radius = radius;
            this.count = count;

            random = new Random();
        }

        @Override
        public float[] genVertices() {
            float[] v = new float[count * 4];
            int offset = 0;
            for (int i = 0; i < count; i++) {
                float angle = (float) Math.PI * 2f * (float) i / (float) count;
                float ep = random.nextFloat() * (radius - mCircle.radius) / 2;
                v[offset++] = (float) (mCircle.x + (mCircle.radius + ep) * Math.cos(angle));
                v[offset++] = (float) (mCircle.y + (mCircle.radius + ep) * Math.sin(angle));
                v[offset++] = (float) (mCircle.x + (radius + ep) * Math.cos(angle));
                v[offset++] = (float) (mCircle.y + (radius + ep) * Math.sin(angle));
            }

            return v;
        }

        @Override
        public void onPostDraw() {
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, getCount());
        }

        @Override
        public int getCount() {
            return count * 2;
        }
    }

    interface Geometry {
        void onDraw(int position);
    }

    static abstract class Model implements Geometry {
        private FloatBuffer vertexBuffer;

        public Model(int verticeCount) {
            vertexBuffer = ByteBuffer.allocateDirect(verticeCount * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
        }

        public void onDraw(int position) {
            vertexBuffer.position(0);
            vertexBuffer.put(genVertices());
            vertexBuffer.position(0);

            GLES20.glVertexAttribPointer(position, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
            onPostDraw();
        }

        abstract void onPostDraw();

        abstract float[] genVertices();

        abstract int getCount();
    }
}

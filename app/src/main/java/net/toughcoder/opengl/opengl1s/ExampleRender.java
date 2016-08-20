package net.toughcoder.opengl.opengl1s;

import android.opengl.GLSurfaceView;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alexhilton on 15/6/9.
 */
public class ExampleRender implements GLSurfaceView.Renderer {
    private float rotateTri;
    private float rotateQuad;
    private int one = 0x10000;

    private int[] triggerBuffer = new int[] {
            0, one, 0,
            -one, -one, 0,
            one, -one, 0
    };

    private int[] quateBuffer = new int[] {
            one, one, 0,
            -one, -one, 0,
            one, -one, 0,
            -one, -one, 0
    };

    private int[] colorBuffer = new int[] {
            one, 0, 0, one,
            0, one, 0, one,
            0, 0, one, one,
    };

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glClearColorx(0, 0, 0, 0);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float ratio = (float) width / height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        gl.glTranslatef(-1.5f, 0.0f, -6.0f);
        gl.glRotatef(rotateTri, 0.0f, 1.0f, 0.0f);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glColorPointer(4, GL10.GL_FIXED, 0, bufferUtil(colorBuffer));
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, bufferUtil(triggerBuffer));
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        gl.glFinish();

        gl.glLoadIdentity();
        gl.glTranslatef(1.5f, 0.0f, -6.0f);
        gl.glColor4f(0.5f, 0.5f, 1.0f, 1.0f);
        gl.glRotatef(rotateQuad, 1.0f, 0.0f, 0.0f);

        gl.glVertexPointer(3, GL10.GL_FIXED, 0, bufferUtil(quateBuffer));
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        gl.glFinish();
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        rotateTri += 0.5f;
        rotateQuad -= 0.5f;
    }

    private Buffer bufferUtil(int[] array) {
        IntBuffer buffer;
        ByteBuffer bf = ByteBuffer.allocateDirect(array.length * 4);
        bf.order(ByteOrder.nativeOrder().nativeOrder());
        buffer = bf.asIntBuffer();
        buffer.put(array);
        buffer.position(0);

        return buffer;
    }
}

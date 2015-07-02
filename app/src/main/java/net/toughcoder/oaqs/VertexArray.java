package net.toughcoder.oaqs;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by alexhilton on 15/7/1.
 */
public class VertexArray {

    private FloatBuffer floatBuffer;

    public VertexArray(float[] data) {
        floatBuffer = ByteBuffer.allocateDirect(data.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(data);
    }

    public void setVertexAttribPointer(int offset, int location, int count, int stride) {
        floatBuffer.position(offset);
        GLES20.glVertexAttribPointer(location, count, GLES20.GL_FLOAT, false, stride, floatBuffer);
        GLES20.glEnableVertexAttribArray(location);
        floatBuffer.position(0);
    }
}

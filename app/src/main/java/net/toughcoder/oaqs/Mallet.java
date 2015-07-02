package net.toughcoder.oaqs;

import android.opengl.GLES20;

/**
 * Created by alexhilton on 15/7/1.
 */
public class Mallet {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;

    private static final float[] vertex = {
            0f, -.4f, 0f, 0f, 1f,
            0f, .4f, 1f, 0f, 0f,
    };

    private final VertexArray data;

    public Mallet() {
        data = new VertexArray(vertex);
    }

    public void bindData(ColorShaderProgram program) {
        data.setVertexAttribPointer(0,
                program.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        data.setVertexAttribPointer(POSITION_COMPONENT_COUNT,
                program.getColorAttributeLocation(),
                COLOR_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 2);
    }
}

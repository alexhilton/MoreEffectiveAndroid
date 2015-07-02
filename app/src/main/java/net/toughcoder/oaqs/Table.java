package net.toughcoder.oaqs;

import android.opengl.GLES20;

/**
 * Created by alexhilton on 15/7/1.
 */
public class Table {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;
    private float[] tableVertices = {
            // the table, first triangle
            0, 0, .5f, .5f,
            -.5f, -.8f, 0f, .9f,
            .5f, -.8f, 1f, .9f,
            .5f, .8f, 1f, .1f,
            -.5f, .8f, 0f, .1f,
            -.5f, -.8f, 0f, .9f,
    };

    private final VertexArray data;

    public Table() {
        data = new VertexArray(tableVertices);
    }

    public void bindData(TextureShaderProgram program) {
        data.setVertexAttribPointer(0, program.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        data.setVertexAttribPointer(POSITION_COMPONENT_COUNT,
                program.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);
    }
}

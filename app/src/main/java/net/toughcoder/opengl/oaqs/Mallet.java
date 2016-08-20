package net.toughcoder.opengl.oaqs;

import java.util.List;

/**
 * Created by alexhilton on 15/7/1.
 */
public class Mallet {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;

    public final float radius;
    public final float height;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Mallet(float radius, float height, int numPointsAroundMallet) {
        ObjectBuilder.GeneratedData generatedData =
                ObjectBuilder.createMallet(new Point(0f, 0f, 0f), radius, height, numPointsAroundMallet);
        this.radius = radius;
        this.height = height;
        vertexArray = new VertexArray(generatedData.vertexData);
        drawList = generatedData.drawList;
    }

    public void bindData(ColorShaderProgram program) {
        vertexArray.setVertexAttribPointer(0,
                program.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                0);
    }

    public void draw() {
        for (ObjectBuilder.DrawCommand cmd : drawList) {
            cmd.draw();
        }
    }
}

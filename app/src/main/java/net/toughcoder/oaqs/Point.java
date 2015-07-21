package net.toughcoder.oaqs;

/**
 * Created by alexhilton on 15/7/4.
 */
public class Point {
    public final float x, y, z;

    public Point(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point translateY(float delta) {
        return new Point(x, y + delta, z);
    }

    public Point translate(Geometry.Vector vector) {
        return new Point(x + vector.x, y + vector.y, z + vector.z);
    }
}

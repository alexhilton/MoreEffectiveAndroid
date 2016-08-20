package net.toughcoder.opengl.oaqs;

/**
 * Created by alexhilton on 15/7/4.
 */
public class Circle {
    public final Point center;
    public final float radius;

    public Circle(Point center, float radius) {
        this.center = center;
        this.radius = radius;
    }

    public Circle scale(float scale) {
        return new Circle(center, scale * radius);
    }
}

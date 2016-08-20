package net.toughcoder.opengl.oaqs;

import android.util.FloatMath;

/**
 * Created by alexhilton on 15/7/5.
 */
public class Geometry {
    public static class Ray {
        public final Point point;
        public final Vector vector;

        public Ray(Point point, Vector vector) {
            this.point = point;
            this.vector = vector;
        }
    }

    public static class Vector {
        public final float x, y, z;
        public Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float length() {
            return FloatMath.sqrt(x * x + y * y + z * z);
        }

        public Vector crossProduct(Vector other) {
            return new Vector(
                    (y * other.z) - (z * other.y),
                    (z * other.x) - (x * other.z),
                    (x * other.y) - (y * other.x));
        }

        public float dotProduct(Vector other) {
            return x * other.x + y * other.y + z * other.z;
        }

        public Vector scale(float f) {
            return new Vector(x * f, y * f, z * f);
        }
    }

    public static class Sphere {
        public final Point center;
        public final float radius;

        public Sphere(Point center, float radius) {
            this.center = center;
            this.radius = radius;
        }
    }

    public static class Plane {
        public final Point point;
        public final Vector normal;

        public Plane(Point point, Vector normal) {
            this.point = point;
            this.normal = normal;
        }
    }

    public static Vector vectorBetween(Point from, Point to) {
        return new Vector(to.x - from.x, to.y - from.y, to.z - from.z);
    }

    public static boolean intersects(Sphere sphere, Ray ray) {
        return distanceBetween(sphere.center, ray) < sphere.radius;
    }

    public static float distanceBetween(Point point, Ray ray) {
        Vector p12Point = vectorBetween(ray.point, point);
        Vector p22Point = vectorBetween(ray.point.translate(ray.vector), point);

        float areaOfTriangleTimesTwo = p12Point.crossProduct(p22Point).length();
        float lengthOfBase = ray.vector.length();

        float distance = areaOfTriangleTimesTwo / lengthOfBase;
        return distance;
    }

    public static Point intersectionPoint(Ray ray, Plane plane) {
        Vector ray2PlaneVector = vectorBetween(ray.point, plane.point);

        float scaleFactor = ray2PlaneVector.dotProduct(plane.normal) / ray.vector.dotProduct(plane.normal);
        Point point = ray.point.translate(ray.vector.scale(scaleFactor));
        return point;
    }
}

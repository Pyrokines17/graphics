package ru.nsu.components.scene.algebra;

public class Vector {
    private double x;
    private double y;
    private double z;
    private double w;

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = 1.0;
    }

    public Vector(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public double get(int index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            case 3 -> w;
            default -> throw new IndexOutOfBoundsException("Index must be between 0 and 3.");
        };
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public double getLength() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector normalize() {
        double length = getLength();

        if (length == 0) {
            return new Vector(0, 0, 0);
        }

        return new Vector(x / length, y / length, z / length);
    }

    public Vector multiply(double scalar) {
        return new Vector(x * scalar, y * scalar, z * scalar);
    }

    public static Vector add(Vector v1, Vector v2) {
        return new Vector(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }

    public static Vector subtract(Vector v1, Vector v2) {
        return new Vector(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    public static Vector cross(Vector v1, Vector v2) {
        return new Vector(
                v1.y * v2.z - v1.z * v2.y,
                v1.z * v2.x - v1.x * v2.z,
                v1.x * v2.y - v1.y * v2.x
        );
    }

    public static Vector divide(Vector v, double scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("Cannot divide by zero.");
        }
        return new Vector(v.x / scalar, v.y / scalar, v.z / scalar);
    }

    public static double dot(Vector v1, Vector v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public static double getLength(Vector v) {
        return Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
    }

    public static double getLengthSquared(Vector v) {
        return v.x * v.x + v.y * v.y + v.z * v.z;
    }

    public static Vector random() {
        return new Vector(Math.random(), Math.random(), Math.random());
    }

    public static Vector random(double min, double max) {
        return new Vector(
            min + Math.random() * (max - min),
            min + Math.random() * (max - min),
            min + Math.random() * (max - min)
        );
    }

    public static Vector randomUnitVector() {
        while (true) {
            var p = Vector.random(-1, 1);
            var lengthSquared = Vector.getLengthSquared(p);
            if (lengthSquared <= 1 && 1e-160 < lengthSquared) {
                return Vector.divide(p, Math.sqrt(lengthSquared));
            }
        }
    }

    public static Vector randomOnHemisphere(Vector normal) {
        Vector inUnitSphere = randomUnitVector();

        if (Vector.dot(inUnitSphere, normal) > 0.0) {
            return inUnitSphere;
        } else {
            return inUnitSphere.multiply(-1.0);
        }
    }

    public boolean nearZero() {
        final double epsilon = 1e-8;
        return Math.abs(x) < epsilon && Math.abs(y) < epsilon && Math.abs(z) < epsilon;
    }

    public static Vector reflect(Vector v, Vector normal) {
        return subtract(v, normal.multiply(Vector.dot(v, normal)*2));
    }

    public static Vector multiply(Vector v, Vector other) {
        return new Vector(v.x * other.x, v.y * other.y, v.z * other.z);
    }
}

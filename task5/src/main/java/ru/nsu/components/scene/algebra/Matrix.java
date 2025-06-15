package ru.nsu.components.scene.algebra;

public class Matrix {
    public static double[][] multiply(double[][] a, double[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int rowsB = b.length;
        int colsB = b[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Incompatible matrix dimensions for multiplication.");
        }

        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                result[i][j] = 0;

                for (int k = 0; k < colsA; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }

        return result;
    }

    public static Vector multiply(double[][] matrix, Vector vector) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        if (cols != 4) {
            throw new IllegalArgumentException("Matrix must have 4 columns to multiply with a 4D vector.");
        }

        double[] result = new double[rows];

        for (int i = 0; i < rows; i++) {
            result[i] = 0;

            for (int j = 0; j < cols; j++) {
                result[i] += matrix[i][j] * vector.get(j);
            }
        }

        return new Vector(result[0], result[1], result[2], result[3]);
    }

    public static double[][] getTranslationMatrix(double tx, double ty, double tz) {
        return new double[][]{
            {1, 0, 0, tx},
            {0, 1, 0, ty},
            {0, 0, 1, tz},
            {0, 0, 0, 1}
        };
    }

    public static double[][] getScalingMatrix(double sx, double sy, double sz) {
        return new double[][]{
            {sx, 0, 0, 0},
            {0, sy, 0, 0},
            {0, 0, sz, 0},
            {0, 0, 0, 1}
        };
    }

    public static double[][] getRotationMatrix(double angle, char axis) {
        double radians = Math.toRadians(angle);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        return switch (axis) {
            case 'x' -> new double[][]{
                    {1, 0, 0, 0},
                    {0, cos, -sin, 0},
                    {0, sin, cos, 0},
                    {0, 0, 0, 1}
            };
            case 'y' -> new double[][]{
                    {cos, 0, sin, 0},
                    {0, 1, 0, 0},
                    {-sin, 0, cos, 0},
                    {0, 0, 0, 1}
            };
            case 'z' -> new double[][]{
                    {cos, -sin, 0, 0},
                    {sin, cos, 0, 0},
                    {0, 0, 1, 0},
                    {0, 0, 0, 1}
            };
            default -> throw new IllegalArgumentException("Invalid axis for rotation. Use 'x', 'y', or 'z'.");
        };
    }

    public static double[][] getLookAt(Vector eye, Vector target, Vector up) {
        Vector vz = Vector.subtract(eye, target).normalize();
        Vector vx = Vector.cross(up, vz).normalize();
        Vector vy = Vector.cross(vz, vx).normalize();

        return Matrix.multiply(
                new double[][]{
                        {vx.getX(), vx.getY(), vx.getZ(), 0},
                        {vy.getX(), vy.getY(), vy.getZ(), 0},
                        {vz.getX(), vz.getY(), vz.getZ(), 0},
                        {0, 0, 0, 1}
                },
                Matrix.getTranslationMatrix(-eye.getX(), -eye.getY(), -eye.getZ()));
    }

    public static double[][] getPerspectiveProjectionMatrix(double fov, double aspect, double n, double f) {
        double radians = Math.toRadians(fov);
        double scale = 1 / Math.tan(radians / 2);
        double sx = scale / aspect;
        double sy = scale;
        double sz = -1 * (f + n) / (f - n);
        double dz = -1 * (2 * f * n) / (f - n);

        return new double[][]{
                {sx, 0, 0, 0},
                {0, sy, 0, 0},
                {0, 0, sz, dz},
                {0, 0, -1, 0}
        };
    }

    public static double[][] getAltPerspectiveProjectionMatrix(double weight, double height, double n, double f) {
        double sx = 2 * n / weight;
        double sy = 2 * n / height;
        double sz = f / (f - n);
        double dz = -1 * (f * n) / (f - n);

        return new double[][]{
                {sx, 0, 0, 0},
                {0, sy, 0, 0},
                {0, 0, sz, dz},
                {0, 0, 1, 0}
        };
    }
}

package ru.nsu.components.scene;

import ru.nsu.components.Model;
import ru.nsu.components.scene.algebra.Ray;
import ru.nsu.components.scene.algebra.Vector;
import ru.nsu.components.scene.figures.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Logic {
    private static int imageHeight;
    private static final int imageWidth = 640;
    private static double aspectRatio = 16.0 / 10.0;
    private static final int samplesPerPixel = 250;
    private static final int maxDepth = 50;
    private static final double vfov = 90.0;
    private static Vector center;
    private static Vector pixel00Loc;
    private static Vector pixelDeltaU;
    private static Vector pixelDeltaV;
    private static double pixelSamplesScale;

    private static Vector u, v, w;

    private static Vector lookFrom = new Vector(0, 1, 0);
    private static Vector lookAt = new Vector(0, 0, -1);
    private static Vector up = new Vector(0, 1, 0);

    private static Model model = null;
    private static Vector backgroundColor = new Vector(0.1, 0.1, 0.1);

    public static void setModel(Model model) {
        Logic.model = model;
    }

    private static void initialize() {
        imageHeight = (int) (imageWidth / aspectRatio);
        imageHeight = Math.max(imageHeight, 1);

        pixelSamplesScale = 1.0 / samplesPerPixel;

        center = lookFrom;

        double focalLength = Vector.subtract(lookFrom, lookAt).getLength();
        double theta = Math.toRadians(vfov);
        double h = Math.tan(theta / 2.0);
        double viewportHeight = 2.0 * h * focalLength;
        double viewportWidth = viewportHeight * ((double) imageWidth / imageHeight);

        w = Vector.subtract(lookFrom, lookAt).normalize();
        u = Vector.cross(up, w).normalize();
        v = Vector.cross(w, u);

        Vector viewportU = u.multiply(viewportWidth);
        Vector viewportV = v.multiply(-viewportHeight);

        pixelDeltaU = Vector.divide(viewportU, imageWidth);
        pixelDeltaV = Vector.divide(viewportV, imageHeight);

        Vector viewportUpperLeft = Vector.subtract(
            Vector.subtract(
                Vector.subtract(center, Vector.divide(viewportU, 2)),
                Vector.divide(viewportV, 2)
            ),
                w.multiply(focalLength)
        );
        pixel00Loc = Vector.add(
            viewportUpperLeft,
            Vector.add(pixelDeltaU.multiply(0.5), pixelDeltaV.multiply(0.5))
        );
    }

    public static double[][][] get3DPoints(ArrayList<Point2D> splinePoints, int M) {
        double[][][] points = new double[M][3][splinePoints.size()];
        double angleStepInDegrees = 360.0 / M;

        for (int i = 0; i < M; i++) {
            double angleInDegrees = i * angleStepInDegrees;
            double angleInRadians = Math.toRadians(angleInDegrees);
            double cosAngle = Math.cos(angleInRadians);
            double sinAngle = Math.sin(angleInRadians);

            for (int j = 0; j < splinePoints.size(); j++) {
                Point2D point = splinePoints.get(j);
                points[i][0][j] = point.getY() * cosAngle;
                points[i][1][j] = point.getY() * sinAngle;
                points[i][2][j] = point.getX();
            }
        }

        return points;
    }

    private static Vector getColorForRay(Ray ray, int depth, Hittable world) {
        if (depth <= 0) {
            return new Vector(0, 0, 0);
        }

        HitRecord record = new HitRecord();

        if (!world.hit(ray, new Interval(0.001, Double.POSITIVE_INFINITY), record)) {
            return backgroundColor;
        }

        Ray scattered = new Ray(new Vector(0, 0, 0), new Vector(0, 0, 0));
        Vector attenuation = new Vector(0, 0, 0);
        Vector colorFromEmission = record.getMaterial().emitted(record.getU(), record.getV(), record.getPoint());

        if (!record.getMaterial().scatter(ray, record, attenuation, scattered)) {
            return colorFromEmission;
        }

        Vector color = getColorForRay(scattered, depth - 1, world);
        Vector tmpColor = Vector.multiply(attenuation, color);
        return Vector.add(colorFromEmission, tmpColor);
    }

    private static Vector sampleSquare() {
        return new Vector(Math.random() - 0.5, Math.random() - 0.5, 0);
    }

    private static Ray getRay(int i, int j) {
        Vector offset = sampleSquare();
        var pixelSample = Vector.add(
            pixel00Loc,
            Vector.add(
                pixelDeltaU.multiply(i + offset.getX()),
                pixelDeltaV.multiply(j + offset.getY())
            )
        );

        var rayOrigin = center;
        var rayDirection = Vector.subtract(pixelSample, rayOrigin);

        return new Ray(rayOrigin, rayDirection);
    }

    private static double linToGamma(double x) {
        if (x > 0) {
            return Math.sqrt(x);
        }

        return 0;
    }

    public static BufferedImage drawRays() {
        initialize();
        HittableList world;

        if (model != null) {
            world = model.getHittableList();
        } else {
            world = new HittableList();
        }

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Interval intensityRange = new Interval(0.000, 0.999);

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                Vector pixelColor = new Vector(0, 0, 0);

                for (int sample = 0; sample < samplesPerPixel; sample++) {
                    Ray ray = getRay(x, y);
                    Vector color = getColorForRay(ray, maxDepth, world);
                    pixelColor = Vector.add(pixelColor, new Vector(color.getX(), color.getY(), color.getZ()));
                }

                pixelColor = pixelColor.multiply(pixelSamplesScale);

                var r = (int)(256 * intensityRange.clamp(pixelColor.getX()));
                var g = (int)(256 * intensityRange.clamp(pixelColor.getY()));
                var b = (int)(256 * intensityRange.clamp(pixelColor.getZ()));

                image.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return image;
    }

    public static ArrayList<Vector> getCirclePoints(Vector center, double radius, int numPoints, String plane) {
        ArrayList<Vector> points = new ArrayList<>();
        double deltaTheta = 2 * Math.PI / numPoints;

        switch (plane) {
            case "XY":
                for (int i = 0; i < numPoints; i++) {
                    double theta = i * deltaTheta;
                    double x = center.getX() + radius * Math.cos(theta);
                    double y = center.getY() + radius * Math.sin(theta);
                    points.add(new Vector(x, y, center.getZ()));
                }
                break;
            case "XZ":
                for (int i = 0; i < numPoints; i++) {
                    double theta = i * deltaTheta;
                    double x = center.getX() + radius * Math.cos(theta);
                    double z = center.getZ() + radius * Math.sin(theta);
                    points.add(new Vector(x, center.getY(), z));
                }
                break;
            case "YZ":
                for (int i = 0; i < numPoints; i++) {
                    double theta = i * deltaTheta;
                    double y = center.getY() + radius * Math.cos(theta);
                    double z = center.getZ() + radius * Math.sin(theta);
                    points.add(new Vector(center.getX(), y, z));
                }
                break;
        }

        return points;
    }
}

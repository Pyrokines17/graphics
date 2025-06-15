package ru.nsu.components.redactor;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class SplineLogic {
    public static List<ArrayList<Point2D>> getSplinePath(ArrayList<Point2D> points, int n) {
        ArrayList<Point2D> controlPoints = new ArrayList<>();
        ArrayList<Point2D> basePoints = new ArrayList<>();

        if (points.size() < 4) {
            return new ArrayList<>();
        }

        for (int i = 0; i < points.size() - 3; ++i) {
            Point2D p0 = points.get(i);
            Point2D p1 = points.get(i + 1);
            Point2D p2 = points.get(i + 2);
            Point2D p3 = points.get(i + 3);

            for (double t = 0; t <= n; ++t) {
                if (i < points.size() - 4 && t == n) {
                    continue;
                }

                double q = t / n;
                Point2D point = evaluateBSpline(q, p0, p1, p2, p3);

                if (t == 0) {
                    basePoints.add(point);
                }

                controlPoints.add(point);
            }
        }

        basePoints.add(controlPoints.getLast());

        List<ArrayList<Point2D>> allPoints = new ArrayList<>();
        allPoints.add(controlPoints);
        allPoints.add(basePoints);

        return allPoints;
    }

    private static Point2D evaluateBSpline(double t, Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
        double t2 = t * t;
        double t3 = t2 * t;

        double b0 = (-t3 + 3*t2 - 3*t + 1) / 6.0;
        double b1 = (3*t3 - 6*t2 + 4) / 6.0;
        double b2 = (-3*t3 + 3*t2 + 3*t + 1) / 6.0;
        double b3 = t3 / 6.0;

        double x = b0 * p0.getX() + b1 * p1.getX() + b2 * p2.getX() + b3 * p3.getX();
        double y = b0 * p0.getY() + b1 * p1.getY() + b2 * p2.getY() + b3 * p3.getY();
        return new Point2D.Double(x, y);
    }
}

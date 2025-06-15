package ru.nsu.components.scene;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Logic {
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
}

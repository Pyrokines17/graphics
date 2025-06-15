package ru.nsu.components.scene;

import ru.nsu.components.Model;
import ru.nsu.components.scene.algebra.Matrix;
import ru.nsu.components.scene.algebra.Vector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class View3D extends JPanel {
    private final Model model;

    private Point lastMousePos = null;
    private boolean isDragging = false;
    double[][] rotationMatrix = Matrix.getRotationMatrix(90, 'z');
    private final double sensitivity = 0.5;
    private double nearPlane = 2000;

    private double fullX = 0;
    private double fullY = 0;

    private final double[][] XYZ = {
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1},
            {0, 0, 0}
    };

    public View3D(Model model) {
        this.model = model;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    lastMousePos = e.getPoint();
                    isDragging = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    isDragging = false;
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    Point currentPos = e.getPoint();

                    double deltaX = currentPos.x - lastMousePos.x;
                    double deltaY = lastMousePos.y - currentPos.y;

                    double angleX = deltaX * sensitivity;
                    double angleY = deltaY * sensitivity;

                    fullX += angleX;
                    fullY += angleY;

                    updateRotMatrix();

                    lastMousePos = currentPos;

                    repaint();
                }
            }
        });

        addMouseWheelListener(e -> {
            nearPlane += e.getWheelRotation() * 50;
            repaint();
        });
    }

    public void updateRotMatrix() {
        resetAngles();

        rotationMatrix = Matrix.multiply(
                Matrix.getRotationMatrix(fullX, 'y'),
                rotationMatrix);
        rotationMatrix = Matrix.multiply(
                Matrix.getRotationMatrix(fullY, 'z'),
                rotationMatrix);
    }

    public double getFullX() {
        return fullX;
    }

    public double getFullY() {
        return fullY;
    }

    public double getNearPlane() {
        return nearPlane;
    }

    public void setFullX(double fullX) {
        this.fullX = fullX;
    }

    public void setFullY(double fullY) {
        this.fullY = fullY;
    }

    public void setNearPlane(double nearPlane) {
        this.nearPlane = nearPlane;
    }

    public void resetAngles() {
        rotationMatrix = Matrix.getRotationMatrix(90, 'z');
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        List<double[][][]> normalizedPoints3D = normalize3D(model.getPoints3D(), calculateCycles(model.getBasePoints3D()));

        double[][][] points3D = applyTransformations(normalizedPoints3D.get(0));
        double[][][] calcCyclesTransformed = applyTransformations(normalizedPoints3D.get(1));
        double[][] XYZTransformed = applyTransformationsXYZ();

//        drawLines(g2d, points3D);
//        drawCycles(g2d, calcCyclesTransformed);

        drawFigure(g2d, points3D, calcCyclesTransformed);
        drawXYZ(XYZTransformed, g2d);
    }

    private List<double[][][]> normalize3D(double[][][] points3D, double[][][] cyclesPoints) {
        int pointsCount = points3D[0][0].length;
        int cyclesCount = cyclesPoints[0][0].length;
        double[][][] normalizedPoints = new double[points3D.length][3][pointsCount];
        double[][][] normalizedCycles = new double[cyclesPoints.length][3][cyclesCount];

        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;

        for (double[][] doubles : points3D) {
            for (int j = 0; j < pointsCount; j++) {
                double x = doubles[0][j];
                double y = doubles[1][j];
                double z = doubles[2][j];

                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
                minZ = Math.min(minZ, z);
                maxZ = Math.max(maxZ, z);
            }
        }

//        for (double[][] doubles : cyclesPoints) {
//            for (int j = 0; j < cyclesCount; j++) {
//                double x = doubles[0][j];
//                double y = doubles[1][j];
//                double z = doubles[2][j];
//
//                minX = Math.min(minX, x);
//                maxX = Math.max(maxX, x);
//                minY = Math.min(minY, y);
//                maxY = Math.max(maxY, y);
//                minZ = Math.min(minZ, z);
//                maxZ = Math.max(maxZ, z);
//            }
//        }

        double sizeX = maxX - minX;
        double sizeY = maxY - minY;
        double sizeZ = maxZ - minZ;
        double centerX = (maxX + minX) / 2;
        double centerY = (maxY + minY) / 2;
        double centerZ = (maxZ + minZ) / 2;

        double maxSize = Math.max(sizeX, Math.max(sizeY, sizeZ));
        double scale = (maxSize == 0) ? 1 : 2.0 / maxSize;

        for (int i = 0; i < points3D.length; i++) {
            for (int j = 0; j < pointsCount; j++) {
                double x = points3D[i][0][j];
                double y = points3D[i][1][j];
                double z = points3D[i][2][j];

                normalizedPoints[i][0][j] = (x - centerX) * scale;
                normalizedPoints[i][1][j] = (y - centerY) * scale;
                normalizedPoints[i][2][j] = (z - centerZ) * scale;
            }
        }

        for (int i = 0; i < cyclesPoints.length; i++) {
            for (int j = 0; j < cyclesCount; j++) {
                double x = cyclesPoints[i][0][j];
                double y = cyclesPoints[i][1][j];
                double z = cyclesPoints[i][2][j];

                normalizedCycles[i][0][j] = (x - centerX) * scale;
                normalizedCycles[i][1][j] = (y - centerY) * scale;
                normalizedCycles[i][2][j] = (z - centerZ) * scale;
            }
        }

        ArrayList<double[][][]> normalizedPointsList = new ArrayList<>();
        normalizedPointsList.add(normalizedPoints);
        normalizedPointsList.add(normalizedCycles);

        return normalizedPointsList;
    }

    private double[][][] calculateCycles(double[][][] points3D) {
        int M1 = model.getM1();
        int genPoints = points3D.length;
        int splinePointsCount = points3D[0][0].length;

        if (genPoints == 2) {
            M1 = 1;
        }

        double[][][] cycles = new double[genPoints * M1][3][splinePointsCount];

        for (int i = 0; i < splinePointsCount; i++) {
            double zAvg = 0;
            double radius = 0;
            double[] radii = new double[genPoints];

            for (int j = 0; j < genPoints; j++) {
                zAvg += points3D[j][2][i];
                radii[j] = Math.sqrt(
                        Math.pow(points3D[j][0][i], 2) +
                                Math.pow(points3D[j][1][i], 2));
            }

            for (double r : radii) {
                radius += r;
            }

            zAvg /= genPoints;
            radius /= genPoints;

            double[] angles = new double[genPoints];

            for (int j = 0; j < genPoints; j++) {
                angles[j] = Math.atan2(points3D[j][1][i], points3D[j][0][i]);
                if (angles[j] < 0) angles[j] += 2 * Math.PI;
            }

            for (int j = 0; j < genPoints; j++) {
                double startAngle = angles[j];
                double endAngle = angles[(j + 1) % genPoints];

                if (endAngle < startAngle) {
                    endAngle += 2 * Math.PI;
                }

                for (int k = 0; k < M1; k++) {
                    double step = (double) k / M1;
                    double angle = startAngle + step * (endAngle - startAngle);

                    double x = radius * Math.cos(angle);
                    double y = radius * Math.sin(angle);

                    cycles[j * M1 + k][0][i] = x;
                    cycles[j * M1 + k][1][i] = y;
                    cycles[j * M1 + k][2][i] = zAvg;
                }
            }
        }

        return cycles;
    }

    private double[][] applyTransformationsXYZ() {
        double[][] transformedXYZ = new double[4][3];

        double[][] matrix = rotationMatrix;
        matrix = Matrix.multiply(
                Matrix.getScalingMatrix(50.0, 50.0, 50.0),
                matrix);
        matrix = Matrix.multiply(
                Matrix.getTranslationMatrix(100, 100, 100),
                matrix);
        matrix = Matrix.multiply(
                Matrix.getLookAt(
                        new Vector(-10, 0, 0),
                        new Vector(10, 0, 0),
                        new Vector(0, 1, 0)),
                matrix);

        for (int i = 0; i < XYZ.length; i++) {
            double x = XYZ[i][0];
            double y = XYZ[i][1];
            double z = XYZ[i][2];

            Vector vector = Matrix.multiply(matrix, new Vector(x, y, z));

            transformedXYZ[i][0] = vector.getX();
            transformedXYZ[i][1] = vector.getY();
            transformedXYZ[i][2] = vector.getZ();
        }

        return transformedXYZ;
    }

    private double[][][] applyTransformations(double[][][] points3D) {
        int pointsCount = points3D[0][0].length;
        double[][][] transformedPoints = new double[points3D.length][3][pointsCount];

        double[][] matrix = rotationMatrix;
        matrix = Matrix.multiply(
                Matrix.getScalingMatrix(1.0, 1.0, 1.0),
                matrix);
        matrix = Matrix.multiply(
                Matrix.getTranslationMatrix(0, 0, 0),
                matrix);
        matrix = Matrix.multiply(
                Matrix.getLookAt(
                        new Vector(-10, 0, 0),
                        new Vector(10, 0, 0),
                        new Vector(0, 1, 0)),
                matrix);
        matrix = Matrix.multiply(
                Matrix.getAltPerspectiveProjectionMatrix(
                        3, 3,
                        nearPlane, 3000),
                matrix);


        for (int i = 0; i < points3D.length; i++) {
            for (int j = 0; j < pointsCount; j++) {
                double x = points3D[i][0][j];
                double y = points3D[i][1][j];
                double z = points3D[i][2][j];

                Vector vector = Matrix.multiply(matrix, new Vector(x, y, z));

                transformedPoints[i][0][j] = vector.getX() / vector.getW();
                transformedPoints[i][1][j] = vector.getY() / vector.getW();
                transformedPoints[i][2][j] = vector.getZ() / vector.getW();
            }
        }

        return transformedPoints;
    }

    private void drawLines(Graphics2D g2d, double[][][] points3D) {
        for (double[][] points : points3D) {
            GeneralPath path = new GeneralPath();

            double minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;

            for (int i = 0; i < points[2].length; i++) {
                minZ = Math.min(minZ, points[2][i]);
                maxZ = Math.max(maxZ, points[2][i]);
            }

            for (int i = 0; i < points[0].length - 1; i++) {
                double z1 = points[2][i];
                double z2 = points[2][i + 1];

                double normalizedZ1 = (z1 - minZ) / (maxZ - minZ);
                double normalizedZ2 = (z2 - minZ) / (maxZ - minZ);

                Color color1 = interpolateColor(normalizedZ1);
                Color color2 = interpolateColor(normalizedZ2);

                g2d.setPaint(new GradientPaint(
                        (float) centralizeX(points[0][i]), (float) centralizeY(points[1][i]), color1,
                        (float) centralizeX(points[0][i + 1]), (float) centralizeY(points[1][i + 1]), color2
                ));

                path.moveTo(centralizeX(points[0][i]), centralizeY(points[1][i]));
                path.lineTo(centralizeX(points[0][i + 1]), centralizeY(points[1][i + 1]));
                g2d.draw(path);
                path.reset();
            }
        }

        g2d.setPaint(Color.BLACK);
    }

    private Color interpolateColor(double t) {
        int r = (int) (Color.BLUE.getRed() + t * (Color.RED.getRed() - Color.BLUE.getRed()));
        int g = (int) (Color.BLUE.getGreen() + t * (Color.RED.getGreen() - Color.BLUE.getGreen()));
        int b = (int) (Color.BLUE.getBlue() + t * (Color.RED.getBlue() - Color.BLUE.getBlue()));
        return new Color(r, g, b);
    }

    private void drawCycles(Graphics2D g2d, double[][][] points3D) {
        int pointsCount = points3D[0][0].length;

        for (int i = 0; i < pointsCount; i++) {
            GeneralPath path = new GeneralPath();

            double minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;

            for (double[][] doubles : points3D) {
                double z = doubles[2][i];
                minZ = Math.min(minZ, z);
                maxZ = Math.max(maxZ, z);
            }

            for (int j = 0; j < points3D.length; j++) {
                double z1 = points3D[j][2][i];
                double z2 = points3D[(j + 1) % points3D.length][2][i];

                double normalizedZ1 = (z1 - minZ) / (maxZ - minZ);
                double normalizedZ2 = (z2 - minZ) / (maxZ - minZ);

                Color color1 = interpolateColor(normalizedZ1);
                Color color2 = interpolateColor(normalizedZ2);

                g2d.setPaint(new GradientPaint(
                        (float) centralizeX(points3D[j][0][i]), (float) centralizeY(points3D[j][1][i]), color1,
                        (float) centralizeX(points3D[(j + 1) % points3D.length][0][i]),
                        (float) centralizeY(points3D[(j + 1) % points3D.length][1][i]), color2
                ));

                if (j == 0) {
                    path.moveTo(centralizeX(points3D[j][0][i]), centralizeY(points3D[j][1][i]));
                }
                path.lineTo(centralizeX(points3D[(j + 1) % points3D.length][0][i]),
                        centralizeY(points3D[(j + 1) % points3D.length][1][i]));
            }

            path.closePath();
            g2d.draw(path);
        }

        g2d.setPaint(Color.BLACK);
    }

    private void drawFigure(Graphics2D g2d, double[][][] points3D, double[][][] cyclesPoints) {
        int cyclesCount = cyclesPoints[0][0].length;

        double minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;

        for (double[][] points : points3D) {
            for (int i = 0; i < points[2].length; i++) {
                minZ = Math.min(minZ, points[2][i]);
                maxZ = Math.max(maxZ, points[2][i]);
            }
        }

        for (double[][] points : cyclesPoints) {
            for (int i = 0; i < points[2].length; i++) {
                minZ = Math.min(minZ, points[2][i]);
                maxZ = Math.max(maxZ, points[2][i]);
            }
        }

        for (double[][] points : points3D) {
            GeneralPath path = new GeneralPath();

            for (int i = 0; i < points[0].length - 1; i++) {
                double z1 = points[2][i];
                double z2 = points[2][i + 1];

                double normalizedZ1 = (z1 - minZ) / (maxZ - minZ);
                double normalizedZ2 = (z2 - minZ) / (maxZ - minZ);

                Color color1 = interpolateColor(normalizedZ1);
                Color color2 = interpolateColor(normalizedZ2);

                g2d.setPaint(new GradientPaint(
                        (float) centralizeX(points[0][i]), (float) centralizeY(points[1][i]), color1,
                        (float) centralizeX(points[0][i + 1]), (float) centralizeY(points[1][i + 1]), color2
                ));

                path.moveTo(centralizeX(points[0][i]), centralizeY(points[1][i]));
                path.lineTo(centralizeX(points[0][i + 1]), centralizeY(points[1][i + 1]));
                g2d.draw(path);
                path.reset();
            }
        }

        for (int i = 0; i < cyclesCount; i++) {
            GeneralPath path = new GeneralPath();

            for (int j = 0; j < cyclesPoints.length; j++) {
                double z1 = cyclesPoints[j][2][i];
                double z2 = cyclesPoints[(j + 1) % cyclesPoints.length][2][i];

                double normalizedZ1 = (z1 - minZ) / (maxZ - minZ);
                double normalizedZ2 = (z2 - minZ) / (maxZ - minZ);

                Color color1 = interpolateColor(normalizedZ1);
                Color color2 = interpolateColor(normalizedZ2);

                g2d.setPaint(new GradientPaint(
                        (float) centralizeX(cyclesPoints[j][0][i]), (float) centralizeY(cyclesPoints[j][1][i]), color1,
                        (float) centralizeX(cyclesPoints[(j + 1) % cyclesPoints.length][0][i]),
                        (float) centralizeY(cyclesPoints[(j + 1) % cyclesPoints.length][1][i]), color2
                ));

                if (j == 0) {
                    path.moveTo(centralizeX(cyclesPoints[j][0][i]), centralizeY(cyclesPoints[j][1][i]));
                }
                path.lineTo(centralizeX(cyclesPoints[(j + 1) % cyclesPoints.length][0][i]),
                        centralizeY(cyclesPoints[(j + 1) % cyclesPoints.length][1][i]));
            }

            path.closePath();
            g2d.draw(path);
        }

        g2d.setPaint(Color.BLACK);
    }

    private void drawXYZ(double[][] points, Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.drawLine((int) points[3][0], (int) points[3][1],
                (int) points[0][0], (int) points[0][1]);

        g2d.setColor(Color.GREEN);
        g2d.drawLine((int) points[3][0], (int) points[3][1],
                (int) points[1][0], (int) points[1][1]);

        g2d.setColor(Color.BLUE);
        g2d.drawLine((int) points[3][0], (int) points[3][1],
                (int) points[2][0], (int) points[2][1]);
    }

    private double centralizeX(double x) {
        return x + (double) getWidth() / 2;
    }

    private double centralizeY(double y) {
        return -y + (double) getHeight() / 2;
    }
}

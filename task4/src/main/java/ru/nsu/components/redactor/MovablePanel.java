package ru.nsu.components.redactor;

import ru.nsu.components.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class MovablePanel extends JPanel {
    private Point2D selectedPoint = null;

    private final double pointRadius = 0.3;
    private final double tickStep = 1;

    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;

    private final Model model;

    private Point2D lastPosition = null;
    private boolean isDragging = false;

    public MovablePanel(Model model) {
        this.model = model;

        xMin = -model.getXBorder();
        xMax = model.getXBorder();
        yMin = -model.getYBorder();
        yMax = model.getYBorder();

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2 && e.isControlDown()) {
                    normalizeImage();
                }

                double mouseX = pixelToCoordinateX(e.getX());
                double mouseY = pixelToCoordinateY(e.getY());

                ArrayList<Point2D> points = model.getPoints();

                for (Point2D point : points) {
                    if (point.distance(mouseX, mouseY) <= pointRadius) {
                        selectedPoint = point;
                        break;
                    }
                }

                if (selectedPoint == null) {
                    lastPosition = new Point2D.Double(mouseX, mouseY);
                    isDragging = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedPoint != null) {
                    selectedPoint = null;
                    repaintPanel();
                }

                if (isDragging) {
                    isDragging = false;
                    lastPosition = null;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedPoint != null) {
                    int borderedX = Math.max(Math.min(e.getX(), getWidth()), 0);
                    int borderedY = Math.max(Math.min(e.getY(), getHeight()), 0);

                    double mouseX = pixelToCoordinateX(borderedX);
                    double mouseY = pixelToCoordinateY(borderedY);

                    selectedPoint.setLocation(mouseX, mouseY);
                    model.recalculateSplinePoints();
                    repaint();
                }

                if (isDragging) {
                    int borderedX = Math.max(Math.min(e.getX(), getWidth()), 0);
                    int borderedY = Math.max(Math.min(e.getY(), getHeight()), 0);

                    double mouseX = pixelToCoordinateX(borderedX);
                    double mouseY = pixelToCoordinateY(borderedY);

                    double deltaX = mouseX - lastPosition.getX();
                    double deltaY = mouseY - lastPosition.getY();

                    for (Point2D point : model.getPoints()) {
                        point.setLocation(point.getX() + deltaX, point.getY() + deltaY);
                    }

                    lastPosition.setLocation(mouseX, mouseY);
                    model.recalculateSplinePoints();
                    repaint();
                }
            }
        };

        MouseWheelListener mouseWheelListener = e -> {
            double zoomFactor = 1.1;
            double zoomAmount = e.getWheelRotation() > 0 ? 1 / zoomFactor : zoomFactor;

            if (e.isAltDown()) {
                xMin *= zoomAmount;
                xMax *= zoomAmount;
                yMin *= zoomAmount;
                yMax *= zoomAmount;
            } else {
                for (Point2D point : model.getPoints()) {
                    point.setLocation(point.getX() * zoomAmount, point.getY() * zoomAmount);
                }
            }

            model.recalculateSplinePoints();
            repaint();
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        addMouseWheelListener(mouseWheelListener);
    }

    private void normalizeImage() {
        xMin = -model.getXBorder();
        xMax = model.getXBorder();
        yMin = -model.getYBorder();
        yMax = model.getYBorder();

        double xMinHalf = xMin / 2;
        double xMaxHalf = xMax / 2;
        double yMinHalf = yMin / 2;
        double yMaxHalf = yMax / 2;

        double xMinVal = Double.MAX_VALUE, yMinVal = Double.MAX_VALUE;
        double xMaxVal = -1 * Double.MAX_VALUE, yMaxVal = -1 * Double.MAX_VALUE;

        for (Point2D point : model.getPoints()) {
            if (point.getX() < xMinVal) {
                xMinVal = point.getX();
            }
            if (point.getY() < yMinVal) {
                yMinVal = point.getY();
            }
            if (point.getX() > xMaxVal) {
                xMaxVal = point.getX();
            }
            if (point.getY() > yMaxVal) {
                yMaxVal = point.getY();
            }
        }

        double sizeX = xMaxVal - xMinVal;
        double sizeY = yMaxVal - yMinVal;

        double centerX = (xMaxVal + xMinVal) / 2;
        double centerY = (yMaxVal + yMinVal) / 2;

        double maxSize = Math.max(sizeX, sizeY);
        double scale = (maxSize == 0) ? 1 :
                Math.min((xMaxHalf - xMinHalf) / maxSize, (yMaxHalf - yMinHalf) / maxSize);

        ArrayList<Point2D> points = model.getPoints();

        for (Point2D point : points) {
            double newX = (point.getX() - centerX) * scale;
            double newY = (point.getY() - centerY) * scale;

            point.setLocation(newX, newY);
        }

        model.recalculateSplinePoints();
        repaint();
    }

    public void repaintPanel() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x0 = coordinateToPixelX(0);
        int y0 = coordinateToPixelY(0);

        g2d.setColor(Color.BLACK);
        g2d.drawLine(coordinateToPixelX(xMin), y0, coordinateToPixelX(xMax), y0);
        g2d.drawLine(x0, coordinateToPixelY(yMin), x0, coordinateToPixelY(yMax));

        for (double x = Math.ceil(xMin / tickStep) * tickStep; x <= xMax; x += tickStep) {
            int xPixel = coordinateToPixelX(x);
            g2d.drawLine(xPixel, y0 - 5, xPixel, y0 + 5);
        }

        for (double y = Math.ceil(yMin / tickStep) * tickStep; y <= yMax; y += tickStep) {
            int yPixel = coordinateToPixelY(y);
            g2d.drawLine(x0 - 5, yPixel, x0 + 5, yPixel);
        }

        ArrayList<Point2D> points = model.getPoints();

        g2d.setColor(Color.MAGENTA);

        int lastX = -1;
        int lastY = -1;

        for (Point2D point : points) {
            int xPixel = coordinateToPixelX(point.getX());
            int yPixel = coordinateToPixelY(point.getY());
            int pixelRadius = (int) (pointRadius * getWidth() / (xMax - xMin));

            if (point == selectedPoint) {
                g2d.setColor(Color.GREEN);
            }

            g2d.drawOval(xPixel - pixelRadius,
                    yPixel - pixelRadius,
                    pixelRadius * 2,
                    pixelRadius * 2);

            if (point == selectedPoint) {
                g2d.setColor(Color.MAGENTA);
            }

            if (lastX != -1 && lastY != -1) {
                g2d.drawLine(lastX, lastY, xPixel, yPixel);
            }

            lastX = xPixel;
            lastY = yPixel;
        }

        g2d.setColor(Color.BLUE);
        ArrayList<Point2D> splinePoints = model.getSplinePoints();
        GeneralPath splinePath = convertPath(splinePoints);
        g2d.draw(splinePath);
    }

    private GeneralPath convertPath(ArrayList<Point2D> points) {
        GeneralPath newPath = new GeneralPath();

        for (Point2D point : points) {
            double x = coordinateToPixelX(point.getX());
            double y = coordinateToPixelY(point.getY());

            if (newPath.getCurrentPoint() == null) {
                newPath.moveTo(x, y);
            } else {
                newPath.lineTo(x, y);
            }
        }

        return newPath;
    }

    private double pixelToCoordinateX(int pixelX) {
        double width = getWidth();
        return xMin + (pixelX / width) * (xMax - xMin);
    }

    private double pixelToCoordinateY(int pixelY) {
        double height = getHeight();
        return yMax - (pixelY / height) * (yMax - yMin);
    }

    private int coordinateToPixelX(double coordinateX) {
        double width = getWidth();
        return (int) (((coordinateX - xMin) / (xMax - xMin)) * width);
    }

    private int coordinateToPixelY(double coordinateY) {
        double height = getHeight();
        return (int) (((yMax - coordinateY) / (yMax - yMin)) * height);
    }
}

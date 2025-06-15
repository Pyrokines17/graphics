package ru.nsu.gunko;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Stack;

public class DrawingPanel extends JPanel {
    private BufferedImage image;

    private boolean polygon;
    private int radius;
    private int sides;
    private double rotation;

    private Color color;
    private int thick;
    private int lastW = 0, lastH = 0;

    private JButton chColor;

    public DrawingPanel() {
        setBackground(Color.WHITE);

        this.polygon = false;
        this.color = Color.BLACK;
        this.thick = 1;

        updateImage();
    }

    public void setChColor(JButton chColor) {
        this.chColor = chColor;
    }

    public JButton getChColor() {
        return chColor;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage img) {
        this.image = img;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        updateImage();
    }

    private void updateImage() {
        int w = getWidth();
        int h = getHeight();

        if (w > 0 && h > 0 && (w-lastW) > 0 && (h-lastH) > 0) {
            BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = newImage.createGraphics();

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, w, h);

            if (image != null) {
                g2d.drawImage(image, 0, 0, null);
            }

            g2d.dispose();

            image = newImage;
            lastW = w; lastH = h;
        }
    }

    public void clearArea() {
        if (image != null) {
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2d.dispose();

            repaint();
        }
    }

    private void setPolygonOrStar(boolean polygon) {
        this.polygon = polygon;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setSides(int sides) {
        this.sides = sides;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public void setColor(int r, int g, int b) {
        this.color = new Color(r, g, b);
    }

    public void setThick(int thick) {
        this.thick = thick;
    }

    public void bresenhamLine(int x0, int y0, int x1, int y1) {
        if (Math.abs(x1-x0) > Math.abs(y1-y0)) {
            horizontalLine(x0, y0, x1, y1);
        } else {
            verticalLine(x0, y0, x1, y1);
        }
    }

    private void horizontalLine(int x0, int y0, int x1, int y1) {
        int[] beginPoint, endPoint;
        int usingThick;
        Color usingColor;

        if (polygon) {
            usingThick = 1;
            usingColor = Color.BLACK;
        } else {
            usingThick = thick;
            usingColor = color;
        }
        
        if (x0 > x1) {
            beginPoint = new int[]{x1, y1};
            endPoint = new int[]{x0, y0};
        } else {
            beginPoint = new int[]{x0, y0};
            endPoint = new int[]{x1, y1};
        }

        int dx = endPoint[0] - beginPoint[0];
        int dy = endPoint[1] - beginPoint[1];

        int dir = (dy < 0) ? -1 : 1;
        dy *= dir;

        if (dx != 0) {
            int y = beginPoint[1];
            int p = 2*dy - dx;

            for (int i = 0; i < dx + 1; ++i) {
                for (int j = 0; j < usingThick; ++j) {
                    try {
                        image.setRGB(beginPoint[0] + i, y + j, usingColor.getRGB());
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                    }
                }

                if (p >= 0) {
                    y += dir;
                    p -= 2*dx;
                }

                p += 2*dy;
            }

            repaint();
        }
    }

    private void verticalLine(int x0, int y0, int x1, int y1) {
        int[] beginPoint, endPoint;
        int usingThick;
        Color usingColor;

        if (polygon) {
            usingThick = 1;
            usingColor = Color.BLACK;
        } else {
            usingThick = thick;
            usingColor = color;
        }

        if (y0 > y1) {
            beginPoint = new int[]{x1, y1};
            endPoint = new int[]{x0, y0};
        } else {
            beginPoint = new int[]{x0, y0};
            endPoint = new int[]{x1, y1};
        }

        int dx = endPoint[0] - beginPoint[0];
        int dy = endPoint[1] - beginPoint[1];

        int dir = (dx < 0) ? -1 : 1;
        dx *= dir;

        if (dy != 0) {
            int x = beginPoint[0];
            int p = 2*dx - dy;

            for (int i = 0; i < dy + 1; ++i) {
                for (int j = 0; j < usingThick; ++j) {
                    try {
                        image.setRGB(x + j, beginPoint[1] + i, usingColor.getRGB());
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                    }
                }

                if (p >= 0) {
                    x += dir;
                    p -= 2*dy;
                }

                p += 2*dx;
            }

            repaint();
        }
    }

    public void fillArea(int x, int y) {
        Color oldColor;

        try {
            oldColor = new Color(image.getRGB(x, y));
        } catch (ArrayIndexOutOfBoundsException exception) {
            return;
        }

        if (oldColor.equals(color)) {
            return;
        }

        int w = image.getWidth(), h = image.getHeight();
        boolean spanAbove, spanBelow;
        int x1, y1;

        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{x, y});

        int intOldColor = oldColor.getRGB();
        int intNewColor = color.getRGB();

        while (!stack.isEmpty()) {
            int[] point = stack.pop();

            x1 = point[0];
            y1 = point[1];

            while (x1 >= 0 && image.getRGB(x1, y1) == intOldColor) {
                x1--;
            }

            x1++;
            spanAbove = spanBelow = false;

            while (x1 < w && image.getRGB(x1, y1) == intOldColor) {
                image.setRGB(x1, y1, intNewColor);

                if (!spanAbove && (y1 > 0) && image.getRGB(x1, y1-1) == intOldColor) {
                    stack.push(new int[]{x1, y1-1});
                    spanAbove = true;
                } else if (spanAbove && (y1 > 0) && image.getRGB(x1, y1-1) != intOldColor) {
                    spanAbove = false;
                }

                if (!spanBelow && (y1 < h-1) && image.getRGB(x1, y1+1) == intOldColor) {
                    stack.push(new int[]{x1, y1+1});
                    spanBelow = true;
                } else if (spanBelow && (y1 < h-1) && image.getRGB(x1, y1+1) != intOldColor) {
                    spanBelow = false;
                }

                x1++;
            }
        }

        repaint();
    }

    public void drawPolygon(int x, int y) {
        int xPoint, yPoint;
        double angle;

        setPolygonOrStar(true);
        double radRot = Math.toRadians(rotation);
        double step = 2 * Math.PI / sides;

        int firstXPoint = x + (int) (radius * Math.cos(radRot));
        int firstYPoint = y + (int) (radius * Math.sin(radRot));

        int lastXPoint = firstXPoint;
        int lastYPoint = firstYPoint;

        for (int i = 1; i < sides; ++i) {
            angle = step * i + radRot;

            xPoint = x + (int) (radius * Math.cos(angle));
            yPoint = y + (int) (radius * Math.sin(angle));

            bresenhamLine(lastXPoint, lastYPoint, xPoint, yPoint);

            lastXPoint = xPoint;
            lastYPoint = yPoint;
        }

        bresenhamLine(lastXPoint, lastYPoint, firstXPoint, firstYPoint);

        setPolygonOrStar(false);
    }

    public void drawStar(int x, int y) {
        int xPoint, yPoint;
        double angle;

        int rad = radius / 2;
        int tempRad;

        setPolygonOrStar(true);
        double radRot = Math.toRadians(rotation);

        int firstXPoint = x + (int) (radius * Math.cos(radRot));
        int firstYPoint = y + (int) (radius * Math.sin(radRot));

        int lastXPoint = firstXPoint;
        int lastYPoint = firstYPoint;

        for (int i = 1; i < 2*sides; ++i) {
            tempRad = (i % 2 == 0) ? radius : rad;
            angle = Math.PI * i / sides + radRot;

            xPoint = x + (int) (tempRad * Math.cos(angle));
            yPoint = y + (int) (tempRad * Math.sin(angle));

            bresenhamLine(lastXPoint, lastYPoint, xPoint, yPoint);

            lastXPoint = xPoint;
            lastYPoint = yPoint;
        }

        bresenhamLine(lastXPoint, lastYPoint, firstXPoint, firstYPoint);

        setPolygonOrStar(false);
    }
}

package ru.nsu.gunko;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class Adapters {
    private final Vector<MouseAdapter> adapters;

    private int lastX, lastY;
    private boolean pressed;

    public Adapters(DrawingPanel panel) {
        adapters = new Vector<>();
        pressed = false;

        adapters.add(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (!pressed) {
                    lastX = e.getX();
                    lastY = e.getY();
                    pressed = true;
                } else {
                    panel.bresenhamLine(lastX, lastY, e.getX(), e.getY());
                    pressed = false;
                }
            }
        });

        adapters.add(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                panel.fillArea(e.getX(), e.getY());
            }
        });

        adapters.add(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                panel.drawPolygon(e.getX(), e.getY());
            }
        });

        adapters.add(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                panel.drawStar(e.getX(), e.getY());
            }
        });
    }

    public MouseAdapter getLineAdapter() {
        return adapters.getFirst();
    }

    public MouseAdapter getFillAdapter() {
        return adapters.get(1);
    }

    public MouseAdapter getPolygonAdapter() {
        return adapters.get(2);
    }

    public MouseAdapter getStarAdapter() {
        return adapters.get(3);
    }
}

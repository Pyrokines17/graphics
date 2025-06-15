package ru.nsu.gunko;

import javax.swing.*;
import java.awt.*;

import java.awt.event.*;

class MyProg extends JFrame {
    public MyProg() {
        super("my_prog");

        setMinimumSize(new Dimension(640, 480));
        setPreferredSize(new Dimension(800, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        MyPanel panel = new MyPanel();
        add(panel);

        Button but = new Button("click");
        but.addActionListener(e -> panel.myButtonClick());
        add(but, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }
}

class MyPanel extends JPanel implements MouseListener {
    private int x, y;
    private boolean drawing, clean;

    public MyPanel() {
        addMouseListener(this);
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (drawing) {
                    Graphics graphics = getGraphics();
                    graphics.setColor(Color.BLACK);
                    graphics.drawLine(x, y, e.getX(), e.getY());
                    x = e.getX(); y = e.getY();
                }
            }
        });
    }

    public void myButtonClick() {
        clean = true;
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        x = e.getX(); y = e.getY();
        drawing = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        drawing = false;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!clean) {
            g.setColor(Color.PINK);
            g.drawLine(0, 0, getWidth(), getHeight());
            g.drawLine(getWidth(), 0, 0, getHeight());
        } else {
            g.setColor(Color.WHITE);
            g.drawRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }
}

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MyProg::new);
    }
}
package ru.nsu.gunko;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class PaintProgram extends JFrame {
    public PaintProgram() {
        super("Paint Program");

        setMinimumSize(new Dimension(640, 480));
        setPreferredSize(new Dimension(1020, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        DrawingPanel drawingPanel = new DrawingPanel();
        Adapters adapters = new Adapters(drawingPanel);

        JToolBar tools = WindowBuilder.getToolBar(drawingPanel, adapters, this);
        JMenuBar menuBar = WindowBuilder.getMenuBar(this, drawingPanel, adapters);

        setJMenuBar(menuBar);

        JScrollPane scrollPane = new JScrollPane(drawingPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
        add(tools, BorderLayout.SOUTH);

        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = scrollPane.getViewport().getSize();
                Dimension oldSize = drawingPanel.getPreferredSize();
                if (size.height > oldSize.height && size.width > oldSize.width) {
                    drawingPanel.setPreferredSize(size);
                    drawingPanel.revalidate();
                }
            }
        });

        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PaintProgram::new);
    }
}
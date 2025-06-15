package ru.nsu.components.redactor;

import ru.nsu.components.Model;

import javax.swing.*;
import java.awt.*;

public class View extends JFrame {
    private JSpinner NSpinner;
    private JSpinner KSpinner;
    private JSpinner MSpinner;
    private JSpinner M1Spinner;

    private final Model model;
    private final MovablePanel movablePanel;

    public View(Model model) {
        super("Redactor");
        this.model = model;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(640, 480));
        setSize(800, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(getParametersPanel(), BorderLayout.SOUTH);
        movablePanel = new MovablePanel(model);
        add(movablePanel, BorderLayout.CENTER);
    }

    private JPanel getParametersPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 4));

        addNParameter(panel);
        addKParameter(panel);
        addMParameter(panel);
        addM1Parameter(panel);

        return panel;
    }

    private void addNParameter(JPanel panel) {
        panel.add(getBorderedLabel("N (число отрезков сплайна)"));
        NSpinner = new JSpinner(new SpinnerNumberModel(model.getN(), 1, 100, 1));
        NSpinner.addChangeListener(e -> {
            model.setN((int) NSpinner.getValue());
            movablePanel.repaintPanel();});
        panel.add(NSpinner);
    }

    private void addKParameter(JPanel panel) {
        panel.add(getBorderedLabel("K (число опорных точек)"));
        KSpinner = new JSpinner(new SpinnerNumberModel(model.getK(), 4, 20, 1));
        KSpinner.addChangeListener(e -> {
            model.setK((int) KSpinner.getValue());
            model.changePoints();
            movablePanel.repaintPanel();});
        panel.add(KSpinner);
    }

    private void addMParameter(JPanel panel) {
        panel.add(getBorderedLabel("M (число образующих)"));
        MSpinner = new JSpinner(new SpinnerNumberModel(model.getM(), 2, 20, 1));
        MSpinner.addChangeListener(e -> model.setM((int) MSpinner.getValue()));
        panel.add(MSpinner);
    }

    private void addM1Parameter(JPanel panel) {
        panel.add(getBorderedLabel("M1 (число отрезков окружности)"));
        M1Spinner = new JSpinner(new SpinnerNumberModel(model.getM1(), 1, 20, 1));
        M1Spinner.addChangeListener(e -> model.setM1((int) M1Spinner.getValue()));
        panel.add(M1Spinner);
    }

    private JPanel getBorderedLabel(String text) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(new JLabel(text));
        return panel;
    }
}

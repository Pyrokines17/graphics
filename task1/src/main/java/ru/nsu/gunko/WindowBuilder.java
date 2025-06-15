package ru.nsu.gunko;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;
import java.util.stream.Collectors;

public class WindowBuilder {
    private static final Integer SIZE = 32;
    private static JToggleButton lastSelected = null;
    private static boolean ignoreChanges = false;

    private static final HashMap<JToggleButton, MouseAdapter> adapterHashMap = new HashMap<>();
    private static final HashMap<Integer, JRadioButtonMenuItem> radioHash = new HashMap<>();
    private static final HashMap<JRadioButtonMenuItem, Integer> revRadioHash = new HashMap<>();
    private static final HashMap<Integer, JToggleButton> toggleHash = new HashMap<>();
    private static final HashMap<JToggleButton, Integer> revToggleHash = new HashMap<>();
    private static final HashMap<Integer, ButtonGroup> groupHash = new HashMap<>();

    private static String lastThick = "1";
    private static String lastRadius = "5";
    private static String lastRotation = "0";
    private static String lastSides = "3";
    private static boolean lastCh = false;

    public static JMenuBar getMenuBar(Frame frame, DrawingPanel panel, Adapters adapters) {
        JMenuBar menuBar = new JMenuBar();
        ButtonGroup group = new ButtonGroup();
        groupHash.put(0, group);

        JMenu fileMenu = new JMenu("File");

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        toolsMenu.setToolTipText("Tools submenu");

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        exitMenuItem.setToolTipText("Exit application");
        exitMenuItem.addActionListener((event) -> System.exit(0));

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.setToolTipText("Save draw pane");
        saveMenuItem.addActionListener((event) -> saveDialog(frame, panel.getImage()));

        JMenuItem loadMenuItem = new JMenuItem("Load");
        loadMenuItem.setMnemonic(KeyEvent.VK_L);
        loadMenuItem.setToolTipText("Load image");
        loadMenuItem.addActionListener((event) -> loadDialog(frame, panel));

        JMenuItem clearMenuItem = new JMenuItem("Clear");
        clearMenuItem.setMnemonic(KeyEvent.VK_C);
        clearMenuItem.setToolTipText("Clear area");
        clearMenuItem.addActionListener((event) -> panel.clearArea());

        JRadioButtonMenuItem lineMenuItem = new JRadioButtonMenuItem("Line");
        lineMenuItem.setMnemonic(KeyEvent.VK_L);
        lineMenuItem.setToolTipText("Draw line");
        lineMenuItem.addActionListener(e -> paramsLine(e, panel, groupHash.get(1), frame));
        group.add(lineMenuItem);
        radioHash.put(0, lineMenuItem);
        revRadioHash.put(lineMenuItem, 0);

        JRadioButtonMenuItem fillMenuItem = new JRadioButtonMenuItem("Fill");
        fillMenuItem.setMnemonic(KeyEvent.VK_F);
        fillMenuItem.setToolTipText("Fill place");
        fillMenuItem.addActionListener(e -> changing(e, panel, groupHash.get(1)));
        group.add(fillMenuItem);
        radioHash.put(1, fillMenuItem);
        revRadioHash.put(fillMenuItem, 1);

        fileMenu.add(exitMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(saveMenuItem);
        fileMenu.add(loadMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(toolsMenu);
        toolsMenu.add(clearMenuItem);
        toolsMenu.add(lineMenuItem);
        toolsMenu.add(fillMenuItem);

        addPolygonsAndStars(toolsMenu, group, panel, frame, adapters);
        addColors(toolsMenu, panel);

        fileMenu.setMnemonic(KeyEvent.VK_F);

        menuBar.add(fileMenu);

        JMenu helpMenu = new JMenu("Help");

        JMenuItem aboutMenuItem = new JMenuItem("About program");
        aboutMenuItem.setMnemonic(KeyEvent.VK_A);
        aboutMenuItem.setToolTipText("Info about program");
        aboutMenuItem.addActionListener((event) -> showDesc(frame));

        helpMenu.add(aboutMenuItem);
        helpMenu.setMnemonic(KeyEvent.VK_H);

        menuBar.add(helpMenu);

        return menuBar;
    }

    private static void addPolygonsAndStars(JMenu menu, ButtonGroup group, DrawingPanel panel, Frame frame, Adapters adapters) {
        JMenu shapes = new JMenu("Shapes");
        shapes.setMnemonic(KeyEvent.VK_S);
        shapes.setToolTipText("Choose figure");

        JMenu polygons = new JMenu("Polygons");
        polygons.setMnemonic(KeyEvent.VK_P);
        polygons.setToolTipText("Choose polygon");

        JMenu stars = new JMenu("Stars");
        stars.setMnemonic(KeyEvent.VK_S);
        stars.setToolTipText("Choose star");

        for (int i = 0; i < 5; ++i) {
            int count = i + 4;
            String polName = String.format("%d-side polygon", i+4);
            JRadioButtonMenuItem pol = new JRadioButtonMenuItem(polName);
            pol.setToolTipText("Draw "+polName);
            pol.addActionListener(e -> paramsPolygonOrStar(e, panel, groupHash.get(1), count, frame, 0));
            polygons.add(pol);
            group.add(pol);
            radioHash.put(3+i, pol);
            revRadioHash.put(pol, 3+i);
        }

        for (int i = 0; i < 5; ++i) {
            int count = i + 4;
            String starName = String.format("%d-side star", i+4);
            JRadioButtonMenuItem star = new JRadioButtonMenuItem(starName);
            star.setToolTipText("Draw "+starName);
            star.addActionListener(e -> paramsPolygonOrStar(e, panel, groupHash.get(1), count, frame, 1));
            stars.add(star);
            group.add(star);
            radioHash.put(8+i, star);
            revRadioHash.put(star, 8+i);
        }

        shapes.add(polygons);
        shapes.add(stars);

        JRadioButtonMenuItem anyShape = new JRadioButtonMenuItem("Any shape");
        anyShape.setMnemonic(KeyEvent.VK_A);
        anyShape.setToolTipText("Choose any shape");
        anyShape.addActionListener(e -> paramsUni(e, panel, groupHash.get(1), frame, toggleHash.get(2), adapters));
        shapes.add(anyShape);
        group.add(anyShape);
        radioHash.put(2, anyShape);
        revRadioHash.put(anyShape, 2);

        menu.add(shapes);
    }

    private static void addColors(JMenu menu, DrawingPanel panel) {
        JMenu colors = new JMenu("Colors");
        colors.setMnemonic(KeyEvent.VK_O);
        colors.setToolTipText("Choose color");

        colors.add(setOneColor("Black", KeyEvent.VK_B, 0, 0, 0, panel));
        colors.add(setOneColor("Red", KeyEvent.VK_R, 255, 0, 0, panel));
        colors.add(setOneColor("Yellow", KeyEvent.VK_Y, 255, 255, 0, panel));
        colors.add(setOneColor("Green", KeyEvent.VK_G, 0, 255, 0, panel));
        colors.add(setOneColor("Cyan", KeyEvent.VK_C, 0, 255, 255, panel));
        colors.add(setOneColor("Blue", KeyEvent.VK_L, 0, 0, 255, panel));
        colors.add(setOneColor("Magenta", KeyEvent.VK_M, 255, 0, 255, panel));
        colors.add(setOneColor("White", KeyEvent.VK_W, 255, 255, 255, panel));

        menu.add(colors);
    }

    private static JMenuItem setOneColor(String name, int key, int r, int g, int b, DrawingPanel panel) {
        JButton colorCh = panel.getChColor();
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.addActionListener((event) -> {
            panel.setColor(r, g, b);
            colorCh.setBackground(new Color(r, g, b));});
        menuItem.setToolTipText(name+" color");
        menuItem.setMnemonic(key);
        return menuItem;
    }

    private static void showDesc(Frame frame) {
        try {
            URL urlPath = WindowBuilder.class.getResource("/desc.md");

            if (urlPath == null) {
                throw new IOException();
            }

            InputStream inputStream = urlPath.openStream();
            String content;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                content = reader.lines().collect(Collectors.joining("\n"));
            }

            Parser parser = Parser.builder().build();
            Node node = parser.parse(content);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            String res = renderer.render(node);

            JEditorPane editorPane = new JEditorPane();
            editorPane.setContentType("text/html");
            editorPane.setText(res);
            editorPane.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(editorPane);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(
                    frame,
                    scrollPane,
                    "About",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Can not load desc: "+e.getLocalizedMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static void loadDialog(Frame frame, DrawingPanel panel) {
        FileDialog fd = new FileDialog(frame, "Load image");
        fd.setFile("*.png;*.jpg;*.jpeg;*.bmp;*.gif");
        fd.setVisible(true);

        String dir = fd.getDirectory();
        String fn = fd.getFile();

        if (dir != null && fn != null) {
            File file = new File(dir, fn);

            try {
                panel.setImage(ImageIO.read(file));
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
        }
    }

    private static void saveDialog(Frame frame, BufferedImage image) {
        FileDialog fd = new FileDialog(frame, "Save image", FileDialog.SAVE);
        fd.setFile("*.png");
        fd.setVisible(true);

        String dir = fd.getDirectory();
        String fn = fd.getFile();

        if (dir != null && fn != null) {
            File file;

            if (!fn.toLowerCase().endsWith(".png")) {
                file = new File(dir, fn+".png");
            } else {
                file = new File(dir, fn);
            }

            try {
                ImageIO.write(image, "png", file);
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
        }
    }

    public static JToolBar getToolBar(DrawingPanel panel, Adapters adapters, Frame frame) {
        JToolBar toolBar = new JToolBar();
        ButtonGroup toolsGroup = new ButtonGroup();
        Vector<JToggleButton> buttonVector = new Vector<>();
        groupHash.put(1, toolsGroup);

        JToggleButton clearButton = getToolBtn(getScaledImage("/icons8-eraser-64.png"), "Clear");
        clearButton.addActionListener((event) -> panel.clearArea());
        clearButton.setToolTipText("Clear panel");
        toolBar.add(clearButton);

        toolBar.addSeparator();

        JToggleButton lineButton = getToolBtn(getScaledImage("/icons8-line-64.png"), "Line");
        lineButton.addActionListener(e -> paramsLine(e, panel, toolsGroup, frame));
        adapterHashMap.put(lineButton, adapters.getLineAdapter());
        lineButton.setToolTipText("Draw line");
        toolsGroup.add(lineButton);
        toolBar.add(lineButton);
        toggleHash.put(0, lineButton);
        revToggleHash.put(lineButton, 0);

        JToggleButton fillButton = getToolBtn(getScaledImage("/icons8-fill-color-64.png"), "Fill");
        fillButton.addActionListener(e -> changing(e, panel, toolsGroup));
        adapterHashMap.put(fillButton, adapters.getFillAdapter());
        fillButton.setToolTipText("Fill area");
        toolsGroup.add(fillButton);
        toolBar.add(fillButton);
        toggleHash.put(1, fillButton);
        revToggleHash.put(fillButton, 1);

        toolBar.addSeparator();

        JToggleButton uniButton = getToolBtn(getScaledImage("/uni.png"), "Uni");
        uniButton.addActionListener(e -> paramsUni(e, panel, toolsGroup, frame, uniButton, adapters));
        uniButton.setToolTipText("Any figure");
        toolsGroup.add(uniButton);
        toolBar.add(uniButton);
        toggleHash.put(2, uniButton);
        revToggleHash.put(uniButton, 2);

        toolBar.addSeparator();

        setPolygons(adapters, panel, toolsGroup, buttonVector, frame);
        setStars(adapters, panel, toolsGroup, buttonVector, frame);

        for (JToggleButton button : buttonVector) {
            toolsGroup.add(button);
            toolBar.add(button);
        }

        toolBar.addSeparator();

        JButton colorCh = new JButton(" ");
        colorCh.setBackground(Color.BLACK);
        colorCh.setToolTipText("Any color");
        toolBar.add(colorCh);

        panel.setChColor(colorCh);

        colorCh.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(frame, "Choose a color", Color.BLACK);
            if (newColor != null) {
                panel.setColor(newColor.getRed(), newColor.getGreen(), newColor.getBlue());
                colorCh.setBackground(newColor);
            }
        });

        toolBar.addSeparator();

        setColors(panel, toolBar, colorCh);

        return toolBar;
    }

    private static void setPolygons(Adapters adapters, DrawingPanel panel, ButtonGroup toolsGroup, Vector<JToggleButton> buttonVector, Frame frame) {
        for (int i = 0; i < 5; ++i) {
            int count = i + 4;
            String name = String.format("%dp", count);
            String path = String.format("/polygons/%s.png", name);
            JToggleButton btn = getToolBtn(getScaledImage(path), name);
            adapterHashMap.put(btn, adapters.getPolygonAdapter());
            btn.setToolTipText(String.format("Draw %d-side polygon", count));
            btn.addActionListener(e -> paramsPolygonOrStar(e, panel, toolsGroup, count, frame, 0));
            buttonVector.add(btn);
            toggleHash.put(3+i, btn);
            revToggleHash.put(btn, 3+i);
        }
    }

    private static void setStars(Adapters adapters, DrawingPanel panel, ButtonGroup toolsGroup, Vector<JToggleButton> buttonVector, Frame frame) {
        for (int i = 0; i < 5; ++i) {
            int count = i + 4;
            String name = String.format("%ds", count);
            String path = String.format("/stars/%s.png", name);
            JToggleButton btn = getToolBtn(getScaledImage(path), name);
            adapterHashMap.put(btn, adapters.getStarAdapter());
            btn.setToolTipText(String.format("Draw %d-side star", count));
            btn.addActionListener(e -> paramsPolygonOrStar(e, panel, toolsGroup, count, frame, 1));
            buttonVector.add(btn);
            toggleHash.put(8+i, btn);
            revToggleHash.put(btn, 8+i);
        }
    }

    private static void paramsUni(ActionEvent e, DrawingPanel panel, ButtonGroup toolsGroup, Frame frame, JToggleButton btn, Adapters adapters) {
        JToggleButton selected;

        try {
            selected = (JToggleButton) e.getSource();
        } catch (ClassCastException exception) {
            selected = toggleHash.get(revRadioHash.get((JRadioButtonMenuItem) e.getSource()));
        }

        int radMin = 5, radMax = 300;
        int rotMin = 0, rotMax = 360;
        int sidMin = 3, sidMax = 16;

        if (lastSelected == selected) {
            toolsGroup.clearSelection();
            panel.removeMouseListener(adapterHashMap.get(selected));
            lastSelected = null;
            groupHash.get(0).clearSelection();
            return;
        }

        JPanel x0 = new JPanel();
        JCheckBox isStar = new JCheckBox();
        isStar.setSelected(lastCh);
        x0.setLayout(new BoxLayout(x0, BoxLayout.X_AXIS));
        x0.add(new JLabel("Draw star")); x0.add(isStar);

        JPanel x1 = new JPanel();
        JTextField radius = new JTextField(lastRadius, 5);
        JSlider sRadius = new JSlider(JSlider.HORIZONTAL, radMin, radMax, Integer.parseInt(lastRadius));
        x1.setLayout(new BoxLayout(x1, BoxLayout.X_AXIS));
        x1.add(new JLabel("Entry radius: ")); x1.add(radius); x1.add(sRadius);

        sRadius.addChangeListener(e1 -> {
            if (ignoreChanges) {
                return;
            }

            ignoreChanges = true;
            radius.setText(String.valueOf(sRadius.getValue()));
            ignoreChanges = false;
        });

        radius.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { processTextFieldChange(radius, sRadius); }
            public void removeUpdate(DocumentEvent e) { processTextFieldChange(radius, sRadius); }
            public void changedUpdate(DocumentEvent e) { processTextFieldChange(radius, sRadius); }
        });

        JPanel x2 = new JPanel();
        JTextField rotation = new JTextField(lastRotation, 5);
        JSlider sRotation = new JSlider(JSlider.HORIZONTAL, rotMin, 10*rotMax, (int)(10*Double.parseDouble(lastRotation)));
        x2.setLayout(new BoxLayout(x2, BoxLayout.X_AXIS));
        x2.add(new JLabel("Entry rotation: ")); x2.add(rotation); x2.add(sRotation);

        sRotation.addChangeListener(e2 -> {
            if (ignoreChanges) {
                return;
            }

            ignoreChanges = true;
            rotation.setText(String.format("%.1f", (float)sRotation.getValue()/10));
            ignoreChanges = false;
        });

        rotation.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { floProcessTextFieldChange(rotation, sRotation); }
            public void removeUpdate(DocumentEvent e) { floProcessTextFieldChange(rotation, sRotation); }
            public void changedUpdate(DocumentEvent e) { floProcessTextFieldChange(rotation, sRotation); }
        });

        JPanel x3 = new JPanel();
        JTextField sides = new JTextField(lastSides, 5);
        JSlider sSides = new JSlider(JSlider.HORIZONTAL, sidMin, sidMax, Integer.parseInt(lastSides));
        x3.setLayout(new BoxLayout(x3, BoxLayout.X_AXIS));
        x3.add(new JLabel("Entry sides: ")); x3.add(sides); x3.add(sSides);

        sSides.addChangeListener(e3 -> {
            if (ignoreChanges) {
                return;
            }

            ignoreChanges = true;
            sides.setText(String.valueOf(sSides.getValue()));
            ignoreChanges = false;
        });

        sides.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { processTextFieldChange(sides, sSides); }
            public void removeUpdate(DocumentEvent e) { processTextFieldChange(sides, sSides); }
            public void changedUpdate(DocumentEvent e) { processTextFieldChange(sides, sSides); }
        });

        JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.Y_AXIS));

        paramPanel.add(x1);
        paramPanel.add(x2);
        paramPanel.add(x3);
        paramPanel.add(x0);

        while (true) {
            int result = JOptionPane.showConfirmDialog(frame, paramPanel, "Entry data", JOptionPane.OK_CANCEL_OPTION);

            if (result != JOptionPane.OK_OPTION) {
                if (lastSelected == null) {
                    toolsGroup.clearSelection();
                    groupHash.get(0).clearSelection();
                } else {
                    toolsGroup.clearSelection();
                    groupHash.get(0).clearSelection();
                    toolsGroup.setSelected(lastSelected.getModel(), true);
                    groupHash.get(0).setSelected(radioHash.get(revToggleHash.get(lastSelected)).getModel(), true);
                }

                return;
            }

            String strRadius = radius.getText();
            String strRotation = rotation.getText();
            String strSides = sides.getText();

            if (strRadius.isEmpty() || strRotation.isEmpty() || strSides.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Input is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            try {
                int radData = Integer.parseInt(strRadius);
                double rotData = Double.parseDouble(strRotation);
                int sidData = Integer.parseInt(strSides);

                if (radData < radMin || radData > radMax || rotData < rotMin || rotData > rotMax || sidData < sidMin || sidData > sidMax) {
                    throw new NumberFormatException();
                }

                if (isStar.isSelected()) {
                    adapterHashMap.put(btn, adapters.getStarAdapter());
                    lastCh = true;
                } else {
                    adapterHashMap.put(btn, adapters.getPolygonAdapter());
                    lastCh = false;
                }

                panel.setRadius(radData);
                panel.setRotation(rotData);
                panel.setSides(sidData);

                if (lastSelected != null) {
                    panel.removeMouseListener(adapterHashMap.get(lastSelected));
                }

                panel.addMouseListener(adapterHashMap.get(selected));
                lastSelected = selected;
                radioHash.get(2).setSelected(true);
                toggleHash.get(2).setSelected(true);

                lastRadius = strRadius;
                lastRotation = strRotation;
                lastSides = strSides;

                break;
            } catch (NumberFormatException ex) {
                String msg = String.format("""
                        Error input!
                        Radius -- int from %d to %d;
                        Rotation -- double from %d to %d;
                        Sides -- int from %d to %d""",
                        radMin, radMax, rotMin, rotMax, sidMin, sidMax);
                JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void paramsLine(ActionEvent e, DrawingPanel panel, ButtonGroup toolsGroup, Frame frame) {
        JToggleButton selected;

        try {
            selected = (JToggleButton) e.getSource();
        } catch (ClassCastException exception) {
            selected = toggleHash.get(revRadioHash.get((JRadioButtonMenuItem) e.getSource()));
        }

        int min = 1, max = 50;

        if (lastSelected == selected) {
            toolsGroup.clearSelection();
            panel.removeMouseListener(adapterHashMap.get(selected));
            lastSelected = null;
            groupHash.get(0).clearSelection();
            return;
        }
        
        JPanel paramPanel = new JPanel();
        JTextField field = new JTextField(lastThick, 5);
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, Integer.parseInt(lastThick));

        slider.addChangeListener(e1 -> {
            if (ignoreChanges) {
                return;
            }

            ignoreChanges = true;
            field.setText(String.valueOf(slider.getValue()));
            ignoreChanges = false;
        });

        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { processTextFieldChange(field, slider); }
            public void removeUpdate(DocumentEvent e) { processTextFieldChange(field, slider); }
            public void changedUpdate(DocumentEvent e) { processTextFieldChange(field, slider); }
        });

        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.X_AXIS));
        paramPanel.add(new JLabel("Entry thick: "));
        paramPanel.add(field);
        paramPanel.add(slider);

        while (true) {
            int result = JOptionPane.showConfirmDialog(frame, paramPanel, "Entry data", JOptionPane.OK_CANCEL_OPTION);

            if (result != JOptionPane.OK_OPTION) {
                if (lastSelected == null) {
                    toolsGroup.clearSelection();
                    groupHash.get(0).clearSelection();
                } else {
                    toolsGroup.clearSelection();
                    groupHash.get(0).clearSelection();
                    toolsGroup.setSelected(lastSelected.getModel(), true);
                    groupHash.get(0).setSelected(radioHash.get(revToggleHash.get(lastSelected)).getModel(), true);
                }

                return;
            }

            String data = field.getText();

            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Input is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            try {
                int intData = Integer.parseInt(data);

                if (intData < min || intData > max) {
                    throw new NumberFormatException();
                }

                panel.setThick(intData);

                if (lastSelected != null) {
                    panel.removeMouseListener(adapterHashMap.get(lastSelected));
                }

                panel.addMouseListener(adapterHashMap.get(selected));
                lastSelected = selected;
                radioHash.get(0).setSelected(true);
                toggleHash.get(0).setSelected(true);

                lastThick = data;

                break;
            } catch (NumberFormatException ex) {
                String msg = String.format("Error input! Available int from %d to %d", min, max);
                JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void processTextFieldChange(JTextField field, JSlider slider) {
        if (ignoreChanges) {
            return;
        }

        String text = field.getText();

        if (text.matches("\\d+")) {
            try {
                int value = Integer.parseInt(text);

                if (value >= slider.getMinimum() && value <= slider.getMaximum()) {
                    ignoreChanges = true;
                    slider.setValue(value);
                    ignoreChanges = false;
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static void floProcessTextFieldChange(JTextField field, JSlider slider) {
        if (ignoreChanges) {
            return;
        }

        String text = field.getText();

        if (text.matches("\\d+(\\.\\d*)?")) {
            try {
                float value = Float.parseFloat(text);

                if (value >= slider.getMinimum() && value <= slider.getMaximum()) {
                    ignoreChanges = true;
                    slider.setValue((int)(10*value));
                    ignoreChanges = false;
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static void paramsPolygonOrStar(ActionEvent e, DrawingPanel panel, ButtonGroup toolsGroup, int sides, Frame frame, int ch) {
        JToggleButton selected;

        try {
            selected = (JToggleButton) e.getSource();
        } catch (ClassCastException exception) {
            selected = toggleHash.get(revRadioHash.get((JRadioButtonMenuItem) e.getSource()));
        }

        int radMin = 5, radMax = 300;
        int rotMin = 0, rotMax = 360;

        if (lastSelected == selected) {
            toolsGroup.clearSelection();
            panel.removeMouseListener(adapterHashMap.get(selected));
            lastSelected = null;
            groupHash.get(0).clearSelection();
            return;
        }

        JPanel x1 = new JPanel();
        JTextField radius = new JTextField(lastRadius, 5);
        JSlider sRadius = new JSlider(JSlider.HORIZONTAL, radMin, radMax, Integer.parseInt(lastRadius));
        x1.setLayout(new BoxLayout(x1, BoxLayout.X_AXIS));
        x1.add(new JLabel("Entry radius: ")); x1.add(radius); x1.add(sRadius);

        sRadius.addChangeListener(e1 -> {
            if (ignoreChanges) {
                return;
            }

            ignoreChanges = true;
            radius.setText(String.valueOf(sRadius.getValue()));
            ignoreChanges = false;
        });

        radius.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { processTextFieldChange(radius, sRadius); }
            public void removeUpdate(DocumentEvent e) { processTextFieldChange(radius, sRadius); }
            public void changedUpdate(DocumentEvent e) { processTextFieldChange(radius, sRadius); }
        });

        JPanel x2 = new JPanel();
        JTextField rotation = new JTextField(lastRotation, 5);
        JSlider sRotation = new JSlider(JSlider.HORIZONTAL, rotMin, 10*rotMax, (int)(10*Double.parseDouble(lastRotation)));
        x2.setLayout(new BoxLayout(x2, BoxLayout.X_AXIS));
        x2.add(new JLabel("Entry rotation: ")); x2.add(rotation); x2.add(sRotation);

        sRotation.addChangeListener(e2 -> {
            if (ignoreChanges) {
                return;
            }

            ignoreChanges = true;
            rotation.setText(String.format("%.1f", (float)sRotation.getValue()/10));
            ignoreChanges = false;
        });

        rotation.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { floProcessTextFieldChange(rotation, sRotation); }
            public void removeUpdate(DocumentEvent e) { floProcessTextFieldChange(rotation, sRotation); }
            public void changedUpdate(DocumentEvent e) { floProcessTextFieldChange(rotation, sRotation); }
        });

        JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.Y_AXIS));

        paramPanel.add(x1);
        paramPanel.add(x2);

        while (true) {
            int result = JOptionPane.showConfirmDialog(frame, paramPanel, "Entry data", JOptionPane.OK_CANCEL_OPTION);

            if (result != JOptionPane.OK_OPTION) {
                if (lastSelected == null) {
                    toolsGroup.clearSelection();
                    groupHash.get(0).clearSelection();
                } else {
                    toolsGroup.clearSelection();
                    groupHash.get(0).clearSelection();
                    toolsGroup.setSelected(lastSelected.getModel(), true);
                    groupHash.get(0).setSelected(radioHash.get(revToggleHash.get(lastSelected)).getModel(), true);
                }

                return;
            }

            String strRadius = radius.getText();
            String strRotation = rotation.getText();

            if (strRadius.isEmpty() || strRotation.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Input is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            try {
                int radData = Integer.parseInt(strRadius);
                double rotData = Double.parseDouble(strRotation);

                if (radData < radMin || radData > radMax || rotData < rotMin || rotData > rotMax) {
                    throw new NumberFormatException();
                }

                panel.setSides(sides);
                panel.setRadius(radData);
                panel.setRotation(rotData);

                if (lastSelected != null) {
                    panel.removeMouseListener(adapterHashMap.get(lastSelected));
                }

                panel.addMouseListener(adapterHashMap.get(selected));
                lastSelected = selected;
                radioHash.get(sides-1+ch*5).setSelected(true);
                toggleHash.get(sides-1+ch*5).setSelected(true);

                lastRadius = strRadius;
                lastRotation = strRotation;

                break;
            } catch (NumberFormatException ex) {
                String msg = String.format("""
                        Error input!
                        Radius -- int from %d to %d;
                        Rotation -- double from %d to %d""",
                        radMin, radMax, rotMin, rotMax);
                JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void changing(ActionEvent e, DrawingPanel panel, ButtonGroup toolsGroup) {
        JToggleButton selected;

        try {
            selected = (JToggleButton) e.getSource();
        } catch (ClassCastException exception) {
            selected = toggleHash.get(revRadioHash.get((JRadioButtonMenuItem) e.getSource()));
        }

        if (lastSelected == null) {
            panel.addMouseListener(adapterHashMap.get(selected));
            lastSelected = selected;
            radioHash.get(1).setSelected(true);

            if (!toolsGroup.isSelected(selected.getModel())) {
                toolsGroup.setSelected(selected.getModel(), true);
            }
        } else {
            if (lastSelected == selected) {
                toolsGroup.clearSelection();
                panel.removeMouseListener(adapterHashMap.get(selected));
                lastSelected = null;
                groupHash.get(0).clearSelection();
            } else {
                panel.removeMouseListener(adapterHashMap.get(lastSelected));
                panel.addMouseListener(adapterHashMap.get(selected));
                lastSelected = selected;
                radioHash.get(1).setSelected(true);

                if (!toolsGroup.isSelected(selected.getModel())) {
                    toolsGroup.setSelected(selected.getModel(), true);
                }
            }
        }
    }

    private static void setColors(DrawingPanel panel, JToolBar toolBar, JButton colorCh) {
        JButton blackButton = new JButton(" ");
        blackButton.addActionListener((event) -> {
            panel.setColor(0, 0, 0);
            colorCh.setBackground(new Color(0, 0, 0));});
        blackButton.setToolTipText("Black color");
        blackButton.setBackground(Color.BLACK);
        toolBar.add(blackButton);

        JButton redButton = new JButton(" ");
        redButton.addActionListener((event) -> {
            panel.setColor(255, 0, 0);
            colorCh.setBackground(new Color(255, 0, 0));});
        redButton.setToolTipText("Red color");
        redButton.setBackground(Color.RED);
        toolBar.add(redButton);

        JButton rgButton = new JButton(" ");
        rgButton.addActionListener((event) -> {
            panel.setColor(255, 255, 0);
            colorCh.setBackground(new Color(255, 255, 0));});
        rgButton.setToolTipText("Yellow color");
        rgButton.setBackground(Color.YELLOW);
        toolBar.add(rgButton);

        JButton greenButton = new JButton(" ");
        greenButton.addActionListener((event) -> {
            panel.setColor(0, 255, 0);
            colorCh.setBackground(new Color(0, 255, 0));});
        greenButton.setToolTipText("Green color");
        greenButton.setBackground(Color.GREEN);
        toolBar.add(greenButton);

        JButton gbButton = new JButton(" ");
        gbButton.addActionListener((event) -> {
            panel.setColor(0, 255, 255);
            colorCh.setBackground(new Color(0, 255, 255));});
        gbButton.setToolTipText("Cyan color");
        gbButton.setBackground(Color.CYAN);
        toolBar.add(gbButton);

        JButton blueButton = new JButton(" ");
        blueButton.addActionListener((event) -> {
            panel.setColor(0, 0, 255);
            colorCh.setBackground(new Color(0, 0, 255));});
        blueButton.setToolTipText("Blue color");
        blueButton.setBackground(Color.BLUE);
        toolBar.add(blueButton);

        JButton rbButton = new JButton(" ");
        rbButton.addActionListener((event) -> {
            panel.setColor(255, 0, 255);
            colorCh.setBackground(new Color(255, 0, 255));});
        rbButton.setToolTipText("Magenta color");
        rbButton.setBackground(Color.MAGENTA);
        toolBar.add(rbButton);

        JButton whiteButton = new JButton(" ");
        whiteButton.addActionListener((event) -> {
            panel.setColor(255, 255, 255);
            colorCh.setBackground(new Color(255, 255, 255));});
        whiteButton.setToolTipText("White color");
        whiteButton.setBackground(Color.WHITE);
        toolBar.add(whiteButton);
    }

    private static Icon getScaledImage(String path) {
        URL urlPath = WindowBuilder.class.getResource(path);

        if (urlPath == null) {
            return null;
        }

        ImageIcon origIcon = new ImageIcon(urlPath);
        Image img = origIcon.getImage().getScaledInstance(SIZE, SIZE, Image.SCALE_SMOOTH);

        return new ImageIcon(img);
    }

    private static JToggleButton getToolBtn(Icon toolIcon, String name) {
        if (toolIcon == null) {
            return new JToggleButton(name);
        } else {
            return new JToggleButton(toolIcon);
        }
    }
}

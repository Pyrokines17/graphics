package ru.nsu;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ru.nsu.components.Model;
import ru.nsu.components.redactor.View;
import ru.nsu.components.scene.View3D;
import ru.nsu.components.secondary.Helper;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;

public class Wireframe extends JFrame {
    private Model model = null;
    private View view = null;

    private final static int DEF_SIZE = 20;

    public Wireframe() {
        super("Wireframe");

        try {
            Object obj = new JSONParser().parse(new FileReader("src/main/resources/test.json"));
            JSONObject jsonObject = (JSONObject) obj;
            model = new Model(jsonObject);
        } catch (FileNotFoundException exception) {
            System.err.println("File not found: " + exception.getMessage());
        } catch (RuntimeException ex) {
            System.err.println("Runtime error: " + ex.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }

        if (model == null) {
            System.out.println("Model is null");
            model = new Model();
        }

        setMinimumSize(new Dimension(640, 480));
        setPreferredSize(new Dimension(1020, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        View3D view3D = new View3D(model);
        model.setDependentPanel(view3D);
        add(view3D);

        add(setupToolBar(view3D), BorderLayout.NORTH);
        setJMenuBar(setupMenuBar(view3D));

        pack();
        setVisible(true);
    }

    private JMenuBar setupMenuBar(View3D view3D) {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        JMenuItem loadItem = new JMenuItem("Load");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem redactorItem = new JMenuItem("Redactor");
        JMenuItem resetItem = new JMenuItem("Reset");

        exitItem.addActionListener(e -> System.exit(0));
        loadItem.addActionListener(e -> loadFromJSON());
        saveItem.addActionListener(e -> saveToJSON());
        redactorItem.addActionListener(e -> getRedactor());
        resetItem.addActionListener(e -> resetView3D(view3D));

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = setupAboutMenuItem();

        fileMenu.add(exitItem);
        fileMenu.add(new JSeparator());
        fileMenu.add(loadItem);
        fileMenu.add(saveItem);
        fileMenu.add(new JSeparator());
        fileMenu.add(redactorItem);
        fileMenu.add(resetItem);

        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JToolBar setupToolBar(View3D view3D) {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        toolbar.add(getRedactorButton());
        toolbar.add(getResetButton(view3D));
        toolbar.add(getLoadButton());
        toolbar.add(getSaveButton());

        return toolbar;
    }

    private JMenuItem setupAboutMenuItem() {
        JMenuItem aboutItem = new JMenuItem("About");

        aboutItem.addActionListener(e -> {
            URL aboutURL = getClass().getResource("/about.md");

            if (aboutURL != null) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(aboutURL.openStream()));
                    StringBuilder content = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }

                    reader.close();

                    String html = Helper.parseMarkdown(content.toString());
                    JTextPane textPane = new JTextPane();
                    textPane.setContentType("text/html");
                    textPane.setText(html);
                    textPane.setEditable(false);
                    textPane.setCaretPosition(0);
                    textPane.setPreferredSize(new Dimension(400, 300));

                    JScrollPane scrollPane = new JScrollPane(textPane);
                    scrollPane.setPreferredSize(new Dimension(400, 300));
                    JOptionPane.showMessageDialog(this, scrollPane, "About", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    System.err.println(ex.getLocalizedMessage());
                }
            } else {
                System.err.println("About file not found");
            }
        });

        return aboutItem;
    }

    private JButton getRedactorButton() {
        ImageIcon icon = getImageIcon("/icons/spline.png");
        JButton button;

        if (icon != null) {
            button = new JButton(icon);
        } else {
            button = new JButton("Spline");
        }

        button.addActionListener(e -> getRedactor());
        button.setToolTipText("Open Spline Redactor");

        return button;
    }

    private JButton getResetButton(View3D view3D) {
        ImageIcon icon = getImageIcon("/icons/reset.png");
        JButton button;

        if (icon != null) {
            button = new JButton(icon);
        } else {
            button = new JButton("Reset");
        }

        button.addActionListener(e -> resetView3D(view3D));
        button.setToolTipText("Reset View");

        return button;
    }

    private JButton getLoadButton() {
        ImageIcon icon = getImageIcon("/icons/load.png");
        JButton button;

        if (icon != null) {
            button = new JButton(icon);
        } else {
            button = new JButton("Load");
        }

        button.addActionListener(e -> loadFromJSON());
        button.setToolTipText("Load JSON File");

        return button;
    }

    private JButton getSaveButton() {
        ImageIcon icon = getImageIcon("/icons/save.png");
        JButton button;

        if (icon != null) {
            button = new JButton(icon);
        } else {
            button = new JButton("Save");
        }

        button.addActionListener(e -> saveToJSON());
        button.setToolTipText("Save JSON File");

        return button;
    }

    private void getRedactor() {
        if (view == null || !view.isDisplayable()) {
            view = new View(model);
            view.setVisible(true);
        } else {
            view.toFront();
        }
    }

    private void resetView3D(View3D view3D) {
        view3D.resetAngles();
        view3D.repaint();
    }

    private void loadFromJSON() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose a JSON file");

            int userSelection = fileChooser.showOpenDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToOpen = fileChooser.getSelectedFile();
                Object obj = new JSONParser().parse(new FileReader(fileToOpen));
                JSONObject jsonObject = (JSONObject) obj;

                model.loadFromJSON(jsonObject);
            }
        } catch (FileNotFoundException exception) {
            JOptionPane.showMessageDialog(this,
                    "File not found: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Runtime error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error parsing JSON: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveToJSON() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose a JSON file");

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                FileWriter fileWriter = new FileWriter(fileToSave);
                JSONObject jsonObject = new JSONObject();
                model.saveToJSON(jsonObject);

                fileWriter.write(jsonObject.toJSONString());
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (FileNotFoundException exception) {
            JOptionPane.showMessageDialog(this,
                    "File not found: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Runtime error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving JSON: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ImageIcon getImageIcon(String path) {
        URL imageURL = getClass().getResource(path);

        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);
            icon.setImage(icon.getImage().getScaledInstance(DEF_SIZE, DEF_SIZE, Image.SCALE_SMOOTH));
            return icon;
        } else {
            System.err.println("Image not found: " + path);
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Wireframe::new);
    }
}
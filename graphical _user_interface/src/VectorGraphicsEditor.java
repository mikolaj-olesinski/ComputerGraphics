import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class VectorGraphicsEditor {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Vector Graphics Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        Container contentPane = frame.getContentPane();
        contentPane.add(new DrawingPane());

        frame.setVisible(true);
    }
}

// Main drawing panel
class DrawingPane extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
    private List<Shape> shapes = new ArrayList<>();
    private int startX, startY;
    private Shape selectedShape;
    private boolean isMovingShape = false;
    private boolean isDraggingEnd1 = false;
    private boolean isDraggingEnd2 = false;
    private ShapeType currentShapeType = ShapeType.LINE;
    private boolean isDrawing = false;
    private Color currentColor = Color.BLACK;

    private JTextField redField, greenField, blueField;
    private JRadioButton lineButton, rectangleButton, circleButton;
    private JButton saveButton, loadButton, saveImageButton;

    enum ShapeType {
        LINE, RECTANGLE, CIRCLE
    }

    public DrawingPane() {
        setLayout(new BorderLayout());
        addMouseListener(this);
        addMouseMotionListener(this);

        // Create drawing area
        JPanel drawArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Draw all shapes
                for (Shape shape : shapes) {
                    shape.draw(g2d);
                }
            }
        };
        drawArea.setBackground(Color.WHITE);
        add(drawArea, BorderLayout.CENTER);

        // Create control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        // Shape selection controls
        ButtonGroup group = new ButtonGroup();
        lineButton = new JRadioButton("Line", true);
        rectangleButton = new JRadioButton("Rectangle");
        circleButton = new JRadioButton("Circle");

        lineButton.addActionListener(this);
        rectangleButton.addActionListener(this);
        circleButton.addActionListener(this);

        group.add(lineButton);
        group.add(rectangleButton);
        group.add(circleButton);

        controlPanel.add(new JLabel("Shape:"));
        controlPanel.add(lineButton);
        controlPanel.add(rectangleButton);
        controlPanel.add(circleButton);

        // Color selection controls
        controlPanel.add(new JLabel("Color (RGB):"));
        redField = new JTextField("0", 3);
        greenField = new JTextField("0", 3);
        blueField = new JTextField("0", 3);

        controlPanel.add(redField);
        controlPanel.add(greenField);
        controlPanel.add(blueField);

        // File operation buttons
        saveButton = new JButton("Save");
        loadButton = new JButton("Load");
        saveImageButton = new JButton("Save as Image");

        saveButton.addActionListener(this);
        loadButton.addActionListener(this);
        saveImageButton.addActionListener(this);

        controlPanel.add(saveButton);
        controlPanel.add(loadButton);
        controlPanel.add(saveImageButton);

        add(controlPanel, BorderLayout.SOUTH);

        // Make the drawing area have focus to capture keyboard events
        drawArea.setFocusable(true);
    }

    // Get current color from RGB fields
    private Color getCurrentColor() {
        try {
            int r = Integer.parseInt(redField.getText());
            int g = Integer.parseInt(greenField.getText());
            int b = Integer.parseInt(blueField.getText());

            r = Math.min(Math.max(r, 0), 255);
            g = Math.min(Math.max(g, 0), 255);
            b = Math.min(Math.max(b, 0), 255);

            return new Color(r, g, b);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }

    // Mouse event handlers
    @Override
    public void mousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();

        // Right-click to delete
        if (e.getButton() == MouseEvent.BUTTON3) {
            for (int i = shapes.size() - 1; i >= 0; i--) {
                if (shapes.get(i).isNearCenter(startX, startY)) {
                    shapes.remove(i);
                    repaint();
                    break;
                }
            }
            return;
        }

        // Left-click
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Check if we're near a shape or shape end for modification
            for (Shape shape : shapes) {
                if (shape instanceof Line) {
                    Line line = (Line) shape;
                    if (line.isNearEnd1(startX, startY)) {
                        selectedShape = line;
                        isDraggingEnd1 = true;
                        return;
                    } else if (line.isNearEnd2(startX, startY)) {
                        selectedShape = line;
                        isDraggingEnd2 = true;
                        return;
                    }
                }

                if (shape.isNearCenter(startX, startY)) {
                    selectedShape = shape;
                    isMovingShape = true;
                    return;
                }
            }

            // If not modifying, start drawing new shape
            isDrawing = true;
            currentColor = getCurrentColor();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // Redraw with XOR mode to handle temporary visuals
        Graphics2D g2d = (Graphics2D) getGraphics();
        g2d.setXORMode(getBackground());

        if (isDraggingEnd1 && selectedShape instanceof Line) {
            Line line = (Line) selectedShape;
            line.setEnd1(x, y);
            repaint();
        } else if (isDraggingEnd2 && selectedShape instanceof Line) {
            Line line = (Line) selectedShape;
            line.setEnd2(x, y);
            repaint();
        } else if (isMovingShape && selectedShape != null) {
            Point center = selectedShape.getCenter();
            int dx = x - center.x;
            int dy = y - center.y;
            selectedShape.move(dx, dy);
            repaint();
        } else if (isDrawing) {
            // Clear previous temporary drawing by repainting
            repaint();

            // Draw temporary shape
            if (currentShapeType == ShapeType.LINE) {
                g2d.setColor(currentColor);
                g2d.drawLine(startX, startY, x, y);
            } else if (currentShapeType == ShapeType.RECTANGLE) {
                int width = x - startX;
                int height = y - startY;
                g2d.setColor(currentColor);
                g2d.drawRect(startX, startY, width, height);
            } else if (currentShapeType == ShapeType.CIRCLE) {
                int radius = (int) Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
                g2d.setColor(currentColor);
                g2d.drawOval(startX - radius, startY - radius, radius * 2, radius * 2);
            }
        }

        g2d.dispose();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (isDraggingEnd1 || isDraggingEnd2 || isMovingShape) {
            // Finish modification
            repaint();
        } else if (isDrawing) {
            // Create new shape
            if (currentShapeType == ShapeType.LINE) {
                shapes.add(new Line(startX, startY, x, y, currentColor));
            } else if (currentShapeType == ShapeType.RECTANGLE) {
                int width = x - startX;
                int height = y - startY;
                shapes.add(new Rectangle(startX, startY, width, height, currentColor));
            } else if (currentShapeType == ShapeType.CIRCLE) {
                int radius = (int) Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
                shapes.add(new Circle(startX, startY, radius, currentColor));
            }
            repaint();
        }

        // Reset state
        isDrawing = false;
        isMovingShape = false;
        isDraggingEnd1 = false;
        isDraggingEnd2 = false;
        selectedShape = null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Nothing to do
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Nothing to do
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Nothing to do
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Nothing to do
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == lineButton) {
            currentShapeType = ShapeType.LINE;
        } else if (source == rectangleButton) {
            currentShapeType = ShapeType.RECTANGLE;
        } else if (source == circleButton) {
            currentShapeType = ShapeType.CIRCLE;
        } else if (source == saveButton) {
            saveToFile();
        } else if (source == loadButton) {
            loadFromFile();
        } else if (source == saveImageButton) {
            saveAsImage();
        }
    }

    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                for (Shape shape : shapes) {
                    writer.println(shape.toFileString());
                }
                JOptionPane.showMessageDialog(this, "File saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                shapes.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[0].equals("LINE")) {
                        int x1 = Integer.parseInt(parts[1]);
                        int y1 = Integer.parseInt(parts[2]);
                        int x2 = Integer.parseInt(parts[3]);
                        int y2 = Integer.parseInt(parts[4]);
                        int r = Integer.parseInt(parts[5]);
                        int g = Integer.parseInt(parts[6]);
                        int b = Integer.parseInt(parts[7]);
                        shapes.add(new Line(x1, y1, x2, y2, new Color(r, g, b)));
                    } else if (parts[0].equals("RECTANGLE")) {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int width = Integer.parseInt(parts[3]);
                        int height = Integer.parseInt(parts[4]);
                        int r = Integer.parseInt(parts[5]);
                        int g = Integer.parseInt(parts[6]);
                        int b = Integer.parseInt(parts[7]);
                        shapes.add(new Rectangle(x, y, width, height, new Color(r, g, b)));
                    } else if (parts[0].equals("CIRCLE")) {
                        int centerX = Integer.parseInt(parts[1]);
                        int centerY = Integer.parseInt(parts[2]);
                        int radius = Integer.parseInt(parts[3]);
                        int r = Integer.parseInt(parts[4]);
                        int g = Integer.parseInt(parts[5]);
                        int b = Integer.parseInt(parts[6]);
                        shapes.add(new Circle(centerX, centerY, radius, new Color(r, g, b)));
                    }
                }
                repaint();
                JOptionPane.showMessageDialog(this, "File loaded successfully!");
            } catch (IOException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveAsImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // Draw white background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Draw all shapes
            for (Shape shape : shapes) {
                shape.draw(g2d);
            }

            g2d.dispose();

            try {
                File file = fileChooser.getSelectedFile();
                String name = file.getName();
                String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
                if (!ext.equals("png") && !ext.equals("jpg") && !ext.equals("jpeg")) {
                    file = new File(file.getAbsolutePath() + ".png");
                    ext = "png";
                }

                ImageIO.write(image, ext, file);
                JOptionPane.showMessageDialog(this, "Image saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
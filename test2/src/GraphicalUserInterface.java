import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;



public class GraphicalUserInterface {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Zadanie 3 grafika");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        Container contentPane = frame.getContentPane();
        contentPane.add(new DrawingPane());

        frame.setVisible(true);
    }
}


class DrawingPane extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

    private int startX, startY, endX, endY;
    private int lastX, lastY;

    private Color currentColor = Color.BLACK;
    private boolean isDrawing = false;
    private boolean isDraggingShape = false;

    private List<Shape> shapes = new ArrayList<>();

    private Shape selectedShape = null;
    private boolean isDraggingEnd1 = false;
    private boolean isDraggingEnd2 = false;

    private JPanel controlPanel;
    private JTextField redField, greenField, blueField;
    private JRadioButton lineButton, rectangleButton, circleButton;
    private JButton saveButton, loadButton, saveImageButton;


    private boolean canDrawOutsideWindow = false;

    // Constructor
    public DrawingPane() {
        setLayout(new BorderLayout());

        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        requestFocusInWindow();

        controlPanel = new JPanel();
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
    }

    // Paint method to draw shapes
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Rysuj wszystkie kształty
        for (Shape shape : shapes) {
            shape.draw(g2d);
        }
    }

    // Get current color from RGB fields
    private Color getCurrentColor() {
        try {
            int r = Integer.parseInt(redField.getText());
            int g = Integer.parseInt(greenField.getText());
            int b = Integer.parseInt(blueField.getText());

            if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
                JOptionPane.showMessageDialog(this, "Wartości RGB muszą być w zakresie 0-255!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return Color.BLACK;
            }

            return new Color(r, g, b);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Proszę wprowadzić poprawne liczby całkowite dla RGB!", "Błąd", JOptionPane.ERROR_MESSAGE);
            return Color.BLACK;
        }
    }


    private int constrainX(int x) {
        if (!canDrawOutsideWindow) {
            return Math.max(0, Math.min(x, getWidth()));
        }
        return x;
    }

    private int constrainY(int y) {
        if (!canDrawOutsideWindow) {
            // Uwzględnij obszar rysowania bez panelu kontrolnego
            int drawingAreaHeight = getHeight() - controlPanel.getHeight();
            return Math.max(0, Math.min(y, drawingAreaHeight));
        }
        return y;
    }
    // Action listener for buttons
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveButton) {
            saveToFile();
        } else if (e.getSource() == loadButton) {
            loadFromFile();
            repaint();
        } else if (e.getSource() == saveImageButton) {
            saveAsImage();
        }

    }

    // Mouse event handlers
    @Override
    public void mousePressed(MouseEvent e) {
        startX = constrainX(e.getX());
        startY = constrainY(e.getY());

        // Left-click
        if (e.getButton() == MouseEvent.BUTTON1) {

            // Check if we are clicking on an existing shape
            for (Shape shape : shapes) {
                if (shape instanceof Line line) {
                    //Check if we are clicking on the ends of the line
                    if (line.isNearEnd1(startX, startY)) {
                        selectedShape = line;
                        isDraggingEnd1 = true;
                        break;
                    }
                    // Check if we are clicking on the end2 of the line
                    else if (line.isNearEnd2(startX, startY)) {
                        selectedShape = line;
                        isDraggingEnd2 = true;
                        break;
                    }
                }

                // If we click in the center of the shape
                if (selectedShape == null && shape.isNearCenter(startX, startY)) {
                    selectedShape = shape;
                    isDraggingShape = true;
                    break;
                }
            }

            // If we are not clicking on any shape and on and of the line
            if (selectedShape == null) {
                isDrawing = true;
                currentColor = getCurrentColor();
            }
        }

        else if (e.getButton() == MouseEvent.BUTTON3) {
            // Right-click to delete
            Shape toRemove = null;
            for (Shape shape : shapes) {
                if (shape.isNearCenter(startX, startY)) {
                    toRemove = shape;
                    break; //we found the shape to remove
                }
            }

            if (toRemove != null) {
                shapes.remove(toRemove); // delete the shape
                repaint();
            }
        }

//        repaint();

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int currentX = constrainX(e.getX());
        int currentY = constrainY(e.getY());
        Graphics2D g2d = (Graphics2D) getGraphics();
        g2d.setXORMode(getBackground());

        //Dragging shape
        if (selectedShape != null && isDraggingShape) {
            Point center = selectedShape.getCenter();
            int dx = currentX - center.x;
            int dy = currentY - center.y;
            selectedShape.move(dx, dy);
            repaint();
        }
        else if (selectedShape != null && (isDraggingEnd1 || isDraggingEnd2)) {
            Line line = (Line) selectedShape;
            if (isDraggingEnd1) {
                line.setEnd1(currentX, currentY);
            } else {
                line.setEnd2(currentX, currentY);
            }
            repaint();
        }

        if (isDrawing){
            g2d.setColor(currentColor);
            // Delete the previous shape (if exists)
            if (lastX != 0 || lastY != 0) {
                if (lineButton.isSelected()) {
                    g2d.drawLine(startX, startY, lastX, lastY);
                } else if (rectangleButton.isSelected()) {
                    int x = Math.min(startX, lastX);
                    int y = Math.min(startY, lastY);
                    int w = Math.abs(lastX - startX);
                    int h = Math.abs(lastY - startY);
                    g2d.drawRect(x, y, w, h);
                } else if (circleButton.isSelected()) {
                    int radius = (int) Math.sqrt(Math.pow(lastX - startX, 2) + Math.pow(lastY - startY, 2));
                    g2d.drawOval(startX - radius, startY - radius, radius * 2, radius * 2);
                }
            }

            //Draw the new shape
            if (lineButton.isSelected()) {
                g2d.drawLine(startX, startY, currentX, currentY);
            } else if (rectangleButton.isSelected()) {
                int x = Math.min(startX, currentX);
                int y = Math.min(startY, currentY);
                int w = Math.abs(currentX - startX);
                int h = Math.abs(currentY - startY);
                g2d.drawRect(x, y, w, h);
            } else if (circleButton.isSelected()) {
                int radius = (int) Math.sqrt(Math.pow(currentX - startX, 2) + Math.pow(currentY - startY, 2));
                g2d.drawOval(startX - radius, startY - radius, radius * 2, radius * 2);
            }

            //Remember the last position
            lastX = currentX;
            lastY = currentY;
        }

        g2d.dispose();
    }


    @Override
    public void mouseReleased(MouseEvent e) {

        if (isDraggingShape) {
            // Zakończ przesuwanie kształtu
            isDraggingShape = false;
            selectedShape = null;
        }
        else if (selectedShape != null && selectedShape instanceof Line) {
            Line line = (Line) selectedShape;
            if (isDraggingEnd1) {
                line.setEnd1(e.getX(), e.getY());
            } else {
                line.setEnd2(e.getX(), e.getY());
            }

            isDraggingEnd1 = false;
            isDraggingEnd2 = false;
            selectedShape = null;
        }

        else if (isDrawing) {
            endX = constrainX(e.getX());
            endY = constrainY(e.getY());

            // Create shape based on selected type
            if (lineButton.isSelected()) {
                // Create line
                System.out.println("Line drawn from (" + startX + ", " + startY + ") to (" + endX + ", " + endY + ")");
                shapes.add(new Line(startX, startY, endX, endY, currentColor));
            }else if (rectangleButton.isSelected()) {
                // Create rectangle
                int x = Math.min(startX, endX);
                int y = Math.min(startY, endY);
                int w = Math.abs(endX - startX);
                int h = Math.abs(endY - startY);
                System.out.println("Rectangle drawn from (" + startX + ", " + startY + ") to (" + endX + ", " + endY + ")");
                shapes.add(new Rectangle(x, y, w, h, currentColor));
            } else if (circleButton.isSelected()) {
                // Create circle
                int radius = (int) Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                System.out.println("Circle drawn with center (" + startX + ", " + startY + ") and radius " + radius);
                shapes.add(new Circle(startX, startY, radius, currentColor));
            }

            isDrawing = false;
        }

        repaint();
        lastX = 0;
        lastY = 0;
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


    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
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
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                List<Shape> tempShapes = new ArrayList<>(); // Temporary list to store shapes
                String line;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (line.trim().isEmpty()) continue; // Skip empty lines

                    String[] parts = line.split(",");

                    // Validate minimum number of parts (at least type + coords + RGB)
                    if (parts.length < 5) {
                        throw new IllegalArgumentException("Line " + lineNumber +
                                ": Invalid format, not enough parameters");
                    }

                    String shapeType = parts[0];

                    // Validate shape type
                    if (!shapeType.equals("LINE") && !shapeType.equals("RECTANGLE") &&
                            !shapeType.equals("CIRCLE")) {
                        throw new IllegalArgumentException("Line " + lineNumber +
                                ": Unknown shape type: " + shapeType);
                    }

                    try {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);

                        // Check if we have enough parts for RGB values
                        if (parts.length <
                                (shapeType.equals("LINE") ? 7 :
                                        shapeType.equals("RECTANGLE") ? 7 : 6)) {
                            throw new IllegalArgumentException("Line " + lineNumber +
                                    ": Missing RGB values");
                        }

                        // Get RGB values (always at the end)
                        int r = Integer.parseInt(parts[parts.length - 3]);
                        int g = Integer.parseInt(parts[parts.length - 2]);
                        int b = Integer.parseInt(parts[parts.length - 1]);

                        // Validate RGB values
                        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
                            throw new IllegalArgumentException("Line " + lineNumber +
                                    ": RGB values must be between 0-255");
                        }

                        Color color = new Color(r, g, b);

                        // Parse shape-specific parameters and add to temp list
                        switch (shapeType) {
                            case "LINE":
                                if (parts.length != 7) {
                                    throw new IllegalArgumentException("Line " + lineNumber +
                                            ": Invalid LINE format, expected 7 parameters");
                                }
                                int endX = Integer.parseInt(parts[3]);
                                int endY = Integer.parseInt(parts[4]);
                                tempShapes.add(new Line(x, y, endX, endY, color));
                                break;

                            case "RECTANGLE":
                                if (parts.length != 7) {
                                    throw new IllegalArgumentException("Line " + lineNumber +
                                            ": Invalid RECTANGLE format, expected 7 parameters");
                                }
                                int width = Integer.parseInt(parts[3]);
                                int height = Integer.parseInt(parts[4]);
                                if (width < 0 || height < 0) {
                                    throw new IllegalArgumentException("Line " + lineNumber +
                                            ": Rectangle width and height must be positive");
                                }
                                tempShapes.add(new Rectangle(x, y, width, height, color));
                                break;

                            case "CIRCLE":
                                if (parts.length != 6) {
                                    throw new IllegalArgumentException("Line " + lineNumber +
                                            ": Invalid CIRCLE format, expected 6 parameters");
                                }
                                int radius = Integer.parseInt(parts[3]);
                                if (radius < 0) {
                                    throw new IllegalArgumentException("Line " + lineNumber +
                                            ": Circle radius must be positive");
                                }
                                tempShapes.add(new Circle(x, y, radius, color));
                                break;
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Line " + lineNumber +
                                ": Invalid number format: " + e.getMessage());
                    }
                }

                // If we've successfully parsed all shapes, update the actual shapes list
                shapes.clear();
                shapes.addAll(tempShapes);
                JOptionPane.showMessageDialog(this, "File loaded successfully with " +
                        shapes.size() + " shapes!");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Error parsing file: " + ex.getMessage(),
                        "Format Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveAsImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
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

//    //wersja 1
//    @Override
//    public void mouseDragged(MouseEvent e) {
//        int currentX = constrainX(e.getX());
//        int currentY = constrainY(e.getY());
//        Graphics2D g2d = (Graphics2D) getGraphics();
//        g2d.setXORMode(getBackground());
//
//        //Dragging shape
//        if (selectedShape != null && isDraggingShape) {
//
//            //Calculate the difference in position
//            int dx = currentX - lastX;
//            int dy = currentY - lastY;
//
//
//            if (lastX != 0 || lastY != 0) {
//                //Delete the previous shape
//                selectedShape.draw(g2d);
//                // Move the shape
//                selectedShape.move(dx, dy);
//            }
//            else {
//                //If the first time we delete the original shape
//                selectedShape.draw(g2d);
//            }
//
//            //Draw the new shape
//            selectedShape.draw(g2d);
//
//            // Update the last position
//            lastX = currentX;
//            lastY = currentY;
//        }
//
//        //If we drag the end of the line
//        else if(selectedShape != null && (isDraggingEnd1 || isDraggingEnd2)) {
//            Line line = (Line) selectedShape;
//            g2d.setColor(line.getColor());
//
//            Point end1 = line.getEnd1();
//            Point end2 = line.getEnd2();
//
//            // Delete the previous line
//            if (lastX != 0 || lastY != 0) {
//                if (isDraggingEnd1) {
//                    //if we are dragging the end1 of the line
//                    System.out.println("Dragging end1");
//                    g2d.drawLine(lastX, lastY, end2.x, end2.y);
//                }
//                else {
//                    //if we are dragging the end2 of the line
//                    g2d.drawLine(end1.x, end1.y, lastX, lastY);
//                }
//            } else {
//                //If the first time we delete the original line
//                g2d.drawLine(end1.x, end1.y, end2.x, end2.y);
//            }
//
//            // Draw the new line
//            if (isDraggingEnd1) {
//                g2d.drawLine(currentX, currentY, end2.x, end2.y);
//            } else {
//                g2d.drawLine(end1.x, end1.y, currentX, currentY);
//            }
//
//            lastX = currentX;
//            lastY = currentY;
//        }
//
//        if (isDrawing){
//            g2d.setColor(currentColor);
//            // Delete the previous shape (if exists)
//            if (lastX != 0 || lastY != 0) {
//                if (lineButton.isSelected()) {
//                    g2d.drawLine(startX, startY, lastX, lastY);
//                } else if (rectangleButton.isSelected()) {
//                    int x = Math.min(startX, lastX);
//                    int y = Math.min(startY, lastY);
//                    int w = Math.abs(lastX - startX);
//                    int h = Math.abs(lastY - startY);
//                    g2d.drawRect(x, y, w, h);
//                } else if (circleButton.isSelected()) {
//                    int radius = (int) Math.sqrt(Math.pow(lastX - startX, 2) + Math.pow(lastY - startY, 2));
//                    g2d.drawOval(startX - radius, startY - radius, radius * 2, radius * 2);
//                }
//            }
//
//            //Draw the new shape
//            if (lineButton.isSelected()) {
//                g2d.drawLine(startX, startY, currentX, currentY);
//            } else if (rectangleButton.isSelected()) {
//                int x = Math.min(startX, currentX);
//                int y = Math.min(startY, currentY);
//                int w = Math.abs(currentX - startX);
//                int h = Math.abs(currentY - startY);
//                g2d.drawRect(x, y, w, h);
//            } else if (circleButton.isSelected()) {
//                int radius = (int) Math.sqrt(Math.pow(currentX - startX, 2) + Math.pow(currentY - startY, 2));
//                g2d.drawOval(startX - radius, startY - radius, radius * 2, radius * 2);
//            }
//
//            //Remember the last position
//            lastX = currentX;
//            lastY = currentY;
//        }
//
//        g2d.dispose();
//    }



}

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;



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


class DrawingPane extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

    private int startX, startY, endX, endY;
    private int lastX, lastY;
    private Color currentColor = Color.BLACK;
    private boolean isDrawing = false;
    private List<Shape> shapes = new ArrayList<>();

    private Shape selectedShape = null;
    private boolean isDraggingEnd1 = false;
    private boolean isDraggingEnd2 = false;

    private JTextField redField, greenField, blueField;
    private JRadioButton lineButton, rectangleButton, circleButton;
    private JButton saveButton, loadButton, saveImageButton;


    public DrawingPane() {
        setLayout(new BorderLayout());

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

        add(drawArea, BorderLayout.CENTER);
        addMouseListener(this);
        addMouseMotionListener(this);
        drawArea.setFocusable(true);


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

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();

        // Left-click
        if (e.getButton() == MouseEvent.BUTTON1) {

            for (Shape shape : shapes) {
                if (shape instanceof Line) {
                    Line line = (Line) shape;
                    if (line.isNearEnd1(startX, startY)) {
                        selectedShape = line;
                        isDraggingEnd1 = true;
                        break;
                    } else if (line.isNearEnd2(startX, startY)) {
                        selectedShape = line;
                        isDraggingEnd2 = true;
                        break;
                    }
                }
            }

            if (selectedShape != null) {
                // If a shape is selected, start dragging
                isDrawing = false;
            } else {
                // No shape selected, start drawing
                isDrawing = true;
                currentColor = getCurrentColor();
            }
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if (selectedShape != null && selectedShape instanceof Line) {
            Line line = (Line) selectedShape;
            if (isDraggingEnd1) {
                line.setEnd1(e.getX(), e.getY());
            } else {
                line.setEnd2(e.getX(), e.getY());
            }

            isDraggingEnd1 = false;
            isDraggingEnd2 = false;
            selectedShape = null;
            lastX = 0;
            lastY = 0;
            repaint();
        }

        else if (isDrawing) {
            endX = e.getX();
            endY = e.getY();

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
                System.out.println("Circle drawn with center at (" + startX + ", " + startY + ") and radius " +
                        Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2)));
                int radius = (int) Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                shapes.add(new Circle(startX, startY, radius, currentColor));
            }

            isDrawing = false;
            lastX = 0;
            lastY = 0;
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int currentX = e.getX();
        int currentY = e.getY();
        Graphics2D g2d = (Graphics2D) getGraphics();
        g2d.setXORMode(getBackground());


        if(selectedShape != null && (isDraggingEnd1 || isDraggingEnd2)) {
            Line line = (Line) selectedShape;
            g2d.setColor(line.getColor());

            Point end1 = line.getEnd1();
            Point end2 = line.getEnd2();

            // Usuń poprzednią linię
            if (lastX != 0 || lastY != 0) {
                if (isDraggingEnd1) {
                    g2d.drawLine(lastX, lastY, end1.x, end1.y);
                } else {
                    g2d.drawLine(end1.x, end1.y, lastX, lastY);
                }
            } else {
                // Pierwszy raz - rysujemy oryginalną linię aby ją usunąć
                g2d.drawLine(end1.x, end1.y, end2.x, end2.y);
            }

            // Rysuj nową linię
            if (isDraggingEnd1) {
                g2d.drawLine(currentX, currentY, end1.x, end1.y);
            } else {
                g2d.drawLine(end1.x, end1.y, currentX, currentY);
            }

            lastX = currentX;
            lastY = currentY;
        }

        if (isDrawing){
            // Usuń poprzednią tymczasową linię (jeśli istnieje)
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

            // Narysuj nową tymczasową linię
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

            // Zapamiętaj aktualną pozycję
            lastX = currentX;
            lastY = currentY;
        }

        g2d.dispose();
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
}
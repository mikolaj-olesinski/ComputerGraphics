import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.imageio.*;

public class PosterEditor extends JFrame {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int THUMBNAIL_SIZE = 80;
    private static final double ROTATION_ANGLE = Math.PI / 36; // 5 degrees
    private static final int TRANSLATION_STEP = 1; // 1 pixel

    private JPanel thumbnailPanel;
    private JPanel shapesPanel;
    private PosterPanel posterPanel;
    private JPanel controlPanel;
    private List<PosterElement> posterElements = new ArrayList<>();
    private PosterElement selectedElement = null;
    private File imageDirectory;

    public PosterEditor() {
        super("Poster Editor");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initPanels();
        initControlButtons();
        loadImageThumbnails();
        createShapeGallery();

        setVisible(true);
    }

    private void initPanels() {
        // Main panel divided into two parts
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Left panel (divided into upper and lower)
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 10));

        // Thumbnail images panel (upper left)
        thumbnailPanel = new JPanel();
        thumbnailPanel.setLayout(new GridLayout(0, 3, 5, 5));
        thumbnailPanel.setBorder(BorderFactory.createTitledBorder("Images"));
        JScrollPane thumbnailScroll = new JScrollPane(thumbnailPanel);
        thumbnailScroll.setPreferredSize(new Dimension(300, 350));

        // Shapes a panel (lower left)
        shapesPanel = new JPanel();
        shapesPanel.setLayout(new GridLayout(0, 3, 5, 5));
        shapesPanel.setBorder(BorderFactory.createTitledBorder("Shapes"));
        JScrollPane shapesScroll = new JScrollPane(shapesPanel);
        shapesScroll.setPreferredSize(new Dimension(300, 350));

        leftPanel.add(thumbnailScroll);
        leftPanel.add(shapesScroll);

        // Poster panel (right)
        posterPanel = new PosterPanel();
        JScrollPane posterScroll = new JScrollPane(posterPanel);
        posterScroll.setPreferredSize(new Dimension(850, 700));

        // Control panel (bottom)
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(posterScroll, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void initControlButtons() {
        JButton moveLeftBtn = new JButton("←");
        JButton moveRightBtn = new JButton("→");
        JButton moveUpBtn = new JButton("↑");
        JButton moveDownBtn = new JButton("↓");
        JButton rotateLeftBtn = new JButton("⟲");
        JButton rotateRightBtn = new JButton("⟳");
        JButton bringToFrontBtn = new JButton("To Front");
        JButton sendToBackBtn = new JButton("To Back");
        JButton deleteBtn = new JButton("Delete");

        moveLeftBtn.addActionListener(e -> moveSelectedInScreenCoordinates(-TRANSLATION_STEP, 0));
        moveRightBtn.addActionListener(e -> moveSelectedInScreenCoordinates(TRANSLATION_STEP, 0));
        moveUpBtn.addActionListener(e -> moveSelectedInScreenCoordinates(0, -TRANSLATION_STEP));
        moveDownBtn.addActionListener(e -> moveSelectedInScreenCoordinates(0, TRANSLATION_STEP));
        rotateLeftBtn.addActionListener(e -> rotateSelected(-ROTATION_ANGLE));
        rotateRightBtn.addActionListener(e -> rotateSelected(ROTATION_ANGLE));
        bringToFrontBtn.addActionListener(e -> bringToFront());
        sendToBackBtn.addActionListener(e -> sendToBack());
        deleteBtn.addActionListener(e -> deleteSelected());

        controlPanel.add(moveLeftBtn);
        controlPanel.add(moveRightBtn);
        controlPanel.add(moveUpBtn);
        controlPanel.add(moveDownBtn);
        controlPanel.add(rotateLeftBtn);
        controlPanel.add(rotateRightBtn);
        controlPanel.add(bringToFrontBtn);
        controlPanel.add(sendToBackBtn);
        controlPanel.add(deleteBtn);


        JButton saveBtn = new JButton("Save");
        JButton loadBtn = new JButton("Load");

        saveBtn.addActionListener(e -> savePosterToFile());
        loadBtn.addActionListener(e -> loadPosterFromFile());

        controlPanel.add(saveBtn);
        controlPanel.add(loadBtn);

        JButton exportImageBtn = new JButton("Export Image");
        exportImageBtn.addActionListener(e -> exportPosterToImage());
        controlPanel.add(exportImageBtn);
    }

    private void savePosterToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("Save Poster");

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Add .poster extension if not specified
            if (!file.getName().toLowerCase().endsWith(".poster")) {
                file = new File(file.getAbsolutePath() + ".poster");
            }

            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("POSTER_FORMAT");

                for (PosterElement element : posterElements) {
                    writer.println(element.save());
                }

                JOptionPane.showMessageDialog(this,
                        "Poster saved successfully to " + file.getName(),
                        "Save Complete", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving poster: " + ex.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void loadPosterFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("Load Poster");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String formatLine = reader.readLine();

                if (!"POSTER_FORMAT".equals(formatLine)) {
                    throw new IOException("Unsupported file format");
                }

                List<PosterElement> newElements = new ArrayList<>();

                String elementData;
                while ((elementData = reader.readLine()) != null) {
                    PosterElement element = PosterElement.deserialize(elementData);
                    if (element != null) {
                        newElements.add(element);
                    }
                }

                // Replace current elements with loaded ones
                posterElements.clear();
                posterElements.addAll(newElements);
                selectedElement = null;
                posterPanel.repaint();

                JOptionPane.showMessageDialog(this,
                        "Poster loaded successfully from " + file.getName(),
                        "Load Complete", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading poster: " + ex.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void exportPosterToImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("Export Image");

        // Add extension filters
        String[] formats = {"png", "jpg", "bmp"};
        for (String format : formats) {
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith("." + format);
                }

                @Override
                public String getDescription() {
                    return format.toUpperCase() + " Images (*." + format + ")";
                }
            });
        }

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String format = "png"; // Default format

            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                format = "jpg";
            } else if (fileName.endsWith(".bmp")) {
                format = "bmp";
            } else if (!fileName.endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }

            savePosterAsImage(file, format);
        }
    }

    private void savePosterAsImage(File file, String format) {
        // Calculate bounding box of all elements
        Rectangle2D bounds = calculatePosterBounds();

        if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "No elements to export or invalid bounds.",
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create image buffer with the exact size needed for all elements
        int width = (int)Math.ceil(bounds.getWidth());
        int height = (int)Math.ceil(bounds.getHeight());

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Set high quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // Fill with white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Apply transformation to account for element positions
        g2d.translate(-bounds.getX(), -bounds.getY());

        // Draw all elements
        for (PosterElement element : posterElements) {
            element.draw(g2d);
        }

        g2d.dispose();

        try {
            ImageIO.write(image, format, file);
            JOptionPane.showMessageDialog(this,
                    "Image exported successfully to " + file.getName(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error exporting image: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private Rectangle2D calculatePosterBounds() {
        if (posterElements.isEmpty()) {
            return new Rectangle2D.Double(0, 0, 800, 600); // Default size
        }

        // Find the bounds that include all elements
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (PosterElement element : posterElements) {
            Rectangle2D bounds = element.getBounds();

            minX = Math.min(minX, bounds.getX());
            minY = Math.min(minY, bounds.getY());
            maxX = Math.max(maxX, bounds.getX() + bounds.getWidth());
            maxY = Math.max(maxY, bounds.getY() + bounds.getHeight());
        }

        // Add small margin
        double margin = 10.0;
        return new Rectangle2D.Double(
                minX - margin,
                minY - margin,
                maxX - minX + 2 * margin,
                maxY - minY + 2 * margin
        );
    }



    private void loadImageThumbnails() {
        // In practice, choose a directory or load from a specific location
        imageDirectory = new File("images");
        if (!imageDirectory.exists() || !imageDirectory.isDirectory()) {
            System.out.println("Current working directory: " + System.getProperty("user.dir"));
            JOptionPane.showMessageDialog(this,
                    "The 'images' directory does not exist. Create it and place images there.");
            return;
        }

        File[] imageFiles = imageDirectory.listFiles((dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                    name.endsWith(".png");
        });

        if (imageFiles == null || imageFiles.length == 0) {
            JOptionPane.showMessageDialog(this, "No images in the 'images' directory.");
            return;
        }

        for (File file : imageFiles) {
            try {
                BufferedImage original = ImageIO.read(file);
                if (original != null) {
                    // Create thumbnail
                    BufferedImage thumbnail = createThumbnail(original, THUMBNAIL_SIZE);
                    JLabel label = new JLabel(new ImageIcon(thumbnail));
                    label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    label.setToolTipText(file.getName());

                    // Add Drag and Drop handling for the thumbnail
                    setupDragSource(label, new ImageElement(original, file.getName()));
                    thumbnailPanel.add(label);
                }
            } catch (IOException e) {
                System.err.println("Cannot load image: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    private BufferedImage createThumbnail(BufferedImage original, int size) {
        int width = original.getWidth();
        int height = original.getHeight();
        double scale = Math.min((double) size / width, (double) size / height);

        int thumbWidth = (int) (width * scale);
        int thumbHeight = (int) (height * scale);

        BufferedImage thumbnail = new BufferedImage(thumbWidth, thumbHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, thumbWidth, thumbHeight, null);
        g2d.dispose();

        return thumbnail;
    }

    private void createShapeGallery() {
        // Create a list of shapes to display
        List<PosterElement> shapesList = new ArrayList<>();

        // Add rectangles with different colors
        shapesList.add(new RectangleElement(Color.RED));
        shapesList.add(new RectangleElement(Color.GREEN));
        shapesList.add(new RectangleElement(Color.BLUE));
        shapesList.add(new RectangleElement(Color.YELLOW));

        // Add circles with different colors
        shapesList.add(new CircleElement(Color.ORANGE));
        shapesList.add(new CircleElement(Color.MAGENTA));
        shapesList.add(new CircleElement(Color.CYAN));
        shapesList.add(new CircleElement(Color.PINK));


        // Add all shapes to the panel
        for (PosterElement shape : shapesList) {
            JPanel shapePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;

                    // Scale the shape to fit the thumbnail
                    double scale = (double)(THUMBNAIL_SIZE - 20) / shape.getInitialWidth();
                    AffineTransform savedTransform = shape.getTransform();

                    // Create a temporary transform for displaying the thumbnail
                    AffineTransform thumbnailTransform = new AffineTransform();
                    thumbnailTransform.translate(10, 10);
                    thumbnailTransform.scale(scale, scale);
                    shape.getTransform().setTransform(thumbnailTransform);

                    // Draw the shape
                    shape.draw(g2d);

                    // Restore the original transform
                    shape.getTransform().setTransform(savedTransform);
                }
            };

            shapePanel.setPreferredSize(new Dimension(THUMBNAIL_SIZE, THUMBNAIL_SIZE));
            shapePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setupDragSource(shapePanel, shape);
            shapesPanel.add(shapePanel);
        }
    }

    private void setupDragSource(Component component, PosterElement element) {
        DragSource dragSource = DragSource.getDefaultDragSource();

        dragSource.createDefaultDragGestureRecognizer(
                component,
                DnDConstants.ACTION_COPY,
                e -> {
                    Transferable transferable = new ElementTransferable(element);
                    e.startDrag(null, transferable);
                }
        );
    }

    private void moveSelectedInScreenCoordinates(int dx, int dy) {
        if (selectedElement != null) {
            selectedElement.move(dx, dy);
            posterPanel.repaint();
        }
    }

    private void rotateSelected(double angle) {
        if (selectedElement != null) {
            selectedElement.rotateAroundCenter(angle);
            posterPanel.repaint();
        }
    }

    private void bringToFront() {
        if (selectedElement != null && posterElements.contains(selectedElement)) {
            posterElements.remove(selectedElement);
            posterElements.add(selectedElement);
            posterPanel.repaint();
        }
    }

    private void sendToBack() {
        if (selectedElement != null && posterElements.contains(selectedElement)) {
            posterElements.remove(selectedElement);
            posterElements.add(0, selectedElement);
            posterPanel.repaint();
        }
    }

    private void deleteSelected() {
        if (selectedElement != null) {
            posterElements.remove(selectedElement);
            selectedElement = null;
            posterPanel.repaint();
        }
    }


    // Poster Panel inner class
    private class PosterPanel extends JPanel implements DropTargetListener {
        private Point lastMousePoint;
        private int dragHandleIndex = -1;
        private boolean isDragging = false;

        public PosterPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(800, 600));

            // Setup drop target for elements
            new DropTarget(this, this);

            // Setup mouse event handling
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleMousePressed(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isDragging = false;
                    lastMousePoint = null;
                    dragHandleIndex = -1;
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    handleMouseDragged(e);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // Delete element with right mouse button
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        for (int i = posterElements.size() - 1; i >= 0; i--) {
                            PosterElement element = posterElements.get(i);
                            if (element.contains(e.getPoint())) {
                                posterElements.remove(i);
                                if (selectedElement == element) {
                                    selectedElement = null;
                                }
                                repaint();
                                break;
                            }
                        }
                    }
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        private void handleMousePressed(MouseEvent e) {
            Point p = e.getPoint();
            PosterElement prevSelected = selectedElement;
            selectedElement = null;
            dragHandleIndex = -1;
            lastMousePoint = p;

            // Iterate from end to select an element on top
            for (int i = posterElements.size() - 1; i >= 0; i--) {
                PosterElement element = posterElements.get(i);

                // Check if clicked on a handle
                if (prevSelected == element) {
                    int handleIndex = element.getHandleAt(p);
                    if (handleIndex != -1) {
                        selectedElement = element;
                        dragHandleIndex = handleIndex;
                        isDragging = true;
                        repaint();
                        return;
                    }
                }
                // Check if clicked in a middle of element
                if (element.contains(p)) {
                    selectedElement = element;
                    isDragging = true;
                    repaint();
                    return;
                }
            }

            repaint(); // Repaint to update selection state
        }

        private void handleMouseDragged(MouseEvent e) {
            if (selectedElement != null && lastMousePoint != null) {
                Point currentPoint = e.getPoint();
                int dx = currentPoint.x - lastMousePoint.x;
                int dy = currentPoint.y - lastMousePoint.y;

                if (dragHandleIndex != -1) {
                    // Handle dragging - resize or rotation
                    selectedElement.transformByHandle(dragHandleIndex, dx, dy);
                } else {
                    // Element dragging - translation in screen coordinates
                    AffineTransform dragTransform = new AffineTransform();
                    dragTransform.translate(dx, dy);
                    selectedElement.getTransform().preConcatenate(dragTransform);
                }

                lastMousePoint = currentPoint;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw all poster elements
            for (PosterElement element : posterElements) {
                element.draw(g2d);
            }

            // Draw handles for a selected element
            if (selectedElement != null) {
                selectedElement.drawHandles(g2d);
            }
        }

        // DropTargetListener implementation
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(ElementTransferable.ELEMENT_FLAVOR)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                dtde.rejectDrag();
            }
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(ElementTransferable.ELEMENT_FLAVOR)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                dtde.rejectDrag();
            }
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            try {
                if (dtde.isDataFlavorSupported(ElementTransferable.ELEMENT_FLAVOR)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);

                    Transferable transferable = dtde.getTransferable();
                    PosterElement element = (PosterElement) transferable.getTransferData(
                            ElementTransferable.ELEMENT_FLAVOR);

                    // Clone element
                    PosterElement newElement = element.clone();

                    // Set element position to drop location
                    Point dropPoint = dtde.getLocation();
                    newElement.getTransform().setToTranslation(
                            dropPoint.x - newElement.getInitialWidth() / 2,
                            dropPoint.y - newElement.getInitialHeight() / 2);

                    posterElements.add(newElement);
                    selectedElement = newElement;
                    repaint();

                    dtde.dropComplete(true);
                } else {
                    dtde.rejectDrop();
                }
            } catch (Exception e) {
                e.printStackTrace();
                dtde.rejectDrop();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PosterEditor());
    }
}
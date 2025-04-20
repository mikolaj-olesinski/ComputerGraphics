import javax.swing.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class PosterEditor extends JFrame {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int THUMBNAIL_SIZE = 80;
    private static final double ROTATION_ANGLE = Math.PI / 36; // 5 stopni
    private static final int TRANSLATION_STEP = 1; // 1 piksel

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
        // Panel główny podzielony na dwie części
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel lewy (podzielony na górny i dolny)
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 10));

        // Panel miniatur obrazów (lewy górny)
        thumbnailPanel = new JPanel();
        thumbnailPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        thumbnailPanel.setBorder(BorderFactory.createTitledBorder("Obrazy"));
        JScrollPane thumbnailScroll = new JScrollPane(thumbnailPanel);
        thumbnailScroll.setPreferredSize(new Dimension(300, 350));

        // Panel kształtów (lewy dolny)
        shapesPanel = new JPanel();
        shapesPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        shapesPanel.setBorder(BorderFactory.createTitledBorder("Kształty"));
        JScrollPane shapesScroll = new JScrollPane(shapesPanel);
        shapesScroll.setPreferredSize(new Dimension(300, 350));

        leftPanel.add(thumbnailScroll);
        leftPanel.add(shapesScroll);

        // Panel plakatu (prawy)
        posterPanel = new PosterPanel();
        JScrollPane posterScroll = new JScrollPane(posterPanel);
        posterScroll.setPreferredSize(new Dimension(850, 700));

        // Panel kontrolny (dolny)
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
        JButton bringToFrontBtn = new JButton("Na wierzch");
        JButton sendToBackBtn = new JButton("Na spód");
        JButton deleteBtn = new JButton("Usuń");

        // Fix: Use screen-oriented movement regardless of element rotation
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
    }

    // Fix: New method for screen-oriented movement
    private void moveSelectedInScreenCoordinates(int dx, int dy) {
        if (selectedElement != null) {
            // Apply screen-oriented translation
            AffineTransform moveTransform = new AffineTransform();
            moveTransform.translate(dx, dy);
            selectedElement.getTransform().preConcatenate(moveTransform);
            posterPanel.repaint();
        }
    }

    private void loadImageThumbnails() {
        // W praktyce wybierz katalog lub wczytaj z określonego miejsca
        imageDirectory = new File("images");
        if (!imageDirectory.exists() || !imageDirectory.isDirectory()) {
            JOptionPane.showMessageDialog(this,
                    "Katalog 'images' nie istnieje. Stwórz go i umieść tam obrazy.");
            return;
        }

        File[] imageFiles = imageDirectory.listFiles((dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                    name.endsWith(".png") || name.endsWith(".gif");
        });

        if (imageFiles == null || imageFiles.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Brak obrazów w katalogu 'images'.");
            return;
        }

        for (File file : imageFiles) {
            try {
                BufferedImage original = ImageIO.read(file);
                if (original != null) {
                    // Stwórz miniaturę
                    BufferedImage thumbnail = createThumbnail(original, THUMBNAIL_SIZE);
                    JLabel label = new JLabel(new ImageIcon(thumbnail));
                    label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    label.setToolTipText(file.getName());

                    // Dodaj obsługę Drag and Drop dla miniatury
                    setupDragSource(label, new ImageElement(original, file.getName()));
                    thumbnailPanel.add(label);
                }
            } catch (IOException e) {
                System.err.println("Nie można wczytać obrazu: " + file.getName());
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
        // Dodaj kwadrat
        JPanel squarePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.RED);
                g2d.fillRect(10, 10, THUMBNAIL_SIZE - 20, THUMBNAIL_SIZE - 20);
            }
        };
        squarePanel.setPreferredSize(new Dimension(THUMBNAIL_SIZE, THUMBNAIL_SIZE));
        squarePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setupDragSource(squarePanel, new ShapeElement(ShapeType.RECTANGLE, Color.RED));

        // Dodaj koło
        JPanel circlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.BLUE);
                g2d.fillOval(10, 10, THUMBNAIL_SIZE - 20, THUMBNAIL_SIZE - 20);
            }
        };
        circlePanel.setPreferredSize(new Dimension(THUMBNAIL_SIZE, THUMBNAIL_SIZE));
        circlePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setupDragSource(circlePanel, new ShapeElement(ShapeType.CIRCLE, Color.BLUE));

        shapesPanel.add(squarePanel);
        shapesPanel.add(circlePanel);
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

    private void moveSelected(int dx, int dy) {
        if (selectedElement != null) {
            selectedElement.getTransform().translate(dx, dy);
            posterPanel.repaint();
        }
    }

    // Fix: Improved rotation method that maintains position
    private void rotateSelected(double angle) {
        if (selectedElement != null) {
            Rectangle2D bounds = selectedElement.getBounds();
            double centerX = bounds.getCenterX();
            double centerY = bounds.getCenterY();

            // Create a new transform for rotation around center
            AffineTransform rotateTransform = new AffineTransform();
            rotateTransform.translate(centerX, centerY);
            rotateTransform.rotate(angle);
            rotateTransform.translate(-centerX, -centerY);

            // Apply this transform to the element's transform
            selectedElement.getTransform().preConcatenate(rotateTransform);
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

    // Panel plakatu
    private class PosterPanel extends JPanel implements DropTargetListener {
        private Point dragStart;
        private int dragHandleIndex = -1;

        public PosterPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(800, 600));

            // Obsługa upuszczania elementów na panel
            new DropTarget(this, this);

            // Obsługa zdarzeń myszy
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleMousePressed(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    dragStart = null;
                    dragHandleIndex = -1;
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    handleMouseDragged(e);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // Usuwanie elementu prawym przyciskiem myszy
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        for (PosterElement element : new ArrayList<>(posterElements)) {
                            if (element.contains(e.getPoint())) {
                                posterElements.remove(element);
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
            selectedElement = null;
            dragHandleIndex = -1;

            // Iteruj od końca, aby wybrać element na wierzchu
            for (int i = posterElements.size() - 1; i >= 0; i--) {
                PosterElement element = posterElements.get(i);

                // Sprawdź, czy kliknięto w uchwyt
                int handleIndex = element.getHandleAt(p);
                if (handleIndex != -1) {
                    selectedElement = element;
                    dragHandleIndex = handleIndex;
                    dragStart = p;
                    repaint();
                    return;
                }

                // Sprawdź, czy kliknięto w środek elementu
                if (element.contains(p)) {
                    selectedElement = element;
                    dragStart = p;
                    repaint();
                    return;
                }
            }
        }

        private void handleMouseDragged(MouseEvent e) {
            if (selectedElement != null && dragStart != null) {
                Point p = e.getPoint();
                int dx = p.x - dragStart.x;
                int dy = p.y - dragStart.y;

                if (dragHandleIndex != -1) {
                    // Przeciąganie uchwytów - zmiana rozmiaru lub rotacja
                    selectedElement.transformByHandle(dragHandleIndex, dx, dy);
                } else {
                    // Przeciąganie całego elementu - translacja
                    selectedElement.getTransform().translate(dx, dy);
                }

                dragStart = p;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Rysuj wszystkie elementy plakatu
            for (PosterElement element : posterElements) {
                element.draw(g2d);
            }

            // Rysuj uchwyty dla wybranego elementu
            if (selectedElement != null) {
                selectedElement.drawHandles(g2d);
            }
        }

        // Implementacja DropTargetListener
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

                    // Klonuj element, aby można go było wielokrotnie dodawać
                    PosterElement newElement = element.clone();

                    // Ustaw pozycję elementu na miejsce upuszczenia
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

    // Rodzaje kształtów
    private enum ShapeType {
        RECTANGLE, CIRCLE
    }

    // Klasa abstrakcyjna dla elementów plakatu
    private abstract static class PosterElement implements Cloneable {
        protected AffineTransform transform = new AffineTransform();
        protected static final int HANDLE_SIZE = 8;

        public abstract void draw(Graphics2D g2d);
        public abstract Rectangle2D getBounds();
        public abstract boolean contains(Point p);
        public abstract int getInitialWidth();
        public abstract int getInitialHeight();

        public AffineTransform getTransform() {
            return transform;
        }

        @Override
        public PosterElement clone() {
            try {
                PosterElement clone = (PosterElement) super.clone();
                clone.transform = new AffineTransform(transform);
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        // Rysuj uchwyty dla wybranego elementu
        public void drawHandles(Graphics2D g2d) {
            Rectangle2D bounds = getBounds();
            Point2D[] handles = getHandlePositions();

            g2d.setColor(Color.BLACK);
            for (Point2D handle : handles) {
                g2d.fillRect((int)(handle.getX() - HANDLE_SIZE/2),
                        (int)(handle.getY() - HANDLE_SIZE/2),
                        HANDLE_SIZE, HANDLE_SIZE);
            }
        }

        // Zwraca pozycje uchwytów
        public Point2D[] getHandlePositions() {
            Rectangle2D bounds = getBounds();
            double centerX = bounds.getCenterX();
            double centerY = bounds.getCenterY();

            return new Point2D[] {
                    new Point2D.Double(bounds.getMinX(), bounds.getMinY()), // Lewy górny
                    new Point2D.Double(centerX, bounds.getMinY()),          // Środek górny
                    new Point2D.Double(bounds.getMaxX(), bounds.getMinY()), // Prawy górny
                    new Point2D.Double(bounds.getMaxX(), centerY),          // Prawy środkowy
                    new Point2D.Double(bounds.getMaxX(), bounds.getMaxY()), // Prawy dolny
                    new Point2D.Double(centerX, bounds.getMaxY()),          // Środek dolny
                    new Point2D.Double(bounds.getMinX(), bounds.getMaxY()), // Lewy dolny
                    new Point2D.Double(bounds.getMinX(), centerY)           // Lewy środkowy
            };
        }

        // Sprawdza, czy punkt znajduje się w obrębie uchwytu i zwraca jego indeks
        public int getHandleAt(Point p) {
            Point2D[] handles = getHandlePositions();

            for (int i = 0; i < handles.length; i++) {
                if (new Rectangle2D.Double(
                        handles[i].getX() - HANDLE_SIZE/2,
                        handles[i].getY() - HANDLE_SIZE/2,
                        HANDLE_SIZE, HANDLE_SIZE).contains(p)) {
                    return i;
                }
            }

            return -1;
        }

        // Fix: Improved handle transformation method with proper resizing
        public void transformByHandle(int handleIndex, int dx, int dy) {
            Rectangle2D bounds = getBounds();
            double centerX = bounds.getCenterX();
            double centerY = bounds.getCenterY();
            Point2D[] handles = getHandlePositions();
            Point2D handle = handles[handleIndex];

            if (handleIndex % 2 == 0) {
                // Corner handles (0, 2, 4, 6) - perform scaling and rotation
                double oldAngle = Math.atan2(
                        handle.getY() - centerY,
                        handle.getX() - centerX
                );
                double newAngle = Math.atan2(
                        handle.getY() + dy - centerY,
                        handle.getX() + dx - centerX
                );
                double angleChange = newAngle - oldAngle;

                double oldDist = Point2D.distance(
                        centerX, centerY,
                        handle.getX(), handle.getY()
                );
                double newDist = Point2D.distance(
                        centerX, centerY,
                        handle.getX() + dx, handle.getY() + dy
                );
                double scaleFactor = newDist / oldDist;

                // Apply transformations relative to the center
                AffineTransform newTransform = new AffineTransform();
                newTransform.translate(centerX, centerY);
                newTransform.rotate(angleChange);
                newTransform.scale(scaleFactor, scaleFactor);
                newTransform.translate(-centerX, -centerY);

                transform.preConcatenate(newTransform);
            } else {
                // Edge handles (1, 3, 5, 7) - perform directional scaling
                // Determine which axis to scale along
                boolean isHorizontalEdge = (handleIndex == 1 || handleIndex == 5);
                boolean isVerticalEdge = (handleIndex == 3 || handleIndex == 7);

                // Calculate scaling factors
                double scaleX = 1.0;
                double scaleY = 1.0;

                if (isHorizontalEdge) {
                    // Get opposite edge y-coordinate
                    double oppositeY = (handleIndex == 1) ?
                            bounds.getMaxY() : bounds.getMinY();
                    double currentHeight = Math.abs(handle.getY() - oppositeY);
                    double newHeight = Math.abs(handle.getY() + dy - oppositeY);
                    scaleY = newHeight / currentHeight;
                }

                if (isVerticalEdge) {
                    // Get opposite edge x-coordinate
                    double oppositeX = (handleIndex == 3) ?
                            bounds.getMinX() : bounds.getMaxX();
                    double currentWidth = Math.abs(handle.getX() - oppositeX);
                    double newWidth = Math.abs(handle.getX() + dx - oppositeX);
                    scaleX = newWidth / currentWidth;
                }

                // Apply directional scaling
                AffineTransform scaleTransform = new AffineTransform();
                scaleTransform.translate(centerX, centerY);
                scaleTransform.scale(scaleX, scaleY);
                scaleTransform.translate(-centerX, -centerY);

                transform.preConcatenate(scaleTransform);
            }
        }
    }

    // Element obrazu
    private static class ImageElement extends PosterElement {
        private BufferedImage image;
        private String name;

        public ImageElement(BufferedImage image, String name) {
            this.image = image;
            this.name = name;
        }

        @Override
        public void draw(Graphics2D g2d) {
            AffineTransform oldTransform = g2d.getTransform();
            g2d.transform(transform);
            g2d.drawImage(image, 0, 0, null);
            g2d.setTransform(oldTransform);
        }

        @Override
        public Rectangle2D getBounds() {
            Rectangle2D rect = new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight());
            return transform.createTransformedShape(rect).getBounds2D();
        }

        @Override
        public boolean contains(Point p) {
            try {
                Point2D inversePt = new Point2D.Double();
                transform.inverseTransform(p, inversePt);
                return new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight())
                        .contains(inversePt);
            } catch (NoninvertibleTransformException e) {
                return false;
            }
        }

        @Override
        public int getInitialWidth() {
            return image.getWidth();
        }

        @Override
        public int getInitialHeight() {
            return image.getHeight();
        }
    }

    // Element kształtu
    private static class ShapeElement extends PosterElement {
        private ShapeType type;
        private Color color;
        private static final int DEFAULT_SIZE = 100;

        public ShapeElement(ShapeType type, Color color) {
            this.type = type;
            this.color = color;
        }

        @Override
        public void draw(Graphics2D g2d) {
            AffineTransform oldTransform = g2d.getTransform();
            g2d.transform(transform);

            g2d.setColor(color);
            if (type == ShapeType.RECTANGLE) {
                g2d.fillRect(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);
            } else {
                g2d.fillOval(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);
            }

            g2d.setTransform(oldTransform);
        }

        @Override
        public Rectangle2D getBounds() {
            Rectangle2D rect = new Rectangle2D.Double(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);
            return transform.createTransformedShape(rect).getBounds2D();
        }

        @Override
        public boolean contains(Point p) {
            try {
                Point2D inversePt = new Point2D.Double();
                transform.inverseTransform(p, inversePt);

                if (type == ShapeType.RECTANGLE) {
                    return new Rectangle2D.Double(0, 0, DEFAULT_SIZE, DEFAULT_SIZE)
                            .contains(inversePt);
                } else {
                    return new Ellipse2D.Double(0, 0, DEFAULT_SIZE, DEFAULT_SIZE)
                            .contains(inversePt);
                }
            } catch (NoninvertibleTransformException e) {
                return false;
            }
        }

        @Override
        public int getInitialWidth() {
            return DEFAULT_SIZE;
        }

        @Override
        public int getInitialHeight() {
            return DEFAULT_SIZE;
        }
    }

    // Transferable dla elementów plakatu
    private static class ElementTransferable implements Transferable {
        public static final DataFlavor ELEMENT_FLAVOR =
                new DataFlavor(PosterElement.class, "Poster Element");

        private PosterElement element;

        public ElementTransferable(PosterElement element) {
            this.element = element;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { ELEMENT_FLAVOR };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(ELEMENT_FLAVOR);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (flavor.equals(ELEMENT_FLAVOR)) {
                return element;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PosterEditor());
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class TriangleCanvas extends JFrame {
    private JPanel controlPanel;
    private DrawingPanel drawingPanel;
    private JButton clearButton;
    private JButton randomButton;
    private JRadioButton bufferImageRadio;
    private JRadioButton graphicsRadio;
    private ArrayList<Triangle2D> triangles = new ArrayList<>();
    private Color[] currentColors = new Color[3];

    public TriangleCanvas() {
        setTitle("Triangle Gouraud Shading Canvas");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Inicjalizacja losowych kolorów dla nowego trójkąta
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            currentColors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }

        // Panel sterowania
        createControlPanel();

        // Panel rysowania
        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        // Przyciski
        clearButton = new JButton("Wyczyść");
        randomButton = new JButton("Losowy trójkąt");

        // Radio buttons dla metod rysowania
        bufferImageRadio = new JRadioButton("BufferedImage");
        graphicsRadio = new JRadioButton("Graphics");
        ButtonGroup renderGroup = new ButtonGroup();
        renderGroup.add(bufferImageRadio);
        renderGroup.add(graphicsRadio);
        bufferImageRadio.setSelected(true);

        // Dodanie elementów do panelu
        controlPanel.add(clearButton);
        controlPanel.add(randomButton);
        controlPanel.add(new JLabel("Metoda rysowania: "));
        controlPanel.add(bufferImageRadio);
        controlPanel.add(graphicsRadio);

        // Dodanie listenerów
        clearButton.addActionListener(e -> {
            triangles.clear();
            drawingPanel.repaint();
        });

        randomButton.addActionListener(e -> {
            addRandomTriangle();
            drawingPanel.repaint();
        });

        bufferImageRadio.addActionListener(e -> drawingPanel.repaint());
        graphicsRadio.addActionListener(e -> drawingPanel.repaint());

        add(controlPanel, BorderLayout.NORTH);
    }

    private void addRandomTriangle() {
        Random random = new Random();
        int width = drawingPanel.getWidth();
        int height = drawingPanel.getHeight();

        int[] x = new int[3];
        int[] y = new int[3];
        Color[] colors = new Color[3];

        // Generowanie losowych wierzchołków
        for (int i = 0; i < 3; i++) {
            x[i] = random.nextInt(width);
            y[i] = random.nextInt(height);
            colors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }

        Triangle2D triangle = new Triangle2D(x, y, colors);
        triangles.add(triangle);
    }

    class DrawingPanel extends JPanel {
        private int[] vertices = new int[6]; // [x1, y1, x2, y2, x3, y3]
        private int vertexCount = 0;
        private BufferedImage buffer;

        public DrawingPanel() {
            setBackground(Color.WHITE);

            // Inicjalizacja bufora
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = buffer.createGraphics();
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.dispose();
                }
            });

            // Obsługa kliknięć myszy do dodawania wierzchołków
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (vertexCount < 6) {
                        vertices[vertexCount++] = e.getX();
                        vertices[vertexCount++] = e.getY();

                        // Jeśli mamy już trzy wierzchołki, tworzymy trójkąt
                        if (vertexCount == 6) {
                            int[] x = {vertices[0], vertices[2], vertices[4]};
                            int[] y = {vertices[1], vertices[3], vertices[5]};

                            Triangle2D triangle = new Triangle2D(x, y, currentColors);
                            triangles.add(triangle);

                            // Reset danych wierzchołków
                            vertexCount = 0;

                            // Nowe losowe kolory dla następnego trójkąta
                            Random random = new Random();
                            for (int i = 0; i < 3; i++) {
                                currentColors[i] = new Color(
                                        random.nextInt(256),
                                        random.nextInt(256),
                                        random.nextInt(256)
                                );
                            }

                            repaint();
                        } else {
                            repaint(); // Narysuj punkty
                        }
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int width = getWidth();
            int height = getHeight();

            if (buffer == null) {
                buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = buffer.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, width, height);
                g2d.dispose();
            }

            boolean useBufferImage = bufferImageRadio.isSelected();

            if (useBufferImage) {
                // Czyścimy bufor
                Graphics2D bufferG = buffer.createGraphics();
                bufferG.setColor(Color.WHITE);
                bufferG.fillRect(0, 0, width, height);

                // Rysujemy trójkąty na buforze
                for (Triangle2D triangle : triangles) {
                    triangle.gouraudShadeToImage(buffer);
                }

                bufferG.dispose();

                // Wyświetlamy bufor
                g.drawImage(buffer, 0, 0, this);
            } else {
                // Czyścimy panel
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, width, height);

                // Rysujemy trójkąty bezpośrednio na Graphics
                for (Triangle2D triangle : triangles) {
                    triangle.gouraudShadeToGraphics(g, width, height);
                }
            }

            // Rysowanie aktualnie dodawanych wierzchołków
            g.setColor(Color.BLACK);
            for (int i = 0; i < vertexCount; i += 2) {
                g.fillOval(vertices[i] - 3, vertices[i + 1] - 3, 6, 6);
                if (i > 0) {
                    g.drawLine(vertices[i - 2], vertices[i - 1], vertices[i], vertices[i + 1]);
                }
            }

            // Informacje pomocnicze
            g.setColor(Color.BLACK);
            g.drawString("Kliknij, aby dodać wierzchołek trójkąta (potrzeba 3 wierzchołków)", 10, height - 30);
            g.drawString("Liczba trójkątów: " + triangles.size(), 10, height - 10);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TriangleCanvas::new);
    }
}
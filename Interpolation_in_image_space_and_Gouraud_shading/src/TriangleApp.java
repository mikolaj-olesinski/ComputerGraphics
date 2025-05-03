import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Główna aplikacja demonstracyjna prezentująca cieniowanie Gourauda dla trójkątów
 */
public class TriangleApp extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private BufferedImage bufferedImage;
    private BufferedImage originalImage;
    private Triangle2D activeTriangle;
    private Triangle2D[] predefinedTriangles;
    private int triangleIndex = 0;
    private boolean useOptimized = true;
    private boolean drawToImage = true;
    private JLabel statusLabel;

    public TriangleApp() {
        super("Cieniowanie Gourauda - Demonstracja");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicjalizacja obrazów
        bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        originalImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        initializeBackgroundImage();
        copyBackground();

        // Utworzenie predefiniowanych trójkątów
        createPredefinedTriangles();
        activeTriangle = predefinedTriangles[triangleIndex];

        // Panel rysowania
        JPanel drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bufferedImage, 0, 0, this);

                // Rysowanie bezpośrednio na ekran, jeśli zaznaczono
                if (!drawToImage && activeTriangle != null) {
                    if (useOptimized) {
                        activeTriangle.drawGouraudToScreenOptimized(g);
                    } else {
                        activeTriangle.drawGouraudToScreen(g);
                    }
                }
            }
        };
        drawingPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // Obsługa myszy do rysowania własnych trójkątów
        MouseHandler mouseHandler = new MouseHandler();
        drawingPanel.addMouseListener(mouseHandler);
        drawingPanel.addMouseMotionListener(mouseHandler);

        // Przyciski kontrolne
        JButton nextTriangleButton = new JButton("Następny przykład");
        nextTriangleButton.addActionListener(e -> {
            triangleIndex = (triangleIndex + 1) % predefinedTriangles.length;
            activeTriangle = predefinedTriangles[triangleIndex];
            redrawTriangle();
            updateStatusLabel();
        });

        JButton prevTriangleButton = new JButton("Poprzedni przykład");
        prevTriangleButton.addActionListener(e -> {
            triangleIndex = (triangleIndex - 1 + predefinedTriangles.length) % predefinedTriangles.length;
            activeTriangle = predefinedTriangles[triangleIndex];
            redrawTriangle();
            updateStatusLabel();
        });

        JButton methodToggleButton = new JButton("Przełącz metodę");
        methodToggleButton.addActionListener(e -> {
            useOptimized = !useOptimized;
            redrawTriangle();
            updateStatusLabel();
        });

        JButton targetToggleButton = new JButton("Przełącz cel rysowania");
        targetToggleButton.addActionListener(e -> {
            drawToImage = !drawToImage;
            redrawTriangle();
            updateStatusLabel();
        });

        JButton clearButton = new JButton("Wyczyść");
        clearButton.addActionListener(e -> {
            copyBackground();
            repaint();
        });

        JButton drawCustomButton = new JButton("Narysuj własny trójkąt");
        drawCustomButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> new CustomTriangleDialog(this));
        });

        // Panel przycisków
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevTriangleButton);
        buttonPanel.add(nextTriangleButton);
        buttonPanel.add(methodToggleButton);
        buttonPanel.add(targetToggleButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(drawCustomButton);

        // Etykieta statusu
        statusLabel = new JLabel();
        updateStatusLabel();

        // Układ główny
        setLayout(new BorderLayout());
        add(drawingPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);

        // Pierwsze rysowanie
        redrawTriangle();
    }

    private void updateStatusLabel() {
        String method = useOptimized ? "zoptymalizowana" : "podstawowa";
        String target = drawToImage ? "do obrazu" : "na ekran";
        statusLabel.setText("Trójkąt #" + (triangleIndex + 1) + " | Metoda: " + method + " | Rysowanie: " + target);
    }

    private void initializeBackgroundImage() {
        // Tworzymy szachownicę jako tło
        Graphics2D g2d = originalImage.createGraphics();
        int tileSize = 20;
        boolean white = true;

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g2d.setColor(Color.GRAY);
        for (int y = 0; y < HEIGHT; y += tileSize) {
            white = !white;
            for (int x = 0; x < WIDTH; x += tileSize) {
                if (white) {
                    g2d.fillRect(x, y, tileSize, tileSize);
                }
                white = !white;
            }
        }

        g2d.dispose();
    }

    private void copyBackground() {
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();
    }

    private void redrawTriangle() {
        copyBackground();

        if (activeTriangle != null && drawToImage) {
            long startTime = System.nanoTime();

            if (useOptimized) {
                activeTriangle.drawGouraudToImageOptimized(bufferedImage);
            } else {
                activeTriangle.drawGouraudToImage(bufferedImage);
            }

            long endTime = System.nanoTime();
            System.out.println("Czas rysowania: " + (endTime - startTime) / 1_000_000.0 + " ms");
        }

        repaint();
    }

    private void createPredefinedTriangles() {
        predefinedTriangles = new Triangle2D[] {
                // Duży trójkąt na środku ekranu
                new Triangle2D(
                        WIDTH / 2, 100,
                        WIDTH - 100, HEIGHT - 100,
                        100, HEIGHT - 100,
                        Color.RED, Color.GREEN, Color.BLUE
                ),

                // Trójkąt z łagodnym gradientem kolorów
                new Triangle2D(
                        200, 150,
                        500, 200,
                        350, 450,
                        new Color(255, 200, 200), // Jasnoróżowy
                        new Color(200, 255, 200), // Jasnozielony
                        new Color(200, 200, 255)  // Jasnoniebieski
                ),

                // Trójkąt z kontrastowymi kolorami
                new Triangle2D(
                        300, 100,
                        500, 400,
                        100, 400,
                        Color.YELLOW, Color.MAGENTA, Color.CYAN
                ),

                // Wąski, wysoki trójkąt
                new Triangle2D(
                        WIDTH / 2, 50,
                        WIDTH / 2 - 50, HEIGHT - 100,
                        WIDTH / 2 + 50, HEIGHT - 100,
                        Color.RED, Color.GREEN, Color.BLUE
                ),

                // Szeroki, niski trójkąt
                new Triangle2D(
                        100, HEIGHT / 2,
                        WIDTH - 100, HEIGHT / 2 - 50,
                        WIDTH - 100, HEIGHT / 2 + 50,
                        Color.RED, Color.GREEN, Color.BLUE
                ),

                // Bardzo duży trójkąt wykraczający poza ekran
                new Triangle2D(
                        -100, -100,
                        WIDTH + 100, HEIGHT / 2,
                        WIDTH / 2, HEIGHT + 100,
                        Color.RED, Color.GREEN, Color.BLUE
                )
        };
    }

    /**
     * Obsługa zdarzeń myszy do rysowania własnych trójkątów
     */
    private class MouseHandler extends MouseAdapter {
        private int[] points = new int[6]; // x1,y1,x2,y2,x3,y3
        private int pointCount = 0;

        @Override
        public void mousePressed(MouseEvent e) {
            if (pointCount < 6) {
                points[pointCount++] = e.getX();
                points[pointCount++] = e.getY();

                // Rysujemy punkty na obrazie
                Graphics g = bufferedImage.getGraphics();
                g.setColor(Color.WHITE);
                g.fillOval(e.getX() - 3, e.getY() - 3, 6, 6);
                g.dispose();
                repaint();

                // Gdy mamy wszystkie trzy punkty, tworzymy trójkąt
                if (pointCount == 6) {
                    Color[] colors = new Color[] {
                            Color.RED, Color.GREEN, Color.BLUE
                    };

                    Triangle2D triangle = new Triangle2D(
                            points[0], points[1],
                            points[2], points[3],
                            points[4], points[5],
                            colors[0], colors[1], colors[2]
                    );

                    activeTriangle = triangle;
                    redrawTriangle();

                    // Reset do następnego trójkąta
                    pointCount = 0;
                }
            }
        }
    }

    /**
     * Dialog do definiowania własnego trójkąta
     */
    private class CustomTriangleDialog extends JDialog {
        private JTextField x1Field, y1Field, x2Field, y2Field, x3Field, y3Field;
        private JButton colorButton1, colorButton2, colorButton3;
        private Color color1 = Color.RED;
        private Color color2 = Color.GREEN;
        private Color color3 = Color.BLUE;

        public CustomTriangleDialog(JFrame parent) {
            super(parent, "Definicja własnego trójkąta", true);
            setSize(300, 350);
            setLocationRelativeTo(parent);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(9, 2, 5, 5));

            // Pierwszy wierzchołek
            panel.add(new JLabel("Wierzchołek 1 - X:"));
            x1Field = new JTextField("100");
            panel.add(x1Field);

            panel.add(new JLabel("Wierzchołek 1 - Y:"));
            y1Field = new JTextField("100");
            panel.add(y1Field);

            panel.add(new JLabel("Kolor 1:"));
            colorButton1 = new JButton("Wybierz");
            colorButton1.setBackground(color1);
            colorButton1.addActionListener(e -> {
                Color newColor = JColorChooser.showDialog(this, "Wybierz kolor", color1);
                if (newColor != null) {
                    color1 = newColor;
                    colorButton1.setBackground(color1);
                }
            });
            panel.add(colorButton1);

            // Drugi wierzchołek
            panel.add(new JLabel("Wierzchołek 2 - X:"));
            x2Field = new JTextField("400");
            panel.add(x2Field);

            panel.add(new JLabel("Wierzchołek 2 - Y:"));
            y2Field = new JTextField("100");
            panel.add(y2Field);

            panel.add(new JLabel("Kolor 2:"));
            colorButton2 = new JButton("Wybierz");
            colorButton2.setBackground(color2);
            colorButton2.addActionListener(e -> {
                Color newColor = JColorChooser.showDialog(this, "Wybierz kolor", color2);
                if (newColor != null) {
                    color2 = newColor;
                    colorButton2.setBackground(color2);
                }
            });
            panel.add(colorButton2);

            // Trzeci wierzchołek
            panel.add(new JLabel("Wierzchołek 3 - X:"));
            x3Field = new JTextField("250");
            panel.add(x3Field);

            panel.add(new JLabel("Wierzchołek 3 - Y:"));
            y3Field = new JTextField("400");
            panel.add(y3Field);

            panel.add(new JLabel("Kolor 3:"));
            colorButton3 = new JButton("Wybierz");
            colorButton3.setBackground(color3);
            colorButton3.addActionListener(e -> {
                Color newColor = JColorChooser.showDialog(this, "Wybierz kolor", color3);
                if (newColor != null) {
                    color3 = newColor;
                    colorButton3.setBackground(color3);
                }
            });
            panel.add(colorButton3);

            // Przyciski
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Anuluj");

            okButton.addActionListener(e -> {
                try {
                    int x1 = Integer.parseInt(x1Field.getText());
                    int y1 = Integer.parseInt(y1Field.getText());
                    int x2 = Integer.parseInt(x2Field.getText());
                    int y2 = Integer.parseInt(y2Field.getText());
                    int x3 = Integer.parseInt(x3Field.getText());
                    int y3 = Integer.parseInt(y3Field.getText());

                    activeTriangle = new Triangle2D(
                            x1, y1, x2, y2, x3, y3,
                            color1, color2, color3
                    );

                    redrawTriangle();
                    dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Nieprawidłowe dane. Wprowadź poprawne liczby całkowite.",
                            "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelButton.addActionListener(e -> dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            setLayout(new BorderLayout());
            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TriangleApp().setVisible(true);
        });
    }
}
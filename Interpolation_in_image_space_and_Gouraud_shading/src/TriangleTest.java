import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TriangleTest extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private BufferedImage bufferedImage;
    private List<Triangle2D> triangles;
    private int currentTestCase = 0;
    private boolean useOptimized = true;

    public TriangleTest() {
        super("Test cieniowania Gourauda");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        triangles = createTestCases();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bufferedImage, 0, 0, this);
            }
        };
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JButton nextButton = new JButton("Następny trójkąt");
        nextButton.addActionListener(e -> {
            currentTestCase = (currentTestCase + 1) % triangles.size();
            renderCurrentTestCase();
        });

        JButton prevButton = new JButton("Poprzedni trójkąt");
        prevButton.addActionListener(e -> {
            currentTestCase = (currentTestCase - 1 + triangles.size()) % triangles.size();
            renderCurrentTestCase();
        });

        JButton toggleMethodButton = new JButton("Przełącz metodę (aktualnie: " +
                (useOptimized ? "zoptymalizowana" : "podstawowa") + ")");
        toggleMethodButton.addActionListener(e -> {
            useOptimized = !useOptimized;
            toggleMethodButton.setText("Przełącz metodę (aktualnie: " +
                    (useOptimized ? "zoptymalizowana" : "podstawowa") + ")");
            renderCurrentTestCase();
        });

        JButton screenButton = new JButton("Rysuj na ekranie");
        screenButton.addActionListener(e -> {
            clearImage();
            Graphics g = panel.getGraphics();
            long startTime = System.nanoTime();
            if (useOptimized) {
                triangles.get(currentTestCase).drawGouraudToScreenOptimized(g);
            } else {
                triangles.get(currentTestCase).drawGouraudToScreen(g);
            }
            long endTime = System.nanoTime();
            System.out.println("Czas rysowania na ekranie: " + (endTime - startTime) / 1000000.0 + " ms");
        });

        JButton imageButton = new JButton("Rysuj do obrazu");
        imageButton.addActionListener(e -> {
            renderCurrentTestCase();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(toggleMethodButton);
        buttonPanel.add(screenButton);
        buttonPanel.add(imageButton);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        renderCurrentTestCase();
    }

    private void clearImage() {
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.dispose();
    }

    private void renderCurrentTestCase() {
        clearImage();
        System.out.println("Rysowanie trójkąta #" + (currentTestCase + 1) +
                " metodą " + (useOptimized ? "zoptymalizowaną" : "podstawową"));

        long startTime = System.nanoTime();
        if (useOptimized) {
            triangles.get(currentTestCase).drawGouraudToImageOptimized(bufferedImage);
        } else {
            triangles.get(currentTestCase).drawGouraudToImage(bufferedImage);
        }
        long endTime = System.nanoTime();
        System.out.println("Czas rysowania do obrazu: " + (endTime - startTime) / 1000000.0 + " ms");

        repaint();
    }

    private List<Triangle2D> createTestCases() {
        List<Triangle2D> testTriangles = new ArrayList<>();

        // Przypadek 1: Normalny trójkąt z różnymi kolorami w wierzchołkach
        testTriangles.add(new Triangle2D(
                200, 100,  // x1, y1
                400, 400,  // x2, y2
                100, 350,  // x3, y3
                Color.RED, Color.GREEN, Color.BLUE  // Kolory wierzchołków
        ));

        // Przypadek 2: Duży trójkąt wypełniający prawie cały ekran
        testTriangles.add(new Triangle2D(
                50, 50,      // x1, y1
                WIDTH - 50, 100,  // x2, y2
                WIDTH/2, HEIGHT - 50,  // x3, y3
                Color.YELLOW, Color.CYAN, Color.MAGENTA  // Kolory wierzchołków
        ));

        // Przypadek 3: Bardzo wąski, wysoki trójkąt
        testTriangles.add(new Triangle2D(
                WIDTH/2, 50,      // x1, y1
                WIDTH/2 - 10, HEIGHT - 50,  // x2, y2
                WIDTH/2 + 10, HEIGHT - 50,  // x3, y3
                Color.WHITE, Color.RED, Color.BLUE  // Kolory wierzchołków
        ));

        // Przypadek 4: Bardzo szeroki, niski trójkąt
        testTriangles.add(new Triangle2D(
                50, HEIGHT/2,      // x1, y1
                WIDTH - 50, HEIGHT/2 - 10,  // x2, y2
                WIDTH - 50, HEIGHT/2 + 10,  // x3, y3
                Color.GREEN, Color.BLUE, Color.YELLOW  // Kolory wierzchołków
        ));

        // Przypadek 5: Trójkąt z prawie identycznymi kolorami
        testTriangles.add(new Triangle2D(
                200, 150,  // x1, y1
                500, 200,  // x2, y2
                300, 400,  // x3, y3
                new Color(200, 100, 100),  // Prawie czerwony
                new Color(210, 110, 110),  // Też prawie czerwony
                new Color(220, 120, 120)   // Też prawie czerwony
        ));

        // Przypadek 6: Trójkąt z jednym kolorem (powinien być jednolity)
        Color singleColor = Color.ORANGE;
        testTriangles.add(new Triangle2D(
                300, 200,  // x1, y1
                500, 300,  // x2, y2
                200, 400,  // x3, y3
                singleColor, singleColor, singleColor  // Ten sam kolor we wszystkich wierzchołkach
        ));

        // Przypadek 7: Bardzo mały trójkąt
        testTriangles.add(new Triangle2D(
                WIDTH/2, HEIGHT/2,      // x1, y1
                WIDTH/2 + 5, HEIGHT/2,  // x2, y2
                WIDTH/2, HEIGHT/2 + 5,  // x3, y3
                Color.RED, Color.GREEN, Color.BLUE  // Kolory wierzchołków
        ));

        // Przypadek 8: Trójkąt w narożniku ekranu
        testTriangles.add(new Triangle2D(
                0, 0,      // x1, y1
                100, 0,    // x2, y2
                0, 100,    // x3, y3
                Color.RED, Color.GREEN, Color.BLUE  // Kolory wierzchołków
        ));

        // Przypadek 9: Trójkąt wychodzący poza ekran
        testTriangles.add(new Triangle2D(
                WIDTH - 50, HEIGHT - 50,  // x1, y1
                WIDTH + 100, HEIGHT - 50,  // x2, y2
                WIDTH - 50, HEIGHT + 100,  // x3, y3
                Color.MAGENTA, Color.CYAN, Color.YELLOW  // Kolory wierzchołków
        ));

        // Przypadek 10: Trójkąt z bardzo intensywnymi kolorami
        testTriangles.add(new Triangle2D(
                300, 150,  // x1, y1
                500, 350,  // x2, y2
                200, 400,  // x3, y3
                Color.RED, Color.GREEN, Color.BLUE  // Maksymalnie intensywne podstawowe kolory
        ));

        return testTriangles;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TriangleTest().setVisible(true);
        });
    }
}
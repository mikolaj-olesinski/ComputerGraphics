import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TriangleSpecialCasesTest extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private BufferedImage bufferedImage;
    private List<TestCase> testCases;
    private int currentTestCase = 0;
    private boolean useOptimized = true;

    public TriangleSpecialCasesTest() {
        super("Test przypadków specjalnych cieniowania Gourauda");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        testCases = createSpecialTestCases();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bufferedImage, 0, 0, this);
            }
        };
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JButton nextButton = new JButton("Następny przypadek");
        nextButton.addActionListener(e -> {
            currentTestCase = (currentTestCase + 1) % testCases.size();
            renderCurrentTestCase();
        });

        JButton prevButton = new JButton("Poprzedni przypadek");
        prevButton.addActionListener(e -> {
            currentTestCase = (currentTestCase - 1 + testCases.size()) % testCases.size();
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

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(toggleMethodButton);

        JLabel infoLabel = new JLabel();
        updateInfoLabel(infoLabel);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(infoLabel, BorderLayout.NORTH);

        renderCurrentTestCase();
    }

    private void updateInfoLabel(JLabel label) {
        if (currentTestCase < testCases.size()) {
            TestCase tc = testCases.get(currentTestCase);
            label.setText("Przypadek #" + (currentTestCase + 1) + ": " + tc.description);
        }
    }

    private void clearImage() {
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.dispose();
    }

    private void renderCurrentTestCase() {
        clearImage();
        TestCase tc = testCases.get(currentTestCase);

        System.out.println("Rysowanie przypadku #" + (currentTestCase + 1) + ": " + tc.description);
        System.out.println("Metodą " + (useOptimized ? "zoptymalizowaną" : "podstawową"));

        long startTime = System.nanoTime();
        if (useOptimized) {
            tc.triangle.drawGouraudToImageOptimized(bufferedImage);
        } else {
            tc.triangle.drawGouraudToImage(bufferedImage);
        }
        long endTime = System.nanoTime();
        System.out.println("Czas rysowania: " + (endTime - startTime) / 1000000.0 + " ms");

        // Aktualizacja etykiety informacyjnej
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JLabel) {
                updateInfoLabel((JLabel)comp);
                break;
            }
        }

        repaint();
    }

    private List<TestCase> createSpecialTestCases() {
        List<TestCase> cases = new ArrayList<>();

        // Przypadek 1: Zdegenerowany trójkąt (linia)
        cases.add(new TestCase(
                new Triangle2D(
                        200, 200,
                        400, 400,
                        300, 300, // Punkt na linii między (200,200) a (400,400)
                        Color.RED, Color.GREEN, Color.BLUE
                ),
                "Zdegenerowany trójkąt (wszystkie punkty na jednej linii)"
        ));

        // Przypadek 2: Trójkąt z dwoma tymi samymi wierzchołkami
        cases.add(new TestCase(
                new Triangle2D(
                        300, 200,
                        300, 200, // Ten sam punkt co pierwszy
                        500, 400,
                        Color.RED, Color.GREEN, Color.BLUE
                ),
                "Trójkąt z dwoma identycznymi wierzchołkami"
        ));

        // Przypadek 3: Trójkąt o zerowej powierzchni (wszystkie wierzchołki w tym samym miejscu)
        cases.add(new TestCase(
                new Triangle2D(
                        350, 300,
                        350, 300,
                        350, 300,
                        Color.RED, Color.GREEN, Color.BLUE
                ),
                "Trójkąt o zerowej powierzchni (wszystkie wierzchołki w tym samym miejscu)"
        ));

        // Przypadek 4: Trójkąt obejmujący prawie cały obraz
        cases.add(new TestCase(
                new Triangle2D(
                        0, 0,
                        WIDTH, 0,
                        WIDTH/2, HEIGHT,
                        Color.RED, Color.GREEN, Color.BLUE
                ),
                "Trójkąt obejmujący prawie cały obraz"
        ));

        // Przypadek 5: Trójkąt całkowicie poza obszarem obrazu (ujemne współrzędne)
        cases.add(new TestCase(
                new Triangle2D(
                        -100, -100,
                        -50, -50,
                        -200, -50,
                        Color.RED, Color.GREEN, Color.BLUE
                ),
                "Trójkąt całkowicie poza obszarem obrazu (ujemne współrzędne)"
        ));

        // Przypadek 6: Trójkąt całkowicie poza obszarem obrazu (za duże współrzędne)
        cases.add(new TestCase(
                new Triangle2D(
                        WIDTH + 10, HEIGHT + 10,
                        WIDTH + 100, HEIGHT + 10,
                        WIDTH + 50, HEIGHT + 100,
                        Color.RED, Color.GREEN, Color.BLUE
                ),
                "Trójkąt całkowicie poza obszarem obrazu (za duże współrzędne)"
        ));

        // Przypadek 7: Trójkąt częściowo poza obszarem obrazu
        cases.add(new TestCase(
                new Triangle2D(
                        -100, HEIGHT/2,
                        WIDTH/2, -100,
                        WIDTH/2, HEIGHT/2,
                        Color.RED, Color.GREEN, Color.BLUE
                ),
                "Trójkąt częściowo poza obszarem obrazu"
        ));

        // Przypadek 8: Bardzo duży trójkąt z odległymi wierzchołkami
        cases.add(new TestCase(
                new Triangle2D(
                        -1000, -1000,
                        WIDTH + 1000, -1000,
                        WIDTH/2, HEIGHT + 1000,
                        Color.RED, Color.GREEN, Color.BLUE
                ),
                "Bardzo duży trójkąt z odległymi wierzchołkami"
        ));

        // Przypadek 9: Trójkąt z bardzo intensywnymi kolorami
        cases.add(new TestCase(
                new Triangle2D(
                        200, 100,
                        400, 300,
                        100, 400,
                        new Color(255, 0, 0), // Czysty czerwony
                        new Color(0, 255, 0), // Czysty zielony
                        new Color(0, 0, 255)  // Czysty niebieski
                ),
                "Trójkąt z bardzo intensywnymi kolorami"
        ));

        // Przypadek 10: Prawie czarny trójkąt (słabe różnice kolorów)
        cases.add(new TestCase(
                new Triangle2D(
                        200, 100,
                        400, 300,
                        100, 400,
                        new Color(10, 10, 10),    // Prawie czarny
                        new Color(20, 20, 20),    // Też prawie czarny
                        new Color(30, 30, 30)     // Też prawie czarny
                ),
                "Prawie czarny trójkąt (słabe różnice kolorów)"
        ));

        return cases;
    }

    // Klasa pomocnicza przechowująca trójkąt i jego opis
    private static class TestCase {
        Triangle2D triangle;
        String description;

        public TestCase(Triangle2D triangle, String description) {
            this.triangle = triangle;
            this.description = description;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TriangleSpecialCasesTest().setVisible(true);
        });
    }
}
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.Random;
import java.text.DecimalFormat;

public class TrianglePerformanceTest extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int TRIANGLE_COUNT = 1000;
    private static final int TEST_DURATION_MS = 5000;

    private BufferedImage bufferedImage;
    private JPanel drawPanel;
    private JLabel statusLabel;

    private Triangle2D[] triangles;
    private Random random = new Random();
    private boolean useBufferedImage = true;
    private int frameCount = 0;
    private long pixelCount = 0;
    private long startTime;
    private boolean testRunning = false;

    public TrianglePerformanceTest() {
        setTitle("Triangle Rendering Performance Test");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        // Initialize triangles array
        triangles = new Triangle2D[TRIANGLE_COUNT];
        generateRandomTriangles();

        // Create UI components
        JPanel contentPanel = new JPanel(new BorderLayout());

        drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (useBufferedImage) {
                    // Clear the buffered image
                    Graphics imgG = bufferedImage.getGraphics();
                    imgG.setColor(Color.BLACK);
                    imgG.fillRect(0, 0, WIDTH, HEIGHT);

                    // Draw triangles to buffered image
                    for (Triangle2D triangle : triangles) {
                        triangle.gouraudShadeToImage(bufferedImage);
                    }
                    imgG.dispose();

                    // Draw the buffered image to screen
                    g.drawImage(bufferedImage, 0, 0, this);
                } else {
                    // Clear the screen
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, WIDTH, HEIGHT);

                    // Draw triangles directly to screen
                    for (Triangle2D triangle : triangles) {
                        triangle.gouraudShadeToGraphics(g, WIDTH, HEIGHT);
                    }
                }

                if (testRunning) {
                    frameCount++;
                    updatePixelCount();
                }
            }
        };

        drawPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        contentPanel.add(drawPanel, BorderLayout.CENTER);

        // Status panel at the bottom
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Press 'B' for BufferedImage mode, 'G' for direct Graphics mode, 'R' to run test");
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        contentPanel.add(statusPanel, BorderLayout.SOUTH);

        add(contentPanel);

        // Add keyboard listener for switching modes
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case 'b':
                    case 'B':
                        useBufferedImage = true;
                        statusLabel.setText("Mode: BufferedImage. Press 'R' to run test.");
                        break;
                    case 'g':
                    case 'G':
                        useBufferedImage = false;
                        statusLabel.setText("Mode: Direct Graphics. Press 'R' to run test.");
                        break;
                    case 'r':
                    case 'R':
                        if (!testRunning) {
                            runPerformanceTest();
                        }
                        break;
                    case 'n':
                    case 'N':
                        generateRandomTriangles();
                        repaint();
                        break;
                }
            }
        });

        // Setup animation timer
        Timer timer = new Timer(16, e -> {
            if (testRunning) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= TEST_DURATION_MS) {
                    endTest();
                }
            }
            repaint();
        });
        timer.start();

        setFocusable(true);
        setVisible(true);
    }

    private void generateRandomTriangles() {
        for (int i = 0; i < TRIANGLE_COUNT; i++) {
            triangles[i] = createRandomTriangle();
        }
        pixelCount = 0;
    }

    private Triangle2D createRandomTriangle() {
        int[] x = new int[3];
        int[] y = new int[3];
        Color[] colors = new Color[3];

        // Generate coordinates
        for (int i = 0; i < 3; i++) {
            x[i] = random.nextInt(WIDTH);
            y[i] = random.nextInt(HEIGHT);
            colors[i] = new Color(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
            );
        }

        return new Triangle2D(x, y, colors);
    }

    private void updatePixelCount() {
        for (Triangle2D triangle : triangles) {
            // Estimate pixels by calculating triangle area
            int[] x = triangle.getX();
            int[] y = triangle.getY();

            // Calculate area using the Shoelace formula
            int area = Math.abs((x[0] * (y[1] - y[2]) + x[1] * (y[2] - y[0]) + x[2] * (y[0] - y[1])) / 2);
            pixelCount += area;
        }
    }

    private void runPerformanceTest() {
        testRunning = true;
        frameCount = 0;
        pixelCount = 0;
        startTime = System.currentTimeMillis();
        statusLabel.setText("Running test... Please wait " + (TEST_DURATION_MS / 1000) + " seconds.");
    }

    private void endTest() {
        testRunning = false;
        long elapsedTime = System.currentTimeMillis() - startTime;
        double trianglesPerSecond = 1000.0 * frameCount * TRIANGLE_COUNT / elapsedTime;
        double pixelsPerSecond = 1000.0 * pixelCount / elapsedTime;

        DecimalFormat df = new DecimalFormat("#,###.##");
        String mode = useBufferedImage ? "BufferedImage" : "Direct Graphics";

        String result = String.format("Mode: %s | Performance: %s triangles/sec | %s pixels/sec | Avg triangle size: %s pixels",
                mode,
                df.format(trianglesPerSecond),
                df.format(pixelsPerSecond),
                df.format((double)pixelCount / (frameCount * TRIANGLE_COUNT)));

        statusLabel.setText(result);
        System.out.println(result);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrianglePerformanceTest());
    }
}
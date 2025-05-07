import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.Random;
import java.text.DecimalFormat;

public class TrianglePerformanceTest {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int NUM_TRIANGLES = 1000;
    private static final int TEST_DURATION_MS = 10000; // 10 seconds test duration
    private static final int NUM_TESTS = 2; // Run 2 tests: buffered image and direct drawing

    private static BufferedImage bufferedImage;
    private static JFrame frame;
    private static JPanel renderPanel;
    private static Triangle2D[] triangles;
    private static Random random = new Random();
    private static DecimalFormat df = new DecimalFormat("#,###.##");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setupUI();

            // Start tests after UI is initialized
            new Thread(() -> {
                try {
                    // Let the UI fully initialize
                    Thread.sleep(1000);

                    // Run both tests and print results
                    runTests();

                    // Print performance comparison with modern GPUs
                    printGPUComparison();

                    // Exit when done
                    System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private static void setupUI() {
        // Create the UI components
        frame = new JFrame("Triangle Performance Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);

        bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        renderPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bufferedImage, 0, 0, null);
            }
        };

        renderPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.add(renderPanel);

        frame.pack();
        frame.setVisible(true);
    }

    private static void runTests() {
        System.out.println("\n=== STARTING PERFORMANCE TESTS ===");
        System.out.println("Testing each mode for " + (TEST_DURATION_MS / 1000) + " seconds\n");

        // First test: Buffered Image
        TestResult bufferedResult = runTest(true);
        System.out.println("\n--- BUFFERED IMAGE RESULTS ---");
        printResults(bufferedResult);

        // Wait a bit between tests
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Second test: Direct Drawing
        TestResult directResult = runTest(false);
        System.out.println("\n--- DIRECT DRAWING RESULTS ---");
        printResults(directResult);

        // Compare results
        System.out.println("\n--- COMPARISON ---");
        double triangleSpeedRatio = bufferedResult.trianglesPerSecond / directResult.trianglesPerSecond;
        double pixelSpeedRatio = bufferedResult.pixelsPerSecond / directResult.pixelsPerSecond;

        System.out.println("Buffered Image is " + df.format(triangleSpeedRatio) + "x " +
                (triangleSpeedRatio > 1 ? "faster" : "slower") +
                " than Direct Drawing for triangle rendering");

        System.out.println("Buffered Image is " + df.format(pixelSpeedRatio) + "x " +
                (pixelSpeedRatio > 1 ? "faster" : "slower") +
                " than Direct Drawing for pixel filling");
    }

    private static TestResult runTest(boolean useBufferedImage) {
        String testType = useBufferedImage ? "BufferedImage" : "Direct Drawing";
        System.out.println("Starting " + testType + " test...");

        // Update the status on screen
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2d.drawString("Running test: " + testType, 50, HEIGHT / 2 - 40);
        g2d.drawString("Please wait...", 50, HEIGHT / 2);
        g2d.dispose();
        renderPanel.repaint();

        long totalTriangles = 0;
        long totalPixels = 0;
        int frameCount = 0;

        long startTime = System.currentTimeMillis();
        long endTime = startTime + TEST_DURATION_MS;

        while (System.currentTimeMillis() < endTime) {
            // Generate new random triangles for each frame
            generateRandomTriangles();

            long pixelsDrawn;
            if (useBufferedImage) {
                // Clear the buffered image
                g2d = bufferedImage.createGraphics();
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, WIDTH, HEIGHT);
                g2d.dispose();

                // Draw triangles to buffered image
                pixelsDrawn = renderToBufferedImage();
            } else {
                // Create a new graphics object for direct drawing
                BufferedImage tempImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
                g2d = tempImage.createGraphics();
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, WIDTH, HEIGHT);

                // Draw triangles directly to graphics
                pixelsDrawn = renderDirectly(g2d);
                g2d.dispose();

                // Copy the result to the display image
                g2d = bufferedImage.createGraphics();
                g2d.drawImage(tempImage, 0, 0, null);
                g2d.dispose();
            }

            // Update statistics
            totalTriangles += triangles.length;
            totalPixels += pixelsDrawn;
            frameCount++;

            // Update the screen occasionally
            if (frameCount % 10 == 0) {
                // Calculate current performance
                long currentElapsed = System.currentTimeMillis() - startTime;
                double currentTPS = (totalTriangles * 1000.0) / currentElapsed;
                double currentPPS = (totalPixels * 1000.0) / currentElapsed;

                // Display current stats on the screen
                g2d = bufferedImage.createGraphics();
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, WIDTH, 100); // Clear status area
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
                g2d.drawString("Testing: " + testType, 10, 20);
                g2d.drawString("Triangles: " + df.format(currentTPS) + " triangles/s", 10, 40);
                g2d.drawString("Pixels: " + df.format(currentPPS) + " pixels/s", 10, 60);
                g2d.drawString("Time remaining: " + ((endTime - System.currentTimeMillis()) / 1000) + "s", 10, 80);
                g2d.dispose();
                renderPanel.repaint();

                // Print progress to console
                System.out.print(".");
                if (frameCount % 100 == 0) System.out.println();
            }
        }
        System.out.println(); // End progress line

        // Calculate final results
        long totalElapsed = System.currentTimeMillis() - startTime;
        double trianglesPerSecond = (totalTriangles * 1000.0) / totalElapsed;
        double pixelsPerSecond = (totalPixels * 1000.0) / totalElapsed;
        double avgPixelsPerTriangle = (double) totalPixels / totalTriangles;
        double framesPerSecond = (frameCount * 1000.0) / totalElapsed;

        // Display final results
        g2d = bufferedImage.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2d.drawString(testType + " - Final Results:", 10, 30);
        g2d.drawString("Triangles: " + df.format(trianglesPerSecond) + " triangles/s", 10, 60);
        g2d.drawString("Pixels: " + df.format(pixelsPerSecond) + " pixels/s", 10, 90);
        g2d.drawString("Avg pixels per triangle: " + df.format(avgPixelsPerTriangle), 10, 120);
        g2d.drawString("Frames per second: " + df.format(framesPerSecond), 10, 150);
        g2d.dispose();
        renderPanel.repaint();

        return new TestResult(trianglesPerSecond, pixelsPerSecond, avgPixelsPerTriangle, framesPerSecond);
    }

    private static void generateRandomTriangles() {
        triangles = new Triangle2D[NUM_TRIANGLES];

        for (int i = 0; i < NUM_TRIANGLES; i++) {
            // Generate random triangle with size between 10 and 100 pixels
            int centerX = random.nextInt(WIDTH);
            int centerY = random.nextInt(HEIGHT);
            int size = 10 + random.nextInt(90);

            // Generate the three vertices around the center point
            int[] x = new int[3];
            int[] y = new int[3];

            for (int j = 0; j < 3; j++) {
                double angle = 2 * Math.PI * j / 3 + random.nextDouble() * Math.PI / 4;
                x[j] = centerX + (int)(size * Math.cos(angle));
                y[j] = centerY + (int)(size * Math.sin(angle));
            }

            // Generate random colors for vertices
            Color[] colors = new Color[3];
            for (int j = 0; j < 3; j++) {
                colors[j] = new Color(
                        random.nextInt(256),
                        random.nextInt(256),
                        random.nextInt(256)
                );
            }

            triangles[i] = new Triangle2D(x, y, colors);
        }
    }

    private static long renderToBufferedImage() {
        long totalPixels = 0;

        for (Triangle2D triangle : triangles) {
            triangle.resetPixelCount();
            totalPixels += triangle.gouraudShadeToImage(bufferedImage);
        }

        return totalPixels;
    }

    private static long renderDirectly(Graphics g) {
        long totalPixels = 0;

        for (Triangle2D triangle : triangles) {
            triangle.resetPixelCount();
            totalPixels += triangle.gouraudShadeToGraphics(g, WIDTH, HEIGHT);
        }

        return totalPixels;
    }

    private static void printResults(TestResult result) {
        System.out.println("Triangle rendering rate: " + df.format(result.trianglesPerSecond) + " triangles/s");
        System.out.println("Pixel fill rate: " + df.format(result.pixelsPerSecond) + " pixels/s");
        System.out.println("Average pixels per triangle: " + df.format(result.avgPixelsPerTriangle));
        System.out.println("Frames per second: " + df.format(result.framesPerSecond));
    }

    private static void printGPUComparison() {
        System.out.println("\n=== PERFORMANCE COMPARISON WITH MODERN GRAPHICS CARDS ===");
        System.out.println("Modern graphics cards can render approximately:");
        System.out.println("- Entry-level GPU: 100-300 million triangles/second");
        System.out.println("- Mid-range GPU: 300-800 million triangles/second");
        System.out.println("- High-end GPU: 800+ million triangles/second");
        System.out.println();
        System.out.println("In terms of pixel fill rate:");
        System.out.println("- Entry-level GPU: 5-15 billion pixels/second");
        System.out.println("- Mid-range GPU: 15-50 billion pixels/second");
        System.out.println("- High-end GPU: 50-120+ billion pixels/second");
        System.out.println();
        System.out.println("Note: Modern benchmarks typically use triangles with");
        System.out.println("an average of 10-30 pixels per triangle for real-time 3D rendering tests.");
    }

    private static class TestResult {
        final double trianglesPerSecond;
        final double pixelsPerSecond;
        final double avgPixelsPerTriangle;
        final double framesPerSecond;

        TestResult(double trianglesPerSecond, double pixelsPerSecond,
                   double avgPixelsPerTriangle, double framesPerSecond) {
            this.trianglesPerSecond = trianglesPerSecond;
            this.pixelsPerSecond = pixelsPerSecond;
            this.avgPixelsPerTriangle = avgPixelsPerTriangle;
            this.framesPerSecond = framesPerSecond;
        }
    }
}
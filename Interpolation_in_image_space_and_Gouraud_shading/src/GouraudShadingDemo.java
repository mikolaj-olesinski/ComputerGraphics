import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GouraudShadingDemo extends JPanel {
    private static final long serialVersionUID = 1L;

    // Canvas dimensions
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;

    // Individual triangle images
    private BufferedImage[] triangleImages;

    // Labels for each case
    private String[] labels = {
            "Case 1: Standard Triangle",
            "Case 2: Flat-Top Triangle",
            "Case 3: Flat-Bottom Triangle",
            "Case 4: Horizontal Line Triangle",
            "Case 5: Inverted Triangle",
            "Case 6: Color Gradient Showcase"
    };

    public GouraudShadingDemo() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // Create and draw all triangle cases
        createTriangleCases();

        // Save the main canvas image
        saveFullCanvas();
    }

    private void createTriangleCases() {
        triangleImages = new BufferedImage[6]; // 6 different cases

        // Create each triangle image
        for (int i = 0; i < triangleImages.length; i++) {
            triangleImages[i] = createTriangleImage(i);
        }
    }

    private BufferedImage createTriangleImage(int caseNumber) {
        BufferedImage image = new BufferedImage(WIDTH/2, HEIGHT/3, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();

        Triangle2D triangle = null;

        switch (caseNumber) {
            case 0: // Standard Triangle (top-to-bottom)
                triangle = new Triangle2D(
                        new int[] {image.getWidth()/2, image.getWidth()/4, 3*image.getWidth()/4},
                        new int[] {30, image.getHeight()-30, image.getHeight()-30},
                        new Color[] {Color.BLUE, Color.RED, Color.GREEN}
                );
                break;

            case 1: // Flat-Top Triangle
                triangle = new Triangle2D(
                        new int[] {image.getWidth()/4, 3*image.getWidth()/4, image.getWidth()/2},
                        new int[] {30, 30, image.getHeight()-30},
                        new Color[] {Color.MAGENTA, Color.YELLOW, Color.CYAN}
                );
                break;

            case 2: // Flat-Bottom Triangle
                triangle = new Triangle2D(
                        new int[] {image.getWidth()/2, image.getWidth()/4, 3*image.getWidth()/4},
                        new int[] {30, image.getHeight()-30, image.getHeight()-30},
                        new Color[] {Color.ORANGE, Color.PINK, new Color(100, 200, 100)}
                );
                break;

            case 3: // Horizontal Line Triangle (Degenerated)
                triangle = new Triangle2D(
                        new int[] {image.getWidth()/4, image.getWidth()/2, 3*image.getWidth()/4},
                        new int[] {image.getHeight()/2, image.getHeight()/2, image.getHeight()/2},
                        new Color[] {Color.RED, Color.GREEN, Color.BLUE}
                );
                break;

            case 4: // Inverted Triangle
                triangle = new Triangle2D(
                        new int[] {image.getWidth()/2, image.getWidth()/4, 3*image.getWidth()/4},
                        new int[] {image.getHeight()-30, 30, 30},
                        new Color[] {new Color(50, 0, 100), new Color(200, 100, 0), new Color(0, 150, 150)}
                );
                break;

            case 5: // Color Gradient Showcase
                triangle = new Triangle2D(
                        new int[] {image.getWidth()/2, 30, image.getWidth()-30},
                        new int[] {30, image.getHeight()-30, image.getHeight()/2},
                        new Color[] {
                                new Color(255, 0, 0),    // Pure Red
                                new Color(0, 255, 0),    // Pure Green
                                new Color(0, 0, 255)     // Pure Blue
                        }
                );
                break;
        }

        if (triangle != null) {
            triangle.gouraudShadeToImage(image);

            // Draw border around the triangle to show its outline
            drawTriangleOutline(image, triangle);
        }

        return image;
    }

    private void drawTriangleOutline(BufferedImage image, Triangle2D triangle) {
        // Need to access the x, y coordinates from Triangle2D
        // Since these are private, we'll use reflection to access them
        try {
            java.lang.reflect.Field xField = Triangle2D.class.getDeclaredField("x");
            java.lang.reflect.Field yField = Triangle2D.class.getDeclaredField("y");

            xField.setAccessible(true);
            yField.setAccessible(true);

            int[] x = (int[]) xField.get(triangle);
            int[] y = (int[]) yField.get(triangle);

            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.BLACK);
            g2d.drawLine(x[0], y[0], x[1], y[1]);
            g2d.drawLine(x[1], y[1], x[2], y[2]);
            g2d.drawLine(x[2], y[2], x[0], y[0]);
            g2d.dispose();
        } catch (Exception e) {
            System.err.println("Could not draw triangle outline: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw white background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw each triangle image in a grid layout
        for (int i = 0; i < triangleImages.length; i++) {
            int row = i / 2;
            int col = i % 2;

            int x = col * (WIDTH/2);
            int y = row * (HEIGHT/3);

            g.drawImage(triangleImages[i], x, y, null);

            // Draw label
            g.setColor(Color.BLACK);
            g.drawString(labels[i], x + 10, y + 20);
        }
    }

    private void saveFullCanvas() {
        BufferedImage fullCanvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fullCanvas.createGraphics();

        // Draw white background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw each triangle image in a grid layout
        for (int i = 0; i < triangleImages.length; i++) {
            int row = i / 2;
            int col = i % 2;

            int x = col * (WIDTH/2);
            int y = row * (HEIGHT/3);

            g.drawImage(triangleImages[i], x, y, null);

            // Draw label
            g.setColor(Color.BLACK);
            g.drawString(labels[i], x + 10, y + 20);
        }

        g.dispose();

        // Save the complete image
        try {
            ImageIO.write(fullCanvas, "PNG", new File("triangle_shading_cases.png"));
            System.out.println("Saved all triangle cases to triangle_shading_cases.png");

            // Also save individual triangle images
            for (int i = 0; i < triangleImages.length; i++) {
                ImageIO.write(triangleImages[i], "PNG", new File("triangle_case_" + (i+1) + ".png"));
            }
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }

    // Save each triangle image separately
    private void saveIndividualTriangleImages() {
        for (int i = 0; i < triangleImages.length; i++) {
            try {
                ImageIO.write(triangleImages[i], "PNG", new File("triangle_case_" + (i+1) + ".png"));
                System.out.println("Saved triangle case " + (i+1) + " to triangle_case_" + (i+1) + ".png");
            } catch (IOException e) {
                System.err.println("Error saving image for case " + (i+1) + ": " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gouraud Shading Triangle Demo");
            GouraudShadingDemo panel = new GouraudShadingDemo();
            frame.add(panel);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
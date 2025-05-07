import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GouraudShadingDemo {
    // Image dimensions
    private static final int WIDTH = 500;
    private static final int HEIGHT = 400;

    // Individual triangle images
    private BufferedImage[] triangleImages;

    // Labels for each case
    private String[] labels = {
            "Case 1: Standard Triangle",
            "Case 2: Upside-Down Triangle",
            "Case 3: Non-Parallel Triangle 1",
            "Case 4: Non-Parallel Triangle 2",
            "Case 5: Triangle with 2 Same Colors",
            "Case 6: Triangle with 3 Same Colors",
            "Case 7: Long Thin Triangle",
            "Case 8: Right-Angled Triangle",
            "Case 9: Obtuse Triangle"
    };

    public GouraudShadingDemo() {
        // Create output directory if it doesn't exist
        createOutputDirectory();

        // Create and save all triangle cases
        createTriangleCases();
    }

    private void createOutputDirectory() {
        File outputDir = new File("triangle_images");
        if (!outputDir.exists()) {
            if (outputDir.mkdir()) {
                System.out.println("Created output directory: triangle_images");
            } else {
                System.err.println("Failed to create output directory!");
            }
        }
    }

    private void createTriangleCases() {
        triangleImages = new BufferedImage[9]; // 9 different cases (6 original + 3 new)

        // Create each triangle image
        for (int i = 0; i < triangleImages.length; i++) {
            triangleImages[i] = createTriangleImage(i);
            saveTriangleImage(triangleImages[i], "triangle_images/triangle_case_" + (i+1) + ".png");
        }
    }

    private BufferedImage createTriangleImage(int caseNumber) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        // Draw the label
        g.setColor(Color.BLACK);
        g.drawString(labels[caseNumber], 10, 20);
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

            case 1: // Upside-Down Triangle
                triangle = new Triangle2D(
                        new int[] {image.getWidth()/2, image.getWidth()/4, 3*image.getWidth()/4},
                        new int[] {image.getHeight()-30, 30, 30},
                        new Color[] {Color.MAGENTA, Color.YELLOW, Color.CYAN}
                );
                break;

            case 2: // Non-Parallel Triangle 1 (no side parallel to screen)
                triangle = new Triangle2D(
                        new int[] {image.getWidth()/3, image.getWidth()/4, 3*image.getWidth()/5},
                        new int[] {image.getHeight()/4, 3*image.getHeight()/4, 2*image.getHeight()/3},
                        new Color[] {Color.ORANGE, Color.PINK, new Color(100, 200, 100)}
                );
                break;

            case 3: // Non-Parallel Triangle 2 (another orientation with no parallel sides)
                triangle = new Triangle2D(
                        new int[] {image.getWidth()/4, 2*image.getWidth()/3, 3*image.getWidth()/4},
                        new int[] {image.getHeight()/3, image.getHeight()/4, 3*image.getHeight()/4},
                        new Color[] {Color.RED, Color.GREEN, Color.BLUE}
                );
                break;

            case 4: // Triangle with 2 same colors
                triangle = new Triangle2D(
                        new int[] {image.getWidth()/2, image.getWidth()/4, 3*image.getWidth()/4},
                        new int[] {30, image.getHeight()-30, image.getHeight()-30},
                        new Color[] {new Color(50, 0, 100), new Color(50, 0, 100), new Color(0, 150, 150)}
                );
                break;

            case 5: // Triangle with 3 same colors
                triangle = new Triangle2D(
                        new int[] {image.getWidth()/2, image.getWidth()/4, 3*image.getWidth()/4},
                        new int[] {30, image.getHeight()-30, image.getHeight()-30},
                        new Color[] {new Color(255, 0, 0), new Color(255, 0, 0), new Color(255, 0, 0)}
                );
                break;

            case 6: // NEW: Long Thin Triangle (very stretched)
                triangle = new Triangle2D(
                        new int[] {50, 450, 250},
                        new int[] {200, 200, 180},
                        new Color[] {Color.BLUE, Color.RED, new Color(255, 165, 0)} // Orange
                );
                break;

            case 7: // NEW: Right-Angled Triangle
                triangle = new Triangle2D(
                        new int[] {100, 100, 400},
                        new int[] {100, 300, 300},
                        new Color[] {Color.CYAN, Color.MAGENTA, Color.YELLOW}
                );
                break;

            case 8: // NEW: Obtuse Triangle
                triangle = new Triangle2D(
                        new int[] {150, 350, 400},
                        new int[] {150, 150, 300},
                        new Color[] {new Color(128, 0, 128), new Color(0, 128, 0), new Color(128, 128, 0)} // Purple, Green, Olive
                );
                break;
        }

        if (triangle != null) {
            triangle.gouraudShadeToImage(image);

            // No longer drawing triangle outline
        }

        return image;
    }

    private void saveTriangleImage(BufferedImage image, String filename) {
        try {
            ImageIO.write(image, "PNG", new File(filename));
            System.out.println("Saved " + filename);
        } catch (IOException e) {
            System.err.println("Error saving image " + filename + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new GouraudShadingDemo();
        System.out.println("All triangle images saved successfully!");
    }
}
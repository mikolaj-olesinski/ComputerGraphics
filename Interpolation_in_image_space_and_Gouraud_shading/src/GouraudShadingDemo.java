import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GouraudShadingDemo {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 400;

    private BufferedImage[] triangleImages;

    private int currentCase = 0;

    private TrianglePanel trianglePanel;

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
        createOutputDirectory();

        createTriangleCases();

        createGraphicsDemo();
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
        triangleImages = new BufferedImage[9];

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


        g.setColor(Color.BLACK);
        g.drawString(labels[caseNumber], 10, 20);
        g.dispose();

        Triangle2D triangle = createTriangle(caseNumber, image.getWidth(), image.getHeight());

        if (triangle != null) {
            triangle.setRenderMode(Triangle2D.RenderMode.BUFFERED_IMAGE);
            triangle.gouraudShadeToImage(image);
        }

        return image;
    }

    private Triangle2D createTriangle(int caseNumber, int width, int height) {
        Triangle2D triangle = null;

        switch (caseNumber) {
            case 0: // Standard Triangle (top-to-bottom)
                triangle = new Triangle2D(
                        new int[] {width/2, width/4, 3*width/4},
                        new int[] {30, height-30, height-30},
                        new Color[] {Color.BLUE, Color.RED, Color.GREEN}
                );
                break;

            case 1: // Upside-Down Triangle
                triangle = new Triangle2D(
                        new int[] {width/2, width/4, 3*width/4},
                        new int[] {height-30, 30, 30},
                        new Color[] {Color.MAGENTA, Color.YELLOW, Color.CYAN}
                );
                break;

            case 2: // Non-Parallel Triangle 1
                triangle = new Triangle2D(
                        new int[] {width/3, width/4, 3*width/5},
                        new int[] {height/4, 3*height/4, 2*height/3},
                        new Color[] {Color.ORANGE, Color.PINK, new Color(100, 200, 100)}
                );
                break;

            case 3: // Non-Parallel Triangle 2
                triangle = new Triangle2D(
                        new int[] {width/4, 2*width/3, 3*width/4},
                        new int[] {height/3, height/4, 3*height/4},
                        new Color[] {Color.RED, Color.GREEN, Color.BLUE}
                );
                break;

            case 4: // Triangle with 2 same colors
                triangle = new Triangle2D(
                        new int[] {width/2, width/4, 3*width/4},
                        new int[] {30, height-30, height-30},
                        new Color[] {new Color(50, 0, 100), new Color(50, 0, 100), new Color(0, 150, 150)}
                );
                break;

            case 5: // Triangle with 3 same colors
                triangle = new Triangle2D(
                        new int[] {width/2, width/4, 3*width/4},
                        new int[] {30, height-30, height-30},
                        new Color[] {new Color(255, 0, 0), new Color(255, 0, 0), new Color(255, 0, 0)}
                );
                break;

            case 6: //Long Thin Triangle
                triangle = new Triangle2D(
                        new int[] {50, 450, 250},
                        new int[] {200, 200, 180},
                        new Color[] {Color.BLUE, Color.RED, new Color(255, 165, 0)} // Orange
                );
                break;

            case 7: //Right-Angled Triangle
                triangle = new Triangle2D(
                        new int[] {100, 100, 400},
                        new int[] {100, 300, 300},
                        new Color[] {Color.CYAN, Color.MAGENTA, Color.YELLOW}
                );
                break;

            case 8: //Obtuse Triangle
                triangle = new Triangle2D(
                        new int[] {150, 350, 400},
                        new int[] {150, 150, 300},
                        new Color[] {new Color(128, 0, 128), new Color(0, 128, 0), new Color(128, 128, 0)} // Purple, Green, Olive
                );
                break;
        }

        return triangle;
    }

    private void saveTriangleImage(BufferedImage image, String filename) {
        try {
            ImageIO.write(image, "PNG", new File(filename));
            System.out.println("Saved " + filename);
        } catch (IOException e) {
            System.err.println("Error saving image " + filename + ": " + e.getMessage());
        }
    }

    private class TrianglePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.BLACK);
            g.drawString(labels[currentCase] + " (Graphics Rendering)", 10, 20);

            Triangle2D triangle = createTriangle(currentCase, getWidth(), getHeight());
            triangle.setRenderMode(Triangle2D.RenderMode.GRAPHICS);
            triangle.gouraudShadeToGraphics(g, getWidth(), getHeight());

            g.setColor(Color.BLACK);
            g.drawString("Pixels drawn: " + triangle.getPixelsDrawn(), 10, getHeight() - 10);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(WIDTH, HEIGHT);
        }
    }

    private void createGraphicsDemo() {
        JFrame frame = new JFrame("Gouraud Shading - Graphics Rendering Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(WIDTH, HEIGHT);

        trianglePanel = new TrianglePanel();
        frame.add(trianglePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton prevButton = new JButton("Previous");
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentCase = (currentCase - 1 + labels.length) % labels.length;
                trianglePanel.repaint();
            }
        });

        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentCase = (currentCase + 1) % labels.length;
                trianglePanel.repaint();
            }
        });

        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new GouraudShadingDemo();
        System.out.println("All triangle images saved successfully!");
    }
}
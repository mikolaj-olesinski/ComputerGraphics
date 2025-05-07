import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class TrianglePerformanceTest extends JPanel {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private java.util.List<Triangle2D> triangles = new ArrayList<>();
    private Random rand = new Random();
    private boolean bufferedMode = true;
    private BufferedImage testImage; // holds last buffered test result

    public TrianglePerformanceTest() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        generateTriangles(1000);
    }

    private void generateTriangles(int count) {
        triangles.clear();
        for (int i = 0; i < count; i++) {
            int[] x = { rand.nextInt(WIDTH), rand.nextInt(WIDTH), rand.nextInt(WIDTH) };
            int[] y = { rand.nextInt(HEIGHT), rand.nextInt(HEIGHT), rand.nextInt(HEIGHT) };
            Color[] cols = { randomColor(), randomColor(), randomColor() };
            Triangle2D t = new Triangle2D(x, y, cols);
            t.setRenderMode(bufferedMode ? Triangle2D.RenderMode.BUFFERED_IMAGE : Triangle2D.RenderMode.GRAPHICS);
            triangles.add(t);
        }
        testImage = null; // clear any previous test display
        repaint();
    }

    private Color randomColor() {
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // if in buffered test mode, draw the generated image
        if (bufferedMode && testImage != null) {
            g.drawImage(testImage, 0, 0, null);
        }

    }

    private void runTest() {
        long t0, t1, pixels;
        double secs, triPerSec, pixPerSec;
        String msg;

        if (bufferedMode) {
            // Buffered image mode test
            BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            t0 = System.nanoTime();
            pixels = 0;
            for (Triangle2D t : triangles) {
                t.resetPixelCount();
                t.gouraudShadeToImage(img);
                pixels += t.getPixelsDrawn();
            }
            t1 = System.nanoTime();
            secs = (t1 - t0) / 1e9;
            triPerSec = triangles.size() / secs;
            pixPerSec = pixels / secs;

            testImage = img; // store for display
            repaint();

            msg = String.format("Buffered: %.1f tri/s, %.1f pix/s", triPerSec, pixPerSec);
        } else {
            // Graphics mode test
            Graphics g = getGraphics();
            t0 = System.nanoTime();
            pixels = 0;
            for (Triangle2D t : triangles) {
                t.resetPixelCount();
                t.gouraudShadeToGraphics(g, WIDTH, HEIGHT);
                pixels += t.getPixelsDrawn();
            }
            t1 = System.nanoTime();
            g.dispose();
            secs = (t1 - t0) / 1e9;
            triPerSec = triangles.size() / secs;
            pixPerSec = pixels / secs;

            msg = String.format("Graphics: %.1f tri/s, %.1f pix/s", triPerSec, pixPerSec);
        }

        // show results
        System.out.println(msg);
        JOptionPane.showMessageDialog(this, msg, "Performance Results", JOptionPane.INFORMATION_MESSAGE);

        if (!bufferedMode) {
            repaint();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Triangle Performance Test");
        TrianglePerformanceTest panel = new TrianglePerformanceTest();

        JButton testBtn = new JButton("Test");
        JButton switchBtn = new JButton(panel.bufferedMode ? "Mode: Buffered" : "Mode: Graphics");
        JButton genBtn = new JButton("New Triangles");

        testBtn.addActionListener(e -> panel.runTest());
        switchBtn.addActionListener(e -> {
            panel.bufferedMode = !panel.bufferedMode;
            for (Triangle2D t : panel.triangles) {
                t.setRenderMode(panel.bufferedMode ? Triangle2D.RenderMode.BUFFERED_IMAGE : Triangle2D.RenderMode.GRAPHICS);
            }
            panel.testImage = null;
            switchBtn.setText(panel.bufferedMode ? "Mode: Buffered" : "Mode: Graphics");
            panel.repaint();
        });
        genBtn.addActionListener(e -> panel.generateTriangles(1000));

        JPanel controls = new JPanel();
        controls.add(testBtn);
        controls.add(switchBtn);
        controls.add(genBtn);

        frame.setLayout(new BorderLayout());
        frame.add(controls, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

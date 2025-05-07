import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;

public class Triangle2D {
    private int[] x;
    private int[] y;
    private Color[] colors;
    private int pixelsDrawn;

    // Enum definiujący tryby rysowania
    public enum RenderMode {
        BUFFERED_IMAGE,
        GRAPHICS
    }

    // Domyślny tryb rysowania
    private RenderMode renderMode = RenderMode.BUFFERED_IMAGE;

    public Triangle2D(int[] x, int[] y, Color[] colors) {
        if (x.length != 3 || y.length != 3 || colors.length != 3) {
            throw new IllegalArgumentException("Trójkąt musi mieć dokładnie 3 wierzchołki!");
        }
        this.x = x.clone();
        this.y = y.clone();
        this.colors = colors.clone();
        this.pixelsDrawn = 0;
        sortVerticesByY();
    }

    public int[] getX() {
        return x.clone();
    }

    public int[] getY() {
        return y.clone();
    }

    public int getPixelsDrawn() {
        return pixelsDrawn;
    }

    public void resetPixelCount() {
        this.pixelsDrawn = 0;
    }

    public void setRenderMode(RenderMode mode) {
        this.renderMode = mode;
    }

    public RenderMode getRenderMode() {
        return renderMode;
    }

    private void sortVerticesByY() {
        Integer[] idx = {0, 1, 2};
        Arrays.sort(idx, Comparator.comparingInt(i -> y[i]));
        int[] newX = new int[3], newY = new int[3];
        Color[] newC = new Color[3];
        for (int i = 0; i < 3; i++) {
            newX[i] = x[idx[i]];
            newY[i] = y[idx[i]];
            newC[i] = colors[idx[i]];
        }
        x = newX;
        y = newY;
        colors = newC;
    }

    // Metoda do rysowania w trybie BufferedImage - pozostawiona dla kompatybilności wstecznej
    public int gouraudShadeToImage(BufferedImage image) {
        return renderTriangle(image, null, image.getWidth(), image.getHeight(), RenderMode.BUFFERED_IMAGE);
    }

    // Metoda do rysowania w trybie Graphics - pozostawiona dla kompatybilności wstecznej
    public int gouraudShadeToGraphics(Graphics g, int width, int height) {
        return renderTriangle(null, g, width, height, RenderMode.GRAPHICS);
    }

    // Nowa zunifikowana metoda rysowania
    public int renderTriangle(BufferedImage image, Graphics g, int width, int height) {
        return renderTriangle(image, g, width, height, this.renderMode);
    }

    // Główna metoda rysowania używająca aktualnego trybu
    private int renderTriangle(BufferedImage image, Graphics g, int width, int height, RenderMode mode) {
        pixelsDrawn = 0; // Reset licznika przed rozpoczęciem rysowania

        if (y[0] != y[1] && y[1] != y[2]) {
            float t = (float) (y[1] - y[0]) / (y[2] - y[0]);
            int x4 = Math.round(x[0] + t * (x[2] - x[0]));
            Color color4 = interpolateColor(colors[0], colors[2], t);

            // Górny trójkąt (flat-bottom)
            fillTriangle(
                    image, g,
                    new Edge(x[0], y[0], colors[0], x[1], y[1], colors[1]),
                    new Edge(x[0], y[0], colors[0], x4,  y[1], color4),
                    width, height, mode
            );

            // Dolny trójkąt (flat-top)
            fillTriangle(
                    image, g,
                    new Edge(x[1], y[1], colors[1], x[2], y[2], colors[2]),
                    new Edge(x4,  y[1], color4,   x[2], y[2], colors[2]),
                    width, height, mode
            );
        } else if (y[0] == y[1]) {
            fillTriangle(
                    image, g,
                    new Edge(x[0], y[0], colors[0], x[2], y[2], colors[2]),
                    new Edge(x[1], y[1], colors[1], x[2], y[2], colors[2]),
                    width, height, mode
            );
        } else {
            fillTriangle(
                    image, g,
                    new Edge(x[0], y[0], colors[0], x[1], y[1], colors[1]),
                    new Edge(x[0], y[0], colors[0], x[2], y[2], colors[2]),
                    width, height, mode
            );
        }

        return pixelsDrawn;
    }

    private void fillTriangle(BufferedImage image, Graphics g, Edge leftEdge, Edge rightEdge,
                              int width, int height, RenderMode mode) {
        EdgeInterpolator l = new EdgeInterpolator(leftEdge);
        EdgeInterpolator r = new EdgeInterpolator(rightEdge);

        int yStart = Math.max(0, Math.min(leftEdge.y1, rightEdge.y1));
        int yEnd   = Math.min(height - 1, Math.max(leftEdge.y2, rightEdge.y2));

        for (int y = yStart; y <= yEnd; y++) {
            int xLeft  = l.getX();
            int xRight = r.getX();

            Color cLeft  = l.getColor();
            Color cRight = r.getColor();

            // Sprawdzenie czy należy zamienić punkty miejscami
            if (xLeft > xRight) {
                int tmpX = xLeft;
                xLeft = xRight;
                xRight = tmpX;
                Color tmpC = cLeft;
                cLeft = cRight;
                cRight = tmpC;
            }
            xLeft  = Math.max(0, Math.min(width - 1, xLeft));
            xRight = Math.max(0, Math.min(width - 1, xRight));

            drawScanline(image, g, y, xLeft, xRight, cLeft, cRight, mode);

            l.step();
            r.step();
        }
    }

    private static class EdgeInterpolator {
        private float x, dx;
        private float r, dr, g, dg, b, db;

        EdgeInterpolator(Edge e) {
            float invH = 1.0f / (e.y2 - e.y1);
            this.x  = e.x1;
            this.dx = (e.x2 - e.x1) * invH;

            this.r  = e.c1.getRed();
            this.dr = (e.c2.getRed() - e.c1.getRed()) * invH;

            this.g  = e.c1.getGreen();
            this.dg = (e.c2.getGreen() - e.c1.getGreen()) * invH;

            this.b  = e.c1.getBlue();
            this.db = (e.c2.getBlue() - e.c1.getBlue()) * invH;
        }

        int getX() {
            return Math.round(x);
        }

        Color getColor() {
            return new Color(
                    clamp(Math.round(r)),
                    clamp(Math.round(g)),
                    clamp(Math.round(b))
            );
        }

        void step() {
            x += dx;
            r += dr;
            g += dg;
            b += db;
        }
    }

    // Zunifikowana metoda rysowania linii skanującej
    private void drawScanline(BufferedImage image, Graphics g, int y, int xLeft, int xRight,
                              Color colorLeft, Color colorRight, RenderMode mode) {
        int pixelCount = xRight - xLeft + 1;
        if (pixelCount <= 0) return;

        for (int x = xLeft; x <= xRight; x++) {
            // Calculate interpolation factor t (0.0 at xLeft, 1.0 at xRight)
            float t = (pixelCount > 1) ? (x - xLeft) / (float)(pixelCount - 1) : 0;
            Color pixelColor = interpolateColor(colorLeft, colorRight, t);

            // Renderowanie w zależności od wybranego trybu
            if (mode == RenderMode.BUFFERED_IMAGE) {
                image.setRGB(x, y, pixelColor.getRGB());
            } else if (mode == RenderMode.GRAPHICS) {
                g.setColor(pixelColor);
                g.fillRect(x, y, 1, 1);
            }

            pixelsDrawn++;
        }
    }

    private Color interpolateColor(Color c1, Color c2, float t) {
        int r = Math.round(c1.getRed()   + t * (c2.getRed()   - c1.getRed()));
        int g = Math.round(c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
        int b = Math.round(c1.getBlue()  + t * (c2.getBlue()  - c1.getBlue()));
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static class Edge {
        int x1, y1, x2, y2;
        Color c1, c2;
        Edge(int x1, int y1, Color c1, int x2, int y2, Color c2) {
            this.x1 = x1; this.y1 = y1; this.c1 = c1;
            this.x2 = x2; this.y2 = y2; this.c2 = c2;
        }
    }
}
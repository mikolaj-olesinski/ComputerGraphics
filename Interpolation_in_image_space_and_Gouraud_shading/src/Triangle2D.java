import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;

public class Triangle2D {
    private int[] x;
    private int[] y;
    private Color[] colors;
    private int pixelsDrawn; // Dodane pole do zliczania narysowanych pikseli

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

    // Nowa metoda zwracająca liczbę narysowanych pikseli
    public int getPixelsDrawn() {
        return pixelsDrawn;
    }

    // Metoda do resetowania licznika pikseli
    public void resetPixelCount() {
        this.pixelsDrawn = 0;
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

    public int gouraudShadeToImage(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Obraz nie może być null!");
        }
        pixelsDrawn = 0; // Reset licznika przed rozpoczęciem rysowania
        int width = image.getWidth();
        int height = image.getHeight();

        if (y[0] != y[1] && y[1] != y[2]) {
            float t = (float) (y[1] - y[0]) / (y[2] - y[0]);
            int x4 = Math.round(x[0] + t * (x[2] - x[0]));
            Color color4 = interpolateColor(colors[0], colors[2], t);

            // Górny trójkąt (flat-bottom)
            fillTriangleWithEdges(
                    image,
                    new Edge(x[0], y[0], colors[0], x[1], y[1], colors[1]),
                    new Edge(x[0], y[0], colors[0], x4,  y[1], color4),
                    width, height
            );

            // Dolny trójkąt (flat-top)
            fillTriangleWithEdges(
                    image,
                    new Edge(x[1], y[1], colors[1], x[2], y[2], colors[2]),
                    new Edge(x4,  y[1], color4,   x[2], y[2], colors[2]),
                    width, height
            );
        } else if (y[0] == y[1]) {
            fillTriangleWithEdges(
                    image,
                    new Edge(x[0], y[0], colors[0], x[2], y[2], colors[2]),
                    new Edge(x[1], y[1], colors[1], x[2], y[2], colors[2]),
                    width, height
            );
        } else {
            fillTriangleWithEdges(
                    image,
                    new Edge(x[0], y[0], colors[0], x[1], y[1], colors[1]),
                    new Edge(x[0], y[0], colors[0], x[2], y[2], colors[2]),
                    width, height
            );
        }

        return pixelsDrawn; // Zwróć liczbę narysowanych pikseli
    }

    // Nowa metoda do rysowania trójkąta bezpośrednio przy użyciu Graphics
    public int gouraudShadeToGraphics(Graphics g, int width, int height) {
        if (g == null) {
            throw new IllegalArgumentException("Graphics nie może być null!");
        }
        pixelsDrawn = 0; // Reset licznika przed rozpoczęciem rysowania

        if (y[0] != y[1] && y[1] != y[2]) {
            float t = (float) (y[1] - y[0]) / (y[2] - y[0]);
            int x4 = Math.round(x[0] + t * (x[2] - x[0]));
            Color color4 = interpolateColor(colors[0], colors[2], t);

            // Górny trójkąt (flat-bottom)
            fillTriangleWithGraphics(
                    g,
                    new Edge(x[0], y[0], colors[0], x[1], y[1], colors[1]),
                    new Edge(x[0], y[0], colors[0], x4,  y[1], color4),
                    width, height
            );

            // Dolny trójkąt (flat-top)
            fillTriangleWithGraphics(
                    g,
                    new Edge(x[1], y[1], colors[1], x[2], y[2], colors[2]),
                    new Edge(x4,  y[1], color4,   x[2], y[2], colors[2]),
                    width, height
            );
        } else if (y[0] == y[1]) {
            fillTriangleWithGraphics(
                    g,
                    new Edge(x[0], y[0], colors[0], x[2], y[2], colors[2]),
                    new Edge(x[1], y[1], colors[1], x[2], y[2], colors[2]),
                    width, height
            );
        } else {
            fillTriangleWithGraphics(
                    g,
                    new Edge(x[0], y[0], colors[0], x[1], y[1], colors[1]),
                    new Edge(x[0], y[0], colors[0], x[2], y[2], colors[2]),
                    width, height
            );
        }

        return pixelsDrawn;
    }

    private void fillTriangleWithEdges(BufferedImage image, Edge leftEdge, Edge rightEdge, int width, int height) {
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

            drawScanline(image, y, xLeft, xRight, cLeft, cRight);

            l.step();
            r.step();
        }
    }

    // Nowa metoda do rysowania trójkąta przy użyciu Graphics
    private void fillTriangleWithGraphics(Graphics g, Edge leftEdge, Edge rightEdge, int width, int height) {
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

            drawScanlineToGraphics(g, y, xLeft, xRight, cLeft, cRight);

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

    private void drawScanline(BufferedImage image, int y, int xLeft, int xRight, Color colorLeft, Color colorRight) {
        int pixelCount = xRight - xLeft + 1;
        if (pixelCount <= 0) return;

        for (int x = xLeft; x <= xRight; x++) {
            // Calculate interpolation factor t (0.0 at xLeft, 1.0 at xRight)
            float t = (pixelCount > 1) ? (x - xLeft) / (float)(pixelCount - 1) : 0;

            Color pixelColor = interpolateColor(colorLeft, colorRight, t);

            // Set the pixel color
            image.setRGB(x, y, pixelColor.getRGB());
            pixelsDrawn++;
        }
    }

    // Nowa metoda do rysowania linii skanującej przy użyciu Graphics
    private void drawScanlineToGraphics(Graphics g, int y, int xLeft, int xRight, Color colorLeft, Color colorRight) {
        int pixelCount = xRight - xLeft + 1;
        if (pixelCount <= 0) return;

        for (int x = xLeft; x <= xRight; x++) {
            // Calculate interpolation factor t (0.0 at xLeft, 1.0 at xRight)
            float t = (pixelCount > 1) ? (x - xLeft) / (float)(pixelCount - 1) : 0;

            Color interpolated_color = interpolateColor(colorLeft, colorRight, t);

            // Ustawienie koloru i narysowanie piksela
            g.setColor(interpolated_color);
            g.fillRect(x, y, 1, 1);
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
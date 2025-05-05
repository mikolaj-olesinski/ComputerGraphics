import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;

public class Triangle2D {
    private int[] x;
    private int[] y;
    private Color[] colors;

    public Triangle2D(int[] x, int[] y, Color[] colors) {
        if (x.length != 3 || y.length != 3 || colors.length != 3) {
            throw new IllegalArgumentException("Trójkąt musi mieć dokładnie 3 wierzchołki!");
        }
        this.x = x.clone();
        this.y = y.clone();
        this.colors = colors.clone();
        sortVerticesByY();
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

    public void gouraudShadeToImage(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Obraz nie może być null!");
        }
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
                    new Edge(x[0], y[0], colors[0], x4, y[1], color4),
                    Math.max(0, y[0]),
                    Math.min(height - 1, y[1]),
                    width, height
            );

            // Dolny trójkąt (flat-top)
            fillTriangleWithEdges(
                    image,
                    new Edge(x[1], y[1], colors[1], x[2], y[2], colors[2]),
                    new Edge(x4, y[1], color4, x[2], y[2], colors[2]),
                    Math.max(0, y[1]),
                    Math.min(height - 1, y[2]),
                    width, height
            );
        } else if (y[0] == y[1]) {
            fillTriangleWithEdges(
                    image,
                    new Edge(x[0], y[0], colors[0], x[2], y[2], colors[2]),
                    new Edge(x[1], y[1], colors[1], x[2], y[2], colors[2]),
                    Math.max(0, y[0]),
                    Math.min(height - 1, y[2]),
                    width, height
            );
        } else {
            fillTriangleWithEdges(
                    image,
                    new Edge(x[0], y[0], colors[0], x[1], y[1], colors[1]),
                    new Edge(x[0], y[0], colors[0], x[2], y[2], colors[2]),
                    Math.max(0, y[0]),
                    Math.min(height - 1, y[2]),
                    width, height
            );
        }
    }

    private void fillTriangleWithEdges(BufferedImage image, Edge leftEdge, Edge rightEdge, int yStart, int yEnd, int width, int height) {
        float leftInvHeight = 1.0f / (leftEdge.y2 - leftEdge.y1);
        float rightInvHeight = 1.0f / (rightEdge.y2 - rightEdge.y1);

        for (int y = yStart; y <= yEnd; y++) {
            float tLeft = (float) (y - leftEdge.y1) * leftInvHeight;
            int xLeft = Math.round(leftEdge.x1 + tLeft * (leftEdge.x2 - leftEdge.x1));
            Color colorLeft = interpolateColor(leftEdge.c1, leftEdge.c2, tLeft);

            float tRight = (float) (y - rightEdge.y1) * rightInvHeight;
            int xRight = Math.round(rightEdge.x1 + tRight * (rightEdge.x2 - rightEdge.x1));
            Color colorRight = interpolateColor(rightEdge.c1, rightEdge.c2, tRight);

            adjustAndDrawScanline(image, y, xLeft, xRight, colorLeft, colorRight, width, height);
        }
    }

    private void adjustAndDrawScanline(BufferedImage image, int y, int xLeft, int xRight, Color colorLeft, Color colorRight, int width, int height) {
        if (xLeft > xRight) {
            int tempX = xLeft;
            xLeft = xRight;
            xRight = tempX;
            Color tempColor = colorLeft;
            colorLeft = colorRight;
            colorRight = tempColor;
        }
        xLeft = Math.max(0, Math.min(width - 1, xLeft));
        xRight = Math.max(0, Math.min(width - 1, xRight));
        drawScanline(image, y, xLeft, xRight, colorLeft, colorRight);
    }

    private Color interpolateColor(Color c1, Color c2, float t) {
        int r = Math.round(c1.getRed() + t * (c2.getRed() - c1.getRed()));
        int g = Math.round(c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
        int b = Math.round(c1.getBlue() + t * (c2.getBlue() - c1.getBlue()));
        return new Color(
                clamp(r), clamp(g), clamp(b)
        );
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private void drawScanline(BufferedImage image, int y, int xLeft, int xRight, Color colorLeft, Color colorRight) {
        int pixelCount = xRight - xLeft + 1;
        if (pixelCount <= 0) return;

        float rStep = (colorRight.getRed() - colorLeft.getRed()) / (float) pixelCount;
        float gStep = (colorRight.getGreen() - colorLeft.getGreen()) / (float) pixelCount;
        float bStep = (colorRight.getBlue() - colorLeft.getBlue()) / (float) pixelCount;

        float r = colorLeft.getRed();
        float g = colorLeft.getGreen();
        float b = colorLeft.getBlue();

        for (int x = xLeft; x <= xRight; x++) {
            image.setRGB(x, y, new Color(clamp((int) r), clamp((int) g), clamp((int) b)).getRGB());
            r += rStep;
            g += gStep;
            b += bStep;
        }
    }

    private static class Edge {
        int x1, y1, x2, y2;
        Color c1, c2;

        Edge(int x1, int y1, Color c1, int x2, int y2, Color c2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.c1 = c1;
            this.c2 = c2;
        }
    }
}
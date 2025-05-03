import java.awt.*;
import java.awt.geom.Point2D;

public abstract class ShapeElement extends PosterElement {
    protected Color color;
    protected static final int DEFAULT_SIZE = 100;

    public ShapeElement(Color color) {
        this.color = color;
    }

    @Override
    public int getInitialWidth() {
        return DEFAULT_SIZE;
    }

    @Override
    public int getInitialHeight() {
        return DEFAULT_SIZE;
    }

    @Override
    public Point2D[] getCornerPoints() {
        Point2D[] points = new Point2D[] {
                new Point2D.Double(0, 0),                // top-left
                new Point2D.Double(DEFAULT_SIZE, 0),     // top-right
                new Point2D.Double(DEFAULT_SIZE, DEFAULT_SIZE), // bottom-right
                new Point2D.Double(0, DEFAULT_SIZE)      // bottom-left
        };

        Point2D[] transformedPoints = new Point2D[4];
        for (int i = 0; i < 4; i++) {
            transformedPoints[i] = new Point2D.Double();
            transform.transform(points[i], transformedPoints[i]);
        }

        return transformedPoints;
    }

    protected static String colorToString(Color color) {
        return String.format("%d,%d,%d",
                color.getRed(), color.getGreen(),
                color.getBlue());
    }

    // Convert string to color
    protected static Color stringToColor(String str) {
        String[] parts = str.split(",");
        if (parts.length != 3) {
            return Color.BLACK;
        }

        try {
            int r = Integer.parseInt(parts[0]);
            int g = Integer.parseInt(parts[1]);
            int b = Integer.parseInt(parts[2]);
            return new Color(r, g, b);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }
}
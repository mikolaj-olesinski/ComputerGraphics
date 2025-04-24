import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// Abstract base class for shape elements
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

    @Override
    public Rectangle2D getBounds() {
        Point2D[] cornerPoints = getCornerPoints();

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Point2D point : cornerPoints) {
            minX = Math.min(minX, point.getX());
            minY = Math.min(minY, point.getY());
            maxX = Math.max(maxX, point.getX());
            maxY = Math.max(maxY, point.getY());
        }

        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }
}
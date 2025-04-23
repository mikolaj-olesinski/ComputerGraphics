import java.awt.*;
import java.awt.geom.*;

public class ShapeElement extends PosterElement {
    private ShapeType type;
    private Color color;
    private static final int DEFAULT_SIZE = 100;

    public ShapeElement(ShapeType type, Color color) {
        this.type = type;
        this.color = color;
    }

    @Override
    public void draw(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();
        g2d.transform(transform);

        g2d.setColor(color);
        if (type == ShapeType.RECTANGLE) {
            g2d.fillRect(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);
        } else {
            g2d.fillOval(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);
        }

        g2d.setTransform(oldTransform);
    }

    @Override
    public Rectangle2D getBounds() {
        // Get the transformed corner points
        Point2D[] cornerPoints = getCornerPoints();

        // Find the bounding box that contains all transformed corner points
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

    @Override
    public Point2D[] getCornerPoints() {
        // Punkty oryginalnego prostokąta
        Point2D[] points = new Point2D[] {
                new Point2D.Double(0, 0),                      // lewy górny
                new Point2D.Double(DEFAULT_SIZE, 0),           // prawy górny
                new Point2D.Double(DEFAULT_SIZE, DEFAULT_SIZE), // prawy dolny
                new Point2D.Double(0, DEFAULT_SIZE)            // lewy dolny
        };

        // Transformacja każdego punktu
        Point2D[] transformedPoints = new Point2D[4];
        for (int i = 0; i < 4; i++) {
            transformedPoints[i] = new Point2D.Double();
            transform.transform(points[i], transformedPoints[i]);
        }

        return transformedPoints;
    }

    @Override
    public boolean contains(Point p) {
        try {
            Point2D inversePt = new Point2D.Double();
            transform.inverseTransform(p, inversePt);

            if (type == ShapeType.RECTANGLE) {
                return new Rectangle2D.Double(0, 0, DEFAULT_SIZE, DEFAULT_SIZE)
                        .contains(inversePt);
            } else {
                return new Ellipse2D.Double(0, 0, DEFAULT_SIZE, DEFAULT_SIZE)
                        .contains(inversePt);
            }
        } catch (NoninvertibleTransformException e) {
            return false;
        }
    }

    @Override
    public int getInitialWidth() {
        return DEFAULT_SIZE;
    }

    @Override
    public int getInitialHeight() {
        return DEFAULT_SIZE;
    }
}
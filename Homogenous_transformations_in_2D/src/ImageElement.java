import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class ImageElement extends PosterElement {
    private BufferedImage image;
    private String name;

    public ImageElement(BufferedImage image, String name) {
        this.image = image;
        this.name = name;
    }

    @Override
    public void draw(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();
        g2d.transform(transform);
        g2d.drawImage(image, 0, 0, null);
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
        // Punkty oryginalnego prostokąta obrazu
        Point2D[] points = new Point2D[] {
                new Point2D.Double(0, 0),                    // lewy górny
                new Point2D.Double(image.getWidth(), 0),     // prawy górny
                new Point2D.Double(image.getWidth(), image.getHeight()), // prawy dolny
                new Point2D.Double(0, image.getHeight())     // lewy dolny
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
            return new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight())
                    .contains(inversePt);
        } catch (NoninvertibleTransformException e) {
            return false;
        }
    }

    @Override
    public int getInitialWidth() {
        return image.getWidth();
    }

    @Override
    public int getInitialHeight() {
        return image.getHeight();
    }
}

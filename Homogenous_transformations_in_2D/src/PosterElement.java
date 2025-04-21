import java.awt.*;
import java.awt.geom.*;

public abstract class PosterElement implements Cloneable {
    protected AffineTransform transform = new AffineTransform();
    protected static final int HANDLE_SIZE = 8;

    public abstract void draw(Graphics2D g2d);
    public abstract Rectangle2D getBounds();
    public abstract boolean contains(Point p);
    public abstract int getInitialWidth();
    public abstract int getInitialHeight();

    public AffineTransform getTransform() {
        return transform;
    }

    @Override
    public PosterElement clone() {
        try {
            PosterElement clone = (PosterElement) super.clone();
            clone.transform = new AffineTransform(transform);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    // Draw handles for selected element
    public void drawHandles(Graphics2D g2d) {
        Point2D[] handles = getHandlePositions();

        g2d.setColor(Color.BLACK);
        for (Point2D handle : handles) {
            g2d.fillRect((int)(handle.getX() - HANDLE_SIZE/2),
                    (int)(handle.getY() - HANDLE_SIZE/2),
                    HANDLE_SIZE, HANDLE_SIZE);
        }

        // Draw bounding box
        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
        Rectangle2D bounds = getBounds();
        g2d.draw(bounds);
    }

    // Return handle positions
    public Point2D[] getHandlePositions() {
        Rectangle2D bounds = getBounds();
        double centerX = bounds.getCenterX();
        double centerY = bounds.getCenterY();

        return new Point2D[] {
                new Point2D.Double(bounds.getMinX(), bounds.getMinY()), // Top-left
                new Point2D.Double(centerX, bounds.getMinY()),          // Top-center
                new Point2D.Double(bounds.getMaxX(), bounds.getMinY()), // Top-right
                new Point2D.Double(bounds.getMaxX(), centerY),          // Right-center
                new Point2D.Double(bounds.getMaxX(), bounds.getMaxY()), // Bottom-right
                new Point2D.Double(centerX, bounds.getMaxY()),          // Bottom-center
                new Point2D.Double(bounds.getMinX(), bounds.getMaxY()), // Bottom-left
                new Point2D.Double(bounds.getMinX(), centerY)           // Left-center
        };
    }

    // Check if point is within a handle and return its index
    public int getHandleAt(Point p) {
        Point2D[] handles = getHandlePositions();

        for (int i = 0; i < handles.length; i++) {
            if (new Rectangle2D.Double(
                    handles[i].getX() - HANDLE_SIZE/2,
                    handles[i].getY() - HANDLE_SIZE/2,
                    HANDLE_SIZE, HANDLE_SIZE).contains(p)) {
                return i;
            }
        }

        return -1;
    }

    // Transform element by handle
    public void transformByHandle(int handleIndex, int dx, int dy) {
        Rectangle2D bounds = getBounds();
        double centerX = bounds.getCenterX();
        double centerY = bounds.getCenterY();
        Point2D[] handles = getHandlePositions();
        Point2D handle = handles[handleIndex];

        if (handleIndex % 2 == 0) {
            // Corner handles (0, 2, 4, 6) - perform scaling and rotation
            double oldAngle = Math.atan2(
                    handle.getY() - centerY,
                    handle.getX() - centerX
            );
            double newAngle = Math.atan2(
                    handle.getY() + dy - centerY,
                    handle.getX() + dx - centerX
            );
            double angleChange = newAngle - oldAngle;

            double oldDist = Point2D.distance(
                    centerX, centerY,
                    handle.getX(), handle.getY()
            );
            double newDist = Point2D.distance(
                    centerX, centerY,
                    handle.getX() + dx, handle.getY() + dy
            );
            double scaleFactor = newDist / oldDist;

            if (scaleFactor > 0.1) { // Prevent scaling to zero or negative
                // Apply transformations relative to the center
                AffineTransform newTransform = new AffineTransform();
                newTransform.translate(centerX, centerY);
                newTransform.rotate(angleChange);
                newTransform.scale(scaleFactor, scaleFactor);
                newTransform.translate(-centerX, -centerY);

                transform.preConcatenate(newTransform);
            }
        } else {
            // Edge handles (1, 3, 5, 7) - perform directional scaling
            // Determine which axis to scale along
            boolean isHorizontalEdge = (handleIndex == 1 || handleIndex == 5);
            boolean isVerticalEdge = (handleIndex == 3 || handleIndex == 7);

            // Calculate scaling factors
            double scaleX = 1.0;
            double scaleY = 1.0;

            if (isHorizontalEdge) {
                // Get opposite edge y-coordinate
                double oppositeY = (handleIndex == 1) ?
                        bounds.getMaxY() : bounds.getMinY();
                double currentHeight = Math.abs(handle.getY() - oppositeY);
                double newHeight = Math.abs(handle.getY() + dy - oppositeY);
                if (newHeight > 5) { // Minimum size check
                    scaleY = newHeight / currentHeight;
                }
            }

            if (isVerticalEdge) {
                // Get opposite edge x-coordinate
                double oppositeX = (handleIndex == 3) ?
                        bounds.getMinX() : bounds.getMaxX();
                double currentWidth = Math.abs(handle.getX() - oppositeX);
                double newWidth = Math.abs(handle.getX() + dx - oppositeX);
                if (newWidth > 5) { // Minimum size check
                    scaleX = newWidth / currentWidth;
                }
            }

            // Apply directional scaling
            AffineTransform scaleTransform = new AffineTransform();
            scaleTransform.translate(centerX, centerY);
            scaleTransform.scale(scaleX, scaleY);
            scaleTransform.translate(-centerX, -centerY);

            transform.preConcatenate(scaleTransform);
        }
    }
}
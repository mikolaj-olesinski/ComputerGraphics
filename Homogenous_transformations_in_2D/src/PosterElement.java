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
    public abstract Point2D[] getCornerPoints();

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

        // Draw bounding polygon connecting corner points
        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));

        Path2D path = new Path2D.Double();
        Point2D[] cornerPoints = getCornerPoints();
        path.moveTo(cornerPoints[0].getX(), cornerPoints[0].getY());
        for (int i = 1; i < cornerPoints.length; i++) {
            path.lineTo(cornerPoints[i].getX(), cornerPoints[i].getY());
        }
        path.closePath();
        g2d.draw(path);
    }

    // Return handle positions
    public Point2D[] getHandlePositions() {
        Point2D[] cornerPoints = getCornerPoints();
        Rectangle2D bounds = getBounds();
        double centerX = bounds.getCenterX();
        double centerY = bounds.getCenterY();

        // Return 8 handle positions (4 corners + 4 midpoints)
        return new Point2D[] {
                cornerPoints[0], // Top-left
                new Point2D.Double((cornerPoints[0].getX() + cornerPoints[1].getX()) / 2,
                        (cornerPoints[0].getY() + cornerPoints[1].getY()) / 2), // Top-center
                cornerPoints[1], // Top-right
                new Point2D.Double((cornerPoints[1].getX() + cornerPoints[2].getX()) / 2,
                        (cornerPoints[1].getY() + cornerPoints[2].getY()) / 2), // Right-center
                cornerPoints[2], // Bottom-right
                new Point2D.Double((cornerPoints[2].getX() + cornerPoints[3].getX()) / 2,
                        (cornerPoints[2].getY() + cornerPoints[3].getY()) / 2), // Bottom-center
                cornerPoints[3], // Bottom-left
                new Point2D.Double((cornerPoints[3].getX() + cornerPoints[0].getX()) / 2,
                        (cornerPoints[3].getY() + cornerPoints[0].getY()) / 2)  // Left-center
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
        Point2D[] cornerPoints = getCornerPoints();
        Point2D[] handles = getHandlePositions();
        Point2D handle = handles[handleIndex];

        // Calculate center of the element
        double sumX = 0, sumY = 0;
        for (Point2D corner : cornerPoints) {
            sumX += corner.getX();
            sumY += corner.getY();
        }
        double centerX = sumX / cornerPoints.length;
        double centerY = sumY / cornerPoints.length;

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

            // Determine which axis to scale based on handle index
            boolean isVertical = (handleIndex == 1 || handleIndex == 5);
            boolean isHorizontal = (handleIndex == 3 || handleIndex == 7);

            // Get the orientation of the element from its current transform
            double[] matrix = new double[6];
            transform.getMatrix(matrix);
            double m00 = matrix[0]; // scaleX * cos(angle)
            double m10 = matrix[1]; // scaleX * sin(angle)
            double m01 = matrix[2]; // -scaleY * sin(angle)
            double m11 = matrix[3]; // scaleY * cos(angle)

            // Calculate the angle of the element
            double angle = Math.atan2(m10, m00);

            // Calculate directional vectors based on current orientation
            double dirX, dirY;
            if (isVertical) {
                // Direction perpendicular to horizontal axis
                dirX = -Math.sin(angle);
                dirY = Math.cos(angle);
            } else { // isHorizontal
                // Direction along horizontal axis
                dirX = Math.cos(angle);
                dirY = Math.sin(angle);
            }

            // Project drag vector onto direction vector
            double dragProjection = dx * dirX + dy * dirY;

            // Determine scaling factor and axis
            double scaleX = 1.0, scaleY = 1.0;

            if (isVertical) {
                // Calculate original vertical dimension
                double originalHeight = Point2D.distance(
                        handles[1].getX(), handles[1].getY(),
                        handles[5].getX(), handles[5].getY()
                );

                // Calculate scale factor for vertical handles
                double scalingDirection = (handleIndex == 1) ? -1 : 1;
                double newHeight = originalHeight + 2 * dragProjection * scalingDirection;

                if (newHeight > 5) {
                    scaleY = newHeight / originalHeight;
                }
            } else {
                // Calculate original horizontal dimension
                double originalWidth = Point2D.distance(
                        handles[3].getX(), handles[3].getY(),
                        handles[7].getX(), handles[7].getY()
                );

                // Calculate scale factor for horizontal handles
                double scalingDirection = (handleIndex == 7) ? -1 : 1;
                double newWidth = originalWidth + 2 * dragProjection * scalingDirection;

                if (newWidth > 5) {
                    scaleX = newWidth / originalWidth;
                }
            }

            // Apply scaling in the element's local coordinate system
            AffineTransform scaleTransform = new AffineTransform();
            scaleTransform.translate(centerX, centerY);
            scaleTransform.rotate(angle);
            scaleTransform.scale(scaleX, scaleY);
            scaleTransform.rotate(-angle);
            scaleTransform.translate(-centerX, -centerY);

            transform.preConcatenate(scaleTransform);
        }
    }
}

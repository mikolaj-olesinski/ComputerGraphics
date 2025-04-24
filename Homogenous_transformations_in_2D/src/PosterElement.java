import java.awt.*;
import java.awt.geom.*;
import java.util.Locale;

public abstract class PosterElement implements Cloneable {
    protected AffineTransform transform = new AffineTransform();
    protected static final int HANDLE_SIZE = 8;

    public abstract void draw(Graphics2D g2d);
    public abstract boolean contains(Point p);
    public abstract int getInitialWidth();
    public abstract int getInitialHeight();
    public abstract Point2D[] getCornerPoints();
    public abstract String save();

    public AffineTransform getTransform() {
        return transform;
    }

    public void move(int dx, int dy) {
        AffineTransform moveTransform = new AffineTransform();
        moveTransform.translate(dx, dy);
        this.transform.preConcatenate(moveTransform);
    }

    public void rotateAroundCenter(double angle) {
        Rectangle2D bounds = this.getBounds();
        double centerX = bounds.getCenterX();
        double centerY = bounds.getCenterY();

        AffineTransform rotateTransform = new AffineTransform();
        rotateTransform.translate(centerX, centerY);
        rotateTransform.rotate(angle);
        rotateTransform.translate(-centerX, -centerY);

        this.transform.preConcatenate(rotateTransform);
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

    // Draw handles for a selected element
    public void drawHandles(Graphics2D g2d) {
        Point2D[] handles = getHandlePositions();
        g2d.setColor(Color.BLACK);
        for (Point2D handle : handles) {
            g2d.fillRect((int)(handle.getX() - HANDLE_SIZE/2),
                    (int)(handle.getY() - HANDLE_SIZE/2),
                    HANDLE_SIZE, HANDLE_SIZE);
        }

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

        return new Point2D[] {
                cornerPoints[0], // Top-left
                cornerPoints[1], // Top-right
                cornerPoints[2], // Bottom-right
                cornerPoints[3]  // Bottom-left
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

    // Transform an element by handle
    public void transformByHandle(int handleIndex, int dx, int dy) {
        Point2D[] handles = getHandlePositions();
        Point2D handle = handles[handleIndex];

        // Calculate center of the element
        Rectangle2D bounds = this.getBounds();
        double centerX = bounds.getCenterX();
        double centerY = bounds.getCenterY();

        // perform scaling and rotation
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
            AffineTransform newTransform = new AffineTransform();
            newTransform.translate(centerX, centerY);
            newTransform.rotate(angleChange);
            newTransform.scale(scaleFactor, scaleFactor);
            newTransform.translate(-centerX, -centerY);

            transform.preConcatenate(newTransform);
        }
    }


    public static PosterElement deserialize(String data) {
        if (data.startsWith("IMAGE:")) {
            return ImageElement.fromString(data);
        } else if (data.startsWith("RECTANGLE:")) {
            return RectangleElement.fromString(data);
        } else if (data.startsWith("CIRCLE:")) {
            return CircleElement.fromString(data);
        }
        return null;
    }

    // Serialize transform to string
    protected String serializeTransform() {
        double[] matrix = new double[6];
        transform.getMatrix(matrix);
        return String.format(Locale.US, "%.6f,%.6f,%.6f,%.6f,%.6f,%.6f",
                matrix[0], matrix[1], matrix[2],
                matrix[3], matrix[4], matrix[5]);
    }


    protected static AffineTransform parseTransform(String data) {
        String[] parts = data.split(",");
        if (parts.length != 6) {
            System.err.println("Invalid transform data: " + data);
            return new AffineTransform();
        }

        try {
            double[] matrix = new double[6];
            for (int i = 0; i < 6; i++) {
                matrix[i] = Double.parseDouble(parts[i]);
            }

            return new AffineTransform(
                    matrix[0], matrix[1], matrix[2],
                    matrix[3], matrix[4], matrix[5]
            );
        } catch (NumberFormatException e) {
            System.err.println("Error parsing transform: " + e.getMessage());
            e.printStackTrace();
            return new AffineTransform();
        }
    }

}

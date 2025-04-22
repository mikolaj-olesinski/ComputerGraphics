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
        Rectangle2D rect = new Rectangle2D.Double(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);
        return transform.createTransformedShape(rect).getBounds2D();
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
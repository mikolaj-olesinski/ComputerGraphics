import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// Rectangle shape implementation
public class RectangleElement extends ShapeElement {

    public RectangleElement(Color color) {
        super(color);
    }

    @Override
    public void draw(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();
        g2d.transform(transform);

        g2d.setColor(color);
        g2d.fillRect(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);

        g2d.setTransform(oldTransform);
    }

    @Override
    public boolean contains(Point p) {
        try {
            Point2D inversePt = new Point2D.Double();
            transform.inverseTransform(p, inversePt);
            return new Rectangle2D.Double(0, 0, DEFAULT_SIZE, DEFAULT_SIZE)
                    .contains(inversePt);
        } catch (NoninvertibleTransformException e) {
            return false;
        }
    }

    @Override
    public PosterElement clone() {
        RectangleElement clone = (RectangleElement) super.clone();
        clone.color = this.color;
        return clone;
    }
}
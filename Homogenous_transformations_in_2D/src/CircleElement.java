import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

// Circle shape implementation
public class CircleElement extends ShapeElement {

    public CircleElement(Color color) {
        super(color);
    }

    @Override
    public void draw(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();
        g2d.transform(transform);

        g2d.setColor(color);
        g2d.fillOval(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);

        g2d.setTransform(oldTransform);
    }

    @Override
    public boolean contains(Point p) {
        try {
            Point2D inversePt = new Point2D.Double();
            transform.inverseTransform(p, inversePt);
            return new Ellipse2D.Double(0, 0, DEFAULT_SIZE, DEFAULT_SIZE)
                    .contains(inversePt);
        } catch (NoninvertibleTransformException e) {
            return false;
        }
    }

    @Override
    public PosterElement clone() {
        CircleElement clone = (CircleElement) super.clone();
        clone.color = this.color;
        return clone;
    }
}
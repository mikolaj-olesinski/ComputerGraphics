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
        g2d.fillOval(0, 0, ShapeElement.DEFAULT_SIZE, ShapeElement.DEFAULT_SIZE);

        g2d.setTransform(oldTransform);
    }

    @Override
    public boolean contains(Point p) {
        try {
            Point2D inversePt = new Point2D.Double();
            transform.inverseTransform(p, inversePt);
            return new Ellipse2D.Double(0, 0, ShapeElement.DEFAULT_SIZE, ShapeElement.DEFAULT_SIZE)
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

    @Override
    public String serialize() {
        return "CIRCLE:" + colorToString(color) + ":" + serializeTransform();
    }

    public static CircleElement fromString(String data) {
        if (!data.startsWith("CIRCLE:")) {
            return null;
        }

        try {
            String[] parts = data.substring("CIRCLE:".length()).split(":", 2);
            if (parts.length != 2) {
                System.err.println("Invalid circle format: " + data);
                return null;
            }

            Color color = stringToColor(parts[0]);
            AffineTransform transform = parseTransform(parts[1]);

            CircleElement element = new CircleElement(color);
            element.transform = transform;  // Ensure transform is assigned properly
            return element;
        } catch (Exception e) {
            System.err.println("Error parsing circle: " + data);
            e.printStackTrace();
            return null;
        }
    }
}
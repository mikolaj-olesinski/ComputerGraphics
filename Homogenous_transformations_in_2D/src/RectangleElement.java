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

    @Override
    public String serialize() {
        return "RECTANGLE:" + colorToString(color) + ":" + serializeTransform();
    }

    public static RectangleElement fromString(String data) {
        if (!data.startsWith("RECTANGLE:")) {
            return null;
        }

        try {
            String[] parts = data.substring("RECTANGLE:".length()).split(":", 2);
            if (parts.length != 2) {
                System.err.println("Invalid rectangle format: " + data);
                return null;
            }

            Color color = stringToColor(parts[0]);
            AffineTransform transform = parseTransform(parts[1]);

            RectangleElement element = new RectangleElement(color);
            element.transform = transform;  // Ensure transform is assigned properly
            return element;
        } catch (Exception e) {
            System.err.println("Error parsing rectangle: " + data);
            e.printStackTrace();
            return null;
        }
    }
}
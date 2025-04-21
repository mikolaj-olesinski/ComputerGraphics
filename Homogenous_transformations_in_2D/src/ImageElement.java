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
        Rectangle2D rect = new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight());
        return transform.createTransformedShape(rect).getBounds2D();
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
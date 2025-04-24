import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
    public Point2D[] getCornerPoints() {

        Point2D[] points = new Point2D[] {
                new Point2D.Double(0, 0),                    // left upper
                new Point2D.Double(image.getWidth(), 0),     // right upper
                new Point2D.Double(image.getWidth(), image.getHeight()), // right lower
                new Point2D.Double(0, image.getHeight())     // left lower
        };

        // Transform the points using the current transform
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


    @Override
    public String save() {
        return "IMAGE:" + name + ":" + serializeTransform();
    }

    public static ImageElement fromString(String data) {
        if (!data.startsWith("IMAGE:")) {
            return null;
        }

        try {
            String[] parts = data.substring("IMAGE:".length()).split(":", 2);
            if (parts.length != 2) {
                System.err.println("Invalid image format: " + data);
                return null;
            }

            String imageName = parts[0];
            AffineTransform transform = parseTransform(parts[1]);

            // Load image from the images directory
            File imageFile = new File("images", imageName);
            BufferedImage image = ImageIO.read(imageFile);

            if (image == null) {
                System.err.println("Could not load image: " + imageName);
                return null;
            }

            ImageElement element = new ImageElement(image, imageName);
            element.transform = transform;

            return element;
        } catch (IOException e) {
            System.err.println("Cannot load image: " + data);
            e.printStackTrace();
            return null;
        }
    }
}

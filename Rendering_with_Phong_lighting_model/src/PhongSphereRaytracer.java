// Main class
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class PhongSphereRaytracer {
    public static void main(String[] args) {
        // Pokaż dialog wyboru pliku
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wybierz plik sceny");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Pliki tekstowe (*.txt)", "txt"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            System.out.println("Nie wybrano pliku.");
            return;
        }

        String sceneFile = fileChooser.getSelectedFile().getAbsolutePath();

        // Załaduj scenę i renderuj
        SceneLoader loader = new SceneLoader();
        Scene scene = loader.loadScene(sceneFile);

        if (scene == null) {
            System.out.println("Nie udało się załadować sceny z pliku: " + sceneFile);
            return;
        }

        Renderer renderer = new Renderer();
        BufferedImage image = renderer.render(scene);

        try {
            // Wyświetl obraz
            JFrame frame = new JFrame("Phong Sphere Raytracer");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.getContentPane().add(new ImagePanel(image));
            frame.pack();
            frame.setVisible(true);

            // Zapisz obraz
            File outputFile = new File(scene.outputFileName);
            String fileExtension = scene.outputFileName.substring(scene.outputFileName.lastIndexOf('.') + 1);
            ImageIO.write(image, fileExtension.toUpperCase(), outputFile);
            System.out.println("Obraz zapisany do: " + scene.outputFileName);
        } catch (Exception e) {
            System.out.println("Błąd podczas zapisywania obrazu: " + e.getMessage());
        }
    }
}

// Vector3 class
class Vector3 {
    double x, y, z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double dot(Vector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vector3 normalize() {
        double length = Math.sqrt(x * x + y * y + z * z);
        return new Vector3(x / length, y / length, z / length);
    }

    public Vector3 subtract(Vector3 v) {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    public Vector3 add(Vector3 v) {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }

    public Vector3 multiply(double scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }

    public Vector3 reflect(Vector3 normal) {
        double dotProduct = this.dot(normal);
        return this.subtract(normal.multiply(2 * dotProduct));
    }
}

// Ray class
class Ray {
    Vector3 origin;
    Vector3 direction;

    public Ray(Vector3 origin, Vector3 direction) {
        this.origin = origin;
        this.direction = direction.normalize();
    }
}

// Material class
class Material {
    Vector3 diffuseCoeff;  // kd (RGB)
    Vector3 specularCoeff; // ks (RGB)
    Vector3 ambientCoeff;  // ka (RGB)
    Vector3 selfLuminance; // S (RGB)
    double glossiness;     // g

    public Material(Vector3 diffuseCoeff, Vector3 specularCoeff, Vector3 ambientCoeff,
                    Vector3 selfLuminance, double glossiness) {
        this.diffuseCoeff = diffuseCoeff;
        this.specularCoeff = specularCoeff;
        this.ambientCoeff = ambientCoeff;
        this.selfLuminance = selfLuminance;
        this.glossiness = glossiness;
    }
}

// Sphere class
class Sphere {
    Vector3 center;
    double radius;
    Material material;

    public Sphere(Vector3 center, double radius, Material material) {
        this.center = center;
        this.radius = radius;
        this.material = material;
    }

    public boolean intersect(Ray ray, double[] t) {
        Vector3 oc = ray.origin.subtract(center);
        double a = ray.direction.dot(ray.direction);
        double b = 2.0 * oc.dot(ray.direction);
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) return false;

        double sqrtDiscriminant = Math.sqrt(discriminant);
        double t0 = (-b - sqrtDiscriminant) / (2 * a);
        double t1 = (-b + sqrtDiscriminant) / (2 * a);

        if (t0 > t1) {
            double temp = t0;
            t0 = t1;
            t1 = temp;
        }

        if (t0 < 0) {
            t0 = t1;
            if (t0 < 0) return false;
        }

        t[0] = t0;
        return true;
    }

    public Vector3 getNormalAt(Vector3 point) {
        return point.subtract(center).normalize();
    }
}

// PointLight class
class PointLight {
    Vector3 position;
    Vector3 intensity; // RGB

    public PointLight(Vector3 position, Vector3 intensity) {
        this.position = position;
        this.intensity = intensity;
    }
}

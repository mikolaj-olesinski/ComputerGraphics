import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Graphics;
import java.awt.Dimension;

public class PhongSphereRaytracer {

    static class Vector3 {
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

    static class Ray {
        Vector3 origin;
        Vector3 direction;

        public Ray(Vector3 origin, Vector3 direction) {
            this.origin = origin;
            this.direction = direction.normalize();
        }
    }

    static class Sphere {
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

            if (discriminant < 0) {
                return false;
            }

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
                if (t0 < 0) {
                    return false;
                }
            }

            t[0] = t0;
            return true;
        }

        public Vector3 getNormalAt(Vector3 point) {
            return point.subtract(center).normalize();
        }
    }

    static class PointLight {
        Vector3 position;
        Vector3 intensity; // RGB

        public PointLight(Vector3 position, Vector3 intensity) {
            this.position = position;
            this.intensity = intensity;
        }
    }

    static class Material {
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

    static class Scene {
        List<PointLight> lights;
        Sphere sphere;
        Vector3 ambientLight; // A (RGB)
        int imageWidth;
        int imageHeight;
        String outputFileName;

        public Scene() {
            lights = new ArrayList<>();
        }
    }

    static class ImagePanel extends JPanel {
        private BufferedImage image;

        public ImagePanel(BufferedImage image) {
            this.image = image;
            setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
        }
    }

    public static void main(String[] args) {
        String sceneFile = null;

        // If a file was provided as command line argument, use it
        if (args.length >= 1) {
            sceneFile = args[0];
        } else {
            // Otherwise show a file chooser dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Wybierz plik sceny");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Pliki tekstowe (*.txt)", "txt"));
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                sceneFile = fileChooser.getSelectedFile().getAbsolutePath();
            } else {
                System.out.println("Nie wybrano pliku.");
                return;
            }
        }

        Scene scene = loadScene(sceneFile);
        if (scene == null) {
            System.out.println("Nie udało się załadować sceny z pliku: " + sceneFile);
            return;
        }

        BufferedImage image = renderScene(scene);

        try {
            // Display the image
            JFrame frame = new JFrame("Phong Sphere Raytracer");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null); // Centrowanie okna
            frame.getContentPane().add(new ImagePanel(image));
            frame.pack();
            frame.setVisible(true);

            // Save the image
            File outputFile = new File(scene.outputFileName);
            String fileExtension = scene.outputFileName.substring(scene.outputFileName.lastIndexOf('.') + 1);
            ImageIO.write(image, fileExtension.toUpperCase(), outputFile);
            System.out.println("Obraz zapisany do: " + scene.outputFileName);
        } catch (Exception e) {
            System.out.println("Error saving image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Scene loadScene(String fileName) {
        Scene scene = new Scene();

        try (Scanner scanner = new Scanner(new File(fileName))) {
            scanner.useLocale(java.util.Locale.US); // Użyj kropki jako separatora dziesiętnego

            // Ignoruj linie komentarzy i puste linie aż do znalezienia liczby punktów światła
            int numLights = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                // Pomijaj komentarze i puste linie
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                // Znaleziono liczbę świateł
                try {
                    numLights = Integer.parseInt(line);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Błąd: nie można odczytać liczby świateł: " + line);
                    return null;
                }
            }

            // Wczytaj właściwości świateł
            for (int i = 0; i < numLights; i++) {
                double x = 0, y = 0, z = 0, r = 0, g = 0, b = 0;

                // Szukaj linii z danymi światła, pomijając komentarze
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    Scanner lineScanner = new Scanner(line);
                    lineScanner.useLocale(java.util.Locale.US);

                    try {
                        x = lineScanner.nextDouble();
                        y = lineScanner.nextDouble();
                        z = lineScanner.nextDouble();
                        r = lineScanner.nextDouble();
                        g = lineScanner.nextDouble();
                        b = lineScanner.nextDouble();
                        lineScanner.close();
                        break;
                    } catch (Exception e) {
                        System.out.println("Błąd: nieprawidłowe dane światła " + (i+1));
                        lineScanner.close();
                        return null;
                    }
                }

                scene.lights.add(new PointLight(
                        new Vector3(x, y, z),
                        new Vector3(r, g, b)
                ));
            }

            // Wczytaj właściwości kuli
            double centerX = 0, centerY = 0, centerZ = 0, radius = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                Scanner lineScanner = new Scanner(line);
                lineScanner.useLocale(java.util.Locale.US);

                try {
                    centerX = lineScanner.nextDouble();
                    centerY = lineScanner.nextDouble();
                    centerZ = lineScanner.nextDouble();
                    radius = lineScanner.nextDouble();
                    lineScanner.close();
                    break;
                } catch (Exception e) {
                    System.out.println("Błąd: nieprawidłowe dane kuli");
                    lineScanner.close();
                    return null;
                }
            }

            // Wczytaj współczynniki difuzji
            double kdR = 0, kdG = 0, kdB = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                Scanner lineScanner = new Scanner(line);
                lineScanner.useLocale(java.util.Locale.US);

                try {
                    kdR = lineScanner.nextDouble();
                    kdG = lineScanner.nextDouble();
                    kdB = lineScanner.nextDouble();
                    lineScanner.close();
                    break;
                } catch (Exception e) {
                    System.out.println("Błąd: nieprawidłowe współczynniki difuzji");
                    lineScanner.close();
                    return null;
                }
            }

            // Wczytaj współczynniki odbić lustrzanych
            double ksR = 0, ksG = 0, ksB = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                Scanner lineScanner = new Scanner(line);
                lineScanner.useLocale(java.util.Locale.US);

                try {
                    ksR = lineScanner.nextDouble();
                    ksG = lineScanner.nextDouble();
                    ksB = lineScanner.nextDouble();
                    lineScanner.close();
                    break;
                } catch (Exception e) {
                    System.out.println("Błąd: nieprawidłowe współczynniki odbić lustrzanych");
                    lineScanner.close();
                    return null;
                }
            }

            // Wczytaj współczynniki światła otoczenia
            double kaR = 0, kaG = 0, kaB = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                Scanner lineScanner = new Scanner(line);
                lineScanner.useLocale(java.util.Locale.US);

                try {
                    kaR = lineScanner.nextDouble();
                    kaG = lineScanner.nextDouble();
                    kaB = lineScanner.nextDouble();
                    lineScanner.close();
                    break;
                } catch (Exception e) {
                    System.out.println("Błąd: nieprawidłowe współczynniki światła otoczenia");
                    lineScanner.close();
                    return null;
                }
            }

            // Wczytaj własne świecenie
            double sR = 0, sG = 0, sB = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                Scanner lineScanner = new Scanner(line);
                lineScanner.useLocale(java.util.Locale.US);

                try {
                    sR = lineScanner.nextDouble();
                    sG = lineScanner.nextDouble();
                    sB = lineScanner.nextDouble();
                    lineScanner.close();
                    break;
                } catch (Exception e) {
                    System.out.println("Błąd: nieprawidłowe wartości własnego świecenia");
                    lineScanner.close();
                    return null;
                }
            }

            // Wczytaj połyskliwość
            double glossiness = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                Scanner lineScanner = new Scanner(line);
                lineScanner.useLocale(java.util.Locale.US);

                try {
                    glossiness = lineScanner.nextDouble();
                    lineScanner.close();
                    break;
                } catch (Exception e) {
                    System.out.println("Błąd: nieprawidłowa wartość połyskliwości");
                    lineScanner.close();
                    return null;
                }
            }

            Material material = new Material(
                    new Vector3(kdR, kdG, kdB),
                    new Vector3(ksR, ksG, ksB),
                    new Vector3(kaR, kaG, kaB),
                    new Vector3(sR, sG, sB),
                    glossiness
            );

            scene.sphere = new Sphere(new Vector3(centerX, centerY, centerZ), radius, material);

            // Wczytaj natężenie światła otoczenia
            double aR = 0, aG = 0, aB = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                Scanner lineScanner = new Scanner(line);
                lineScanner.useLocale(java.util.Locale.US);

                try {
                    aR = lineScanner.nextDouble();
                    aG = lineScanner.nextDouble();
                    aB = lineScanner.nextDouble();
                    lineScanner.close();
                    break;
                } catch (Exception e) {
                    System.out.println("Błąd: nieprawidłowe natężenie światła otoczenia");
                    lineScanner.close();
                    return null;
                }
            }
            scene.ambientLight = new Vector3(aR, aG, aB);

            // Wczytaj rozdzielczość obrazu
            int width = 0, height = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                Scanner lineScanner = new Scanner(line);

                try {
                    width = lineScanner.nextInt();
                    height = lineScanner.nextInt();
                    lineScanner.close();
                    break;
                } catch (Exception e) {
                    System.out.println("Błąd: nieprawidłowa rozdzielczość obrazu");
                    lineScanner.close();
                    return null;
                }
            }
            scene.imageWidth = width;
            scene.imageHeight = height;

            // Wczytaj nazwę pliku wyjściowego
            String outputFileName = "";
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                outputFileName = line;
                break;
            }
            scene.outputFileName = outputFileName;

            return scene;
        } catch (FileNotFoundException e) {
            System.out.println("Nie znaleziono pliku sceny: " + fileName);
            return null;
        } catch (Exception e) {
            System.out.println("Błąd podczas odczytu pliku sceny: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static BufferedImage renderScene(Scene scene) {
        BufferedImage image = new BufferedImage(scene.imageWidth, scene.imageHeight, BufferedImage.TYPE_INT_RGB);

        double aspectRatio = (double) scene.imageWidth / scene.imageHeight;
        double viewportHeight = 2.0;
        double viewportWidth = aspectRatio * viewportHeight;

        Vector3 origin = new Vector3(0, 0, -3);  // Camera at origin, looking in positive z direction

        for (int y = 0; y < scene.imageHeight; y++) {
            for (int x = 0; x < scene.imageWidth; x++) {
                // Map pixel to world space
                double u = (x + 0.5) / scene.imageWidth;
                double v = (y + 0.5) / scene.imageHeight;

                // Convert to view space coordinates
                double worldX = -viewportWidth/2 + u * viewportWidth;
                double worldY = viewportHeight/2 - v * viewportHeight;

                // For parallel rays, all rays have the same direction
                Vector3 direction = new Vector3(0, 0, 1).normalize();

                // Calculate ray starting point
                Vector3 rayOrigin = new Vector3(worldX, worldY, -scene.sphere.radius);

                Ray ray = new Ray(rayOrigin, direction);
                Vector3 color = traceRay(ray, scene);

                // Clamp color values to [0, 1]
                color = new Vector3(
                        Math.min(1, Math.max(0, color.x)),
                        Math.min(1, Math.max(0, color.y)),
                        Math.min(1, Math.max(0, color.z))
                );

                // Convert color to RGB int
                int r = (int) (color.x * 255);
                int g = (int) (color.y * 255);
                int b = (int) (color.z * 255);
                int rgb = (r << 16) | (g << 8) | b;

                image.setRGB(x, y, rgb);
            }
        }

        return image;
    }

    private static Vector3 traceRay(Ray ray, Scene scene) {
        double[] t = new double[1];

        if (scene.sphere.intersect(ray, t)) {
            // Calculate intersection point
            Vector3 intersectionPoint = ray.origin.add(ray.direction.multiply(t[0]));

            // Calculate normal at intersection point
            Vector3 normal = scene.sphere.getNormalAt(intersectionPoint);

            // Get material properties
            Material material = scene.sphere.material;

            // Calculate view direction (from intersection point to camera)
            Vector3 viewDir = ray.origin.subtract(intersectionPoint).normalize();

            // Initialize with self-luminance
            Vector3 color = material.selfLuminance;

            // Add ambient light contribution
            color = color.add(new Vector3(
                    material.ambientCoeff.x * scene.ambientLight.x,
                    material.ambientCoeff.y * scene.ambientLight.y,
                    material.ambientCoeff.z * scene.ambientLight.z
            ));

            // Add contribution from each light source
            for (PointLight light : scene.lights) {
                // Calculate light direction
                Vector3 lightDir = light.position.subtract(intersectionPoint).normalize();

                // Distance to light
                double distanceToLight = light.position.subtract(intersectionPoint).dot(light.position.subtract(intersectionPoint));
                distanceToLight = Math.sqrt(distanceToLight);

                // Calculate light attenuation (simplified version)
                double c0 = 1.0;
                double c1 = 0.1;
                double c2 = 0.01;
                double attenuation = Math.min(1.0, 1.0 / (c0 + c1 * distanceToLight + c2 * distanceToLight * distanceToLight));

                // Diffuse reflection
                double diffuseFactor = Math.max(0, normal.dot(lightDir));

                // Specular reflection
                Vector3 reflectedLight = lightDir.multiply(-1).reflect(normal);
                double specularFactor = Math.pow(Math.max(0, reflectedLight.dot(viewDir)), material.glossiness);

                // Add diffuse component
                color = color.add(new Vector3(
                        material.diffuseCoeff.x * light.intensity.x * diffuseFactor * attenuation,
                        material.diffuseCoeff.y * light.intensity.y * diffuseFactor * attenuation,
                        material.diffuseCoeff.z * light.intensity.z * diffuseFactor * attenuation
                ));

                // Add specular component
                color = color.add(new Vector3(
                        material.specularCoeff.x * light.intensity.x * specularFactor * attenuation,
                        material.specularCoeff.y * light.intensity.y * specularFactor * attenuation,
                        material.specularCoeff.z * light.intensity.z * specularFactor * attenuation
                ));
            }

            return color;
        }

        // No intersection, return black (background color)
        return new Vector3(0, 0, 0);
    }
}
// SceneLoader class
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


// Scene class
class Scene {
    java.util.List<PointLight> lights = new java.util.ArrayList<>();
    Sphere sphere;
    Vector3 ambientLight;
    int imageWidth;
    int imageHeight;
    String outputFileName;
}


class SceneLoader {
    public Scene loadScene(String fileName) {
        Scene scene = new Scene();

        try (Scanner scanner = new Scanner(new File(fileName))) {
            scanner.useLocale(java.util.Locale.US);

            // Find number of lights
            int numLights = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                numLights = Integer.parseInt(line);
                break;
            }

            // Load lights
            for (int i = 0; i < numLights; i++) {
                double[] values = readNextDoubles(scanner, 6);
                scene.lights.add(new PointLight(
                        new Vector3(values[0], values[1], values[2]),
                        new Vector3(values[3], values[4], values[5])
                ));
            }

            //Load sphere properties
            double[] sphereValues = readNextDoubles(scanner, 1);

            //Load material properties
            double[] kd = readNextDoubles(scanner, 3);
            double[] ks = readNextDoubles(scanner, 3);
            double[] ka = readNextDoubles(scanner, 3);
            double[] s = readNextDoubles(scanner, 3);
            double glossiness = readNextDoubles(scanner, 1)[0];

            Material material = new Material(
                    new Vector3(kd[0], kd[1], kd[2]),
                    new Vector3(ks[0], ks[1], ks[2]),
                    new Vector3(ka[0], ka[1], ka[2]),
                    new Vector3(s[0], s[1], s[2]),
                    glossiness
            );

            scene.sphere = new Sphere(
                    sphereValues[0],
                    material
            );

            //Load ambient light
            double[] ambient = readNextDoubles(scanner, 3);
            scene.ambientLight = new Vector3(ambient[0], ambient[1], ambient[2]);

            // Load resolution
            int[] resolution = readNextInts(scanner, 2);
            scene.imageWidth = resolution[0];
            scene.imageHeight = resolution[1];

            // Load output file name
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                scene.outputFileName = line;
                break;
            }

            return scene;
        } catch (FileNotFoundException e) {
            System.out.println("Nie znaleziono pliku sceny: " + fileName);
            return null;
        } catch (Exception e) {
            System.out.println("Błąd podczas odczytu pliku sceny: " + e.getMessage());
            return null;
        }
    }

    private double[] readNextDoubles(Scanner scanner, int count) {
        double[] values = new double[count];
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            Scanner lineScanner = new Scanner(line);
            lineScanner.useLocale(java.util.Locale.US);

            try {
                for (int i = 0; i < count; i++) {
                    values[i] = lineScanner.nextDouble();
                }
                lineScanner.close();
                return values;
            } catch (Exception e) {
                lineScanner.close();
                throw new RuntimeException("Błąd podczas parsowania wartości");
            }
        }
        throw new RuntimeException("Koniec pliku przed przeczytaniem wszystkich wartości");
    }

    private int[] readNextInts(Scanner scanner, int count) {
        int[] values = new int[count];
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            Scanner lineScanner = new Scanner(line);

            try {
                for (int i = 0; i < count; i++) {
                    values[i] = lineScanner.nextInt();
                }
                lineScanner.close();
                return values;
            } catch (Exception e) {
                lineScanner.close();
                throw new RuntimeException("Błąd podczas parsowania wartości całkowitych");
            }
        }
        throw new RuntimeException("Koniec pliku przed przeczytaniem wszystkich wartości");
    }
}

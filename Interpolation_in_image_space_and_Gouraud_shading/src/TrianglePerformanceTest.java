import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrianglePerformanceTest {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int NUM_TRIANGLES = 1000;
    private static final int NUM_RUNS = 5;

    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        List<Triangle2D> triangles = generateRandomTriangles(NUM_TRIANGLES);

        System.out.println("=== Test wydajności cieniowania Gourauda ===");
        System.out.println("Liczba trójkątów: " + NUM_TRIANGLES);
        System.out.println("Liczba powtórzeń: " + NUM_RUNS);
        System.out.println();

        // Test standardowej metody
        System.out.println("Metoda standardowa (drawGouraudToImage):");
        long[] standardTimes = new long[NUM_RUNS];
        for (int run = 0; run < NUM_RUNS; run++) {
            clearImage(image);
            long startTime = System.nanoTime();

            for (Triangle2D triangle : triangles) {
                triangle.drawGouraudToImage(image);
            }

            long endTime = System.nanoTime();
            long elapsedTime = (endTime - startTime) / 1_000_000; // w milisekundach
            standardTimes[run] = elapsedTime;
            System.out.println("Przebieg " + (run + 1) + ": " + elapsedTime + " ms");
        }
        double standardAvg = calculateAverage(standardTimes);
        System.out.println("Średni czas: " + standardAvg + " ms");
        System.out.println();

        // Test zoptymalizowanej metody
        System.out.println("Metoda zoptymalizowana (drawGouraudToImageOptimized):");
        long[] optimizedTimes = new long[NUM_RUNS];
        for (int run = 0; run < NUM_RUNS; run++) {
            clearImage(image);
            long startTime = System.nanoTime();

            for (Triangle2D triangle : triangles) {
                triangle.drawGouraudToImageOptimized(image);
            }

            long endTime = System.nanoTime();
            long elapsedTime = (endTime - startTime) / 1_000_000; // w milisekundach
            optimizedTimes[run] = elapsedTime;
            System.out.println("Przebieg " + (run + 1) + ": " + elapsedTime + " ms");
        }
        double optimizedAvg = calculateAverage(optimizedTimes);
        System.out.println("Średni czas: " + optimizedAvg + " ms");
        System.out.println();

        // Podsumowanie
        double speedup = standardAvg / optimizedAvg;
        System.out.println("=== Podsumowanie ===");
        System.out.println("Metoda standardowa: " + standardAvg + " ms");
        System.out.println("Metoda zoptymalizowana: " + optimizedAvg + " ms");
        System.out.println("Przyspieszenie: " + speedup + "x");
    }

    private static List<Triangle2D> generateRandomTriangles(int count) {
        List<Triangle2D> triangles = new ArrayList<>();
        Random random = new Random(42); // Stały seed dla powtarzalności

        for (int i = 0; i < count; i++) {
            // Losowe współrzędne w granicach obrazu
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            int x3 = random.nextInt(WIDTH);
            int y3 = random.nextInt(HEIGHT);

            // Losowe kolory
            Color color1 = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            Color color2 = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            Color color3 = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));

            triangles.add(new Triangle2D(x1, y1, x2, y2, x3, y3, color1, color2, color3));
        }

        return triangles;
    }

    private static void clearImage(BufferedImage image) {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.dispose();
    }

    private static double calculateAverage(long[] times) {
        double sum = 0;
        for (long time : times) {
            sum += time;
        }
        return sum / times.length;
    }
}
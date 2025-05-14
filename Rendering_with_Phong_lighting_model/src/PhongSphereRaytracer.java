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

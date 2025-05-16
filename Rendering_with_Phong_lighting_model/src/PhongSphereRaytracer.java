import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class PhongSphereRaytracer {
    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wybierz plik sceny");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Pliki tekstowe (*.txt)", "txt"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            System.out.println("Nie wybrano pliku.");
            return;
        }

        File sceneFile = fileChooser.getSelectedFile();

        JFrame frame = new JFrame("Phong Sphere Raytracer");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        ImagePanel imagePanel = new ImagePanel(renderScene(sceneFile.getAbsolutePath()));
        frame.add(imagePanel, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Odśwież");
        refreshButton.addActionListener(e -> {
            BufferedImage newImage = renderScene(sceneFile.getAbsolutePath());
            imagePanel.setImage(newImage);
            imagePanel.repaint();
        });
        frame.add(refreshButton, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static BufferedImage renderScene(String sceneFile) {
        SceneLoader loader = new SceneLoader();
        Scene scene = loader.loadScene(sceneFile);

        if (scene == null) {
            System.out.println("Nie udało się załadować sceny z pliku: " + sceneFile);
            return null;
        }

        Renderer renderer = new Renderer();
        BufferedImage image = renderer.render(scene);

        try {
            File outputFile = new File(scene.outputFileName);
            String fileExtension = scene.outputFileName.substring(scene.outputFileName.lastIndexOf('.') + 1);
            ImageIO.write(image, fileExtension.toUpperCase(), outputFile);
            System.out.println("Obraz zapisany do: " + scene.outputFileName);
        } catch (Exception e) {
            System.out.println("Błąd podczas zapisywania obrazu: " + e.getMessage());
        }

        return image;
    }
}

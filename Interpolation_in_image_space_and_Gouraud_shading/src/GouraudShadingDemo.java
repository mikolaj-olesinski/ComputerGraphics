import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GouraudShadingDemo extends JPanel {
    private static final long serialVersionUID = 1L;

    // Przykładowe trójkąty
    private Triangle2D triangle1;
    private Triangle2D triangle2;
    private BufferedImage canvasImage;

    public GouraudShadingDemo() {
        setPreferredSize(new Dimension(800, 600));

        // Inicjalizacja obrazu do cieniowania
        canvasImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics g = canvasImage.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 800, 600);
        g.dispose();

        // Definicja pierwszego trójkąta (podobny do przykładu z materiałów)
        int[] x1 = {400, 200, 600};
        int[] y1 = {100, 400, 400};
        Color[] colors1 = {Color.RED, Color.GREEN, Color.PINK};
        triangle1 = new Triangle2D(x1, y1, colors1);

        // Definicja drugiego trójkąta (o dowolnym kształcie)
        int[] x2 = {150, 350, 100};
        int[] y2 = {450, 500, 550};
        Color[] colors2 = {Color.YELLOW, Color.MAGENTA, Color.CYAN};
        triangle2 = new Triangle2D(x2, y2, colors2);

        // Cieniowanie pierwszego trójkąta na obrazie
        triangle1.gouraudShadeToImage(canvasImage);

        // Zapisanie obrazu z wynikiem
        try {
            ImageIO.write(canvasImage, "PNG", new File("triangle_shaded.png"));
            System.out.println("Zapisano wynik cieniowania do pliku triangle_shaded.png");
        } catch (IOException e) {
            System.err.println("Błąd podczas zapisywania obrazu: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Wyświetlenie obrazu z pierwszym trójkątem
        g.drawImage(canvasImage, 0, 0, null);

        // Cieniowanie drugiego trójkąta bezpośrednio na ekranie
        triangle2.gouraudShadeToScreen(g);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Demonstracja cieniowania Gourauda");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new GouraudShadingDemo());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InteractiveTriangleDemo extends JPanel {
    private BufferedImage image;
    private List<Triangle2D> triangles;
    private List<Point> currentPoints;
    private Random random;

    public InteractiveTriangleDemo() {
        setPreferredSize(new Dimension(800, 600));
        image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        triangles = new ArrayList<>();
        currentPoints = new ArrayList<>();
        random = new Random();

        // Wypełnienie tła
        clearImage();

        // Dodanie obsługi zdarzeń myszy
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });

        // Dodanie obsługi klawisza C do czyszczenia ekranu
        setFocusable(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyChar() == 'c' || e.getKeyChar() == 'C') {
                    clearAll();
                }
            }
        });
    }

    private void clearImage() {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.dispose();
    }

    private void clearAll() {
        triangles.clear();
        currentPoints.clear();
        clearImage();
        repaint();
    }

    private void handleMouseClick(int x, int y) {
        // Dodaj nowy punkt do listy
        currentPoints.add(new Point(x, y));

        // Jeśli mamy już 3 punkty, utwórz nowy trójkąt
        if (currentPoints.size() == 3) {
            int[] xPoints = new int[3];
            int[] yPoints = new int[3];
            Color[] colors = new Color[3];

            // Wypełnianie współrzędnych i losowych kolorów
            for (int i = 0; i < 3; i++) {
                xPoints[i] = (int) currentPoints.get(i).getX();
                yPoints[i] = (int) currentPoints.get(i).getY();
                colors[i] = generateRandomColor();
            }

            // Tworzenie nowego trójkąta
            Triangle2D triangle = new Triangle2D(xPoints, yPoints, colors);
            triangles.add(triangle);

            // Rysowanie trójkąta na obrazie
            triangle.gouraudShadeToImage(image);

            // Czyszczenie listy punktów na kolejny trójkąt
            currentPoints.clear();

            // Odświeżenie panelu
            repaint();
        } else {
            // Jeśli jeszcze nie mamy 3 punktów, po prostu odświeżamy, aby pokazać punkty
            repaint();
        }
    }

    private Color generateRandomColor() {
        return new Color(
                random.nextInt(256),  // R
                random.nextInt(256),  // G
                random.nextInt(256)   // B
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Rysowanie obrazu
        g.drawImage(image, 0, 0, this);

        // Rysowanie aktualnie zbieranych punktów (jako małe kółka)
        g.setColor(Color.WHITE);
        for (Point p : currentPoints) {
            g.fillOval((int)p.getX() - 3, (int)p.getY() - 3, 6, 6);
        }

        // Jeśli mamy już 2 punkty, rysujemy linię między nimi
        if (currentPoints.size() >= 2) {
            g.setColor(Color.WHITE);
            for (int i = 0; i < currentPoints.size() - 1; i++) {
                Point p1 = currentPoints.get(i);
                Point p2 = currentPoints.get(i + 1);
                g.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
            }
        }

        // Wyświetlanie instrukcji
        g.setColor(Color. WHITE);
        g.drawString("Kliknij w trzech różnych miejscach, aby utworzyć trójkąt", 10, 20);
        g.drawString("Naciśnij 'C', aby wyczyścić ekran", 10, 40);
        g.drawString("Liczba zebranych punktów: " + currentPoints.size() + "/3", 10, 60);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Interaktywne tworzenie trójkątów");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            InteractiveTriangleDemo panel = new InteractiveTriangleDemo();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Ustawienie focusu na panel, aby działały klawisze
            panel.requestFocusInWindow();
        });
    }
}
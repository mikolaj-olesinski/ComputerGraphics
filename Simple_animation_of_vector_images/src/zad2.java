import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class zad2 {
    public static void main(String[] args) {
        // Utwórz okno układu słonecznego
        SolarSystemWindow window = new SolarSystemWindow();

        // Zamknięcie okna kończy program
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Ustaw początkową pozycję okna na ekranie
        // i uczyń okno widocznym
        window.setBounds(50, 50, 800, 600);
        window.setVisible(true);

        // Uruchom nieskończoną pętlę animacji
        // Program będzie działał, dopóki okno układu słonecznego nie zostanie zamknięte
        Timer timer = new Timer(40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.repaint();
            }
        });
        timer.start();
    }
}

// ===============================================================
// SolarSystemPane - klasa implementująca panel zawartości okna
// w którym wyświetlany jest układ słoneczny
// ===============================================================

class SolarSystemPane extends JPanel {
    // Współrzędne środka układu (Słońca)
    private int centerX, centerY;

    // Okres orbitalny planet (w milisekundach)
    private final long MERCURY_PERIOD = 2400;
    private final long VENUS_PERIOD = 6200;
    private final long EARTH_PERIOD = 10000;
    private final long MARS_PERIOD = 18800;

    // Okres orbitalny księżyca wokół Ziemi
    private final long MOON_PERIOD = 2700;

    // Promienie orbit planet
    private final int MERCURY_RADIUS = 50;
    private final int VENUS_RADIUS = 80;
    private final int EARTH_RADIUS = 120;
    private final int MARS_RADIUS = 180;

    // Promień orbity księżyca wokół Ziemi
    private final int MOON_RADIUS = 20;

    // Rozmiary planet (średnice)
    private final int SUN_SIZE = 30;
    private final int MERCURY_SIZE = 6;
    private final int VENUS_SIZE = 10;
    private final int EARTH_SIZE = 12;
    private final int MARS_SIZE = 8;
    private final int MOON_SIZE = 4;

    // Czas początkowy (do obliczania kąta orbitalnego)
    private final long startTime;

    SolarSystemPane() {
        super();
        setBackground(Color.BLACK);
        startTime = System.currentTimeMillis();
    }

    // Oblicza pozycję obiektu na orbicie
    private Point calculatePosition(int centerX, int centerY, int radius, double angle) {
        int x = (int) (centerX + radius * Math.cos(angle));
        int y = (int) (centerY + radius * Math.sin(angle));
        return new Point(x, y);
    }

    // Rysuje orbitę (okrąg)
    private void drawOrbit(Graphics g, int centerX, int centerY, int radius) {
        g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    // Rysuje ciało niebieskie (planetę lub księżyc)
    private void drawCelestialBody(Graphics g, Point position, int size, Color color) {
        g.setColor(color);
        g.fillOval(position.x - size/2, position.y - size/2, size, size);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Włącz antyaliasing dla lepszej jakości grafiki
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Pobierz aktualny rozmiar okna
        Dimension size = getSize();

        // Oblicz środek układu
        centerX = size.width / 2;
        centerY = size.height / 2;

        // Oblicz aktualny czas od startu
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        // Oblicz kąty dla każdej planety
        double mercuryAngle = (2 * Math.PI * elapsedTime) / MERCURY_PERIOD;
        double venusAngle = (2 * Math.PI * elapsedTime) / VENUS_PERIOD;
        double earthAngle = (2 * Math.PI * elapsedTime) / EARTH_PERIOD;
        double marsAngle = (2 * Math.PI * elapsedTime) / MARS_PERIOD;
        double moonAngle = (2 * Math.PI * elapsedTime) / MOON_PERIOD;

        // Narysuj orbity planet
        g2d.setColor(new Color(100, 100, 100, 70));
        drawOrbit(g2d, centerX, centerY, MERCURY_RADIUS);
        drawOrbit(g2d, centerX, centerY, VENUS_RADIUS);
        drawOrbit(g2d, centerX, centerY, EARTH_RADIUS);
        drawOrbit(g2d, centerX, centerY, MARS_RADIUS);

        // Oblicz pozycje planet
        Point mercuryPos = calculatePosition(centerX, centerY, MERCURY_RADIUS, mercuryAngle);
        Point venusPos = calculatePosition(centerX, centerY, VENUS_RADIUS, venusAngle);
        Point earthPos = calculatePosition(centerX, centerY, EARTH_RADIUS, earthAngle);
        Point marsPos = calculatePosition(centerX, centerY, MARS_RADIUS, marsAngle);

        // Narysuj orbitę księżyca wokół Ziemi
        g2d.setColor(new Color(100, 100, 100, 40));
        drawOrbit(g2d, earthPos.x, earthPos.y, MOON_RADIUS);

        // Oblicz pozycję księżyca względem Ziemi
        Point moonPos = calculatePosition(earthPos.x, earthPos.y, MOON_RADIUS, moonAngle);

        // Narysuj Słońce
        drawCelestialBody(g2d, new Point(centerX, centerY), SUN_SIZE, Color.YELLOW);

        // Narysuj planety
        drawCelestialBody(g2d, mercuryPos, MERCURY_SIZE, new Color(200, 200, 200)); // Merkury - szary
        drawCelestialBody(g2d, venusPos, VENUS_SIZE, new Color(255, 190, 100));    // Wenus - pomarańczowy
        drawCelestialBody(g2d, earthPos, EARTH_SIZE, new Color(50, 130, 255));      // Ziemia - niebieska
        drawCelestialBody(g2d, marsPos, MARS_SIZE, new Color(255, 80, 30));         // Mars - czerwony

        // Narysuj księżyc
        drawCelestialBody(g2d, moonPos, MOON_SIZE, new Color(200, 200, 200));      // Księżyc - szary

        // Dodaj informację o planetach
        g2d.setColor(Color.WHITE);
        g2d.drawString("Merkury", 10, 20);
        g2d.drawString("Wenus", 10, 40);
        g2d.drawString("Ziemia", 10, 60);
        g2d.drawString("Mars", 10, 80);
        g2d.drawString("Księżyc", 10, 100);
    }
}

// ==============================================================
// SolarSystemWindow - klasa implementująca okno zawierające
// animowany układ słoneczny
// ==============================================================

class SolarSystemWindow extends JFrame {
    public SolarSystemWindow() {
        setContentPane(new SolarSystemPane());
        setTitle("Układ Słoneczny");
    }
}
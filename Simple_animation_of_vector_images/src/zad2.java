import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class zad2 {
    public static void main(String[] args) {
        // Create the window of the solar system
        SolarSystemWindow window = new SolarSystemWindow();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setBounds(50, 50, 1000, 800);
        window.setVisible(true);

        // Start the infinite loop of animation.
        while (true) {
            try {
                // Wait x milliseconds before the system is redisplayed
                Thread.sleep(5);
            } catch (InterruptedException e) {
                System.out.println("Program interrupted");
            }
            // Redraw the system
            window.repaint();
        }
    }
}

// ===============================================================
// CelestialBody - represents a generic celestial object
// ===============================================================
abstract class CelestialBody {
    protected final String name;
    protected final int size; // diameter of the body
    protected final int orbitRadius;
    protected final double period; //period of the planet in seconds
    protected final Color color;
    protected Point position; // Current position of the celestial body
    protected final double initialAngle;


    public CelestialBody(String name, int size, int orbitRadius, double realPeriod, Color color) {
        this.name = name;
        this.size = size;
        this.orbitRadius = orbitRadius;
        this.period = realPeriod;
        this.color = color;
        this.position = new Point(0, 0); // Initialize position
        this.initialAngle = new Random().nextDouble() * 2 * Math.PI;
    }

    public int getSize() { return size; }
    public int getOrbitRadius() { return orbitRadius; }
    public double getPeriod() { return period; }
    public Color getColor() { return color; }
    public String getName() { return name; }
    public Point getPosition() { return position; }
    public void setPosition(Point position) { this.position = position; }

    protected void drawCelestialBody(Graphics g) {
        Color originalColor = g.getColor();
        g.setColor(color);
        g.fillOval(position.x - size/2, position.y - size/2, size, size);
        g.setColor(originalColor);
    }

    protected void drawOrbit(Graphics g, int centerX, int centerY) {
        g.drawOval(centerX - orbitRadius, centerY - orbitRadius, orbitRadius * 2, orbitRadius * 2);
    }
}

// ===============================================================
// Sun - represents the sun in the solar system
// ===============================================================
class Sun extends CelestialBody {
    public Sun(String name, int size, Color color) {
        // Sun has no orbit and no period as it's stationary at the center
        super(name, size, 0, 0, color);
    }

    // No need to draw orbit for Sun
    @Override
    protected void drawOrbit(Graphics g, int centerX, int centerY) {
        // Sun doesn't have an orbit
    }
}

// ===============================================================
// Planet - represents a planet in the solar system
// ===============================================================
class Planet extends CelestialBody {
    public Planet(String name, int size, int orbitRadius, double realPeriod, Color color) {
        super(name, size, orbitRadius, realPeriod, color);
    }

    protected void drawPlanetWithRing(Graphics g) {
        // Draw the planet
        drawCelestialBody(g);
        Color originalColor = g.getColor();

        // Draw the ring
        g.setColor(new Color(200, 200, 200, 100));
        g.drawOval(position.x - size, position.y - size / 4, size * 2, size / 2);
        g.setColor(originalColor);
    }
}

// ===============================================================
// Moon - represents a moon orbiting a planet
// ===============================================================
class Moon extends CelestialBody {
    private final Planet parentPlanet;

    public Moon(String name, int size, int orbitRadius, double realPeriod, Color color, Planet parentPlanet) {
        super(name, size, orbitRadius, realPeriod, color);
        this.parentPlanet = parentPlanet;
    }

    public Planet getParentPlanet() {
        return parentPlanet;
    }

    protected void drawOrbit(Graphics g) {
        Point parentPos = parentPlanet.getPosition();
        g.drawOval(parentPos.x - orbitRadius, parentPos.y - orbitRadius, orbitRadius * 2, orbitRadius * 2);
    }
}

// ===============================================================
// SolarSystemPane - implements the content pane of the window
// in which the animated solar system is displayed
// ===============================================================
class SolarSystemPane extends JPanel {

    private double timeScale = 2_000_000;
    private final Color orbitColor = new Color(100, 100, 100, 70);
    private final long startTime;
    private final JLabel speedLabel;
    private final Sun sun;
    private final Planet[] planets;
    private final Moon[] moons;

    public SolarSystemPane() {
        setBackground(Color.BLACK);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        startTime = System.currentTimeMillis();

        // Initialize sun
        sun = new Sun("Sun", 30, Color.YELLOW);

        // Initialize celestial bodies
        planets = new Planet[]{
                new Planet("Mercury", 6, 50, 7600530.0, new Color(200, 200, 200)),
                new Planet("Venus", 10, 80, 19414166.0, new Color(255, 190, 100)),
                new Planet("Earth", 12, 120, 31558149.0, new Color(50, 130, 255)),
                new Planet("Mars", 8, 180, 59355036.0, new Color(255, 80, 30)),
                new Planet("Jupiter", 24, 260, 374335776.0, new Color(255, 200, 150)),
                new Planet("Saturn", 22, 320, 929596608.0, new Color(230, 220, 130)),
                new Planet("Uranus", 16, 370, 2651370019.0, new Color(180, 230, 230)),
                new Planet("Neptune", 16, 420, 5200418560.0, new Color(100, 150, 255))
        };

        moons = new Moon[]{
                new Moon("Moon", 4, 20, 2360591.0, new Color(200, 200, 200), planets[2])
        };

        // Add controls to change speed
        JButton speedUpButton = new JButton("Increase Speed");
        JButton slowDownButton = new JButton("Decrease Speed");
        speedLabel = new JLabel("Time acceleration: " + (int) timeScale + "x");
        speedLabel.setForeground(Color.WHITE);

        add(speedUpButton);
        add(slowDownButton);
        add(speedLabel);

        speedUpButton.addActionListener(e -> {
            timeScale *= 2;
            updateSpeedLabel();
        });

        slowDownButton.addActionListener(e -> {
            timeScale = Math.max(1, timeScale / 2);
            updateSpeedLabel();
        });
    }

    private void updateSpeedLabel() {
        speedLabel.setText("Time acceleration: " + (int) timeScale + "x");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        long elapsedTime = System.currentTimeMillis() - startTime;

        Dimension size = getSize();
        int centerX = size.width / 2;
        int centerY = size.height / 2;

        // Set sun position at the center
        sun.setPosition(new Point(centerX, centerY));

        // Draw sun
        sun.drawCelestialBody(g2d);

        g2d.setColor(orbitColor);
        // Update and draw planets and orbits
        for (Planet planet : planets) {
            // Draw planet's orbit
            planet.drawOrbit(g2d, centerX, centerY);

            // Calculate and set planet position
            Point position = calculatePosition(centerX, centerY, planet.getOrbitRadius(), calculateAngle(elapsedTime, planet.getPeriod(), timeScale, planet.initialAngle));
            planet.setPosition(position);

            // Draw the planet
            if (planet.getName().equals("Saturn")) {
                planet.drawPlanetWithRing(g2d);
            } else {
                planet.drawCelestialBody(g2d);
            }
        }

        // Draw moons
        g2d.setColor(orbitColor);
        for (Moon moon : moons) {
            //Get parent planet position
            Planet parent = moon.getParentPlanet();
            Point parentPosition = parent.getPosition();

            // Draw moon's orbit
            moon.drawOrbit(g2d);

            // Calculate and set moon position
            Point moonPosition = calculatePosition(parentPosition.x, parentPosition.y, moon.getOrbitRadius(), calculateAngle(elapsedTime, moon.getPeriod(), timeScale, moon.initialAngle));
            moon.setPosition(moonPosition);

            // Draw the moon
            moon.drawCelestialBody(g2d);
        }
    }

    private Point calculatePosition(int centerX, int centerY, int radius, double angle) {
        int x = (int) (centerX + radius * Math.cos(angle));
        int y = (int) (centerY + radius * Math.sin(angle));
        return new Point(x, y);
    }

    public double calculateAngle(long elapsedTime, double period, double timeScale, double initialAngle) {
        return (2 * Math.PI * elapsedTime) / (period * 1000 / timeScale) + initialAngle;
    }
}

// ==============================================================
// SolarSystemWindow - class implementing the window containing
// the animated solar system
// ==============================================================
class SolarSystemWindow extends JFrame {
    public SolarSystemWindow() {
        setContentPane(new SolarSystemPane());
        setTitle("Solar System");
    }
}
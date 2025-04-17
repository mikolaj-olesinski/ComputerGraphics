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
    protected final double baseSize; // base diameter of the body
    protected final double baseOrbitRadius;
    protected final double period; //period of the planet in seconds
    protected final Color color;
    protected Point position; // Current position of the celestial body
    protected final double initialAngle;
    protected int scaledSize; // Scaled size based on window dimensions
    protected int scaledOrbitRadius; // Scaled orbit radius based on window dimensions

    public CelestialBody(String name, double baseSize, double baseOrbitRadius, double realPeriod, Color color) {
        this.name = name;
        this.baseSize = baseSize;
        this.baseOrbitRadius = baseOrbitRadius;
        this.period = realPeriod;
        this.color = color;
        this.position = new Point(0, 0); // Initialize position
        this.initialAngle = new Random().nextDouble() * 2 * Math.PI;
        this.scaledSize = (int)baseSize;
        this.scaledOrbitRadius = (int)baseOrbitRadius;
    }

    public int getSize() { return scaledSize; }
    public int getOrbitRadius() { return scaledOrbitRadius; }
    public double getPeriod() { return period; }
    public Color getColor() { return color; }
    public String getName() { return name; }
    public Point getPosition() { return position; }
    public void setPosition(Point position) { this.position = position; }

    // Scale the size and orbit radius based on the window dimensions
    public void scale(double scaleFactor) {
        this.scaledSize = Math.max(1, (int)(baseSize * scaleFactor));
        this.scaledOrbitRadius = Math.max(1, (int)(baseOrbitRadius * scaleFactor));
    }

    protected void drawCelestialBody(Graphics g) {
        Color originalColor = g.getColor();
        g.setColor(color);
        g.fillOval(position.x - scaledSize/2, position.y - scaledSize/2, scaledSize, scaledSize);
        g.setColor(originalColor);
    }

    protected void drawOrbit(Graphics g, int centerX, int centerY) {
        g.drawOval(centerX - scaledOrbitRadius, centerY - scaledOrbitRadius, scaledOrbitRadius * 2, scaledOrbitRadius * 2);
    }
}

// ===============================================================
// Sun - represents the sun in the solar system
// ===============================================================
class Sun extends CelestialBody {
    public Sun(String name, double baseSize, Color color) {
        // Sun has no orbit and no period as it's stationary at the center
        super(name, baseSize, 0, 0, color);
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
    public Planet(String name, double baseSize, double baseOrbitRadius, double realPeriod, Color color) {
        super(name, baseSize, baseOrbitRadius, realPeriod, color);
    }

    protected void drawPlanetWithRing(Graphics g) {
        // Draw the planet
        drawCelestialBody(g);
        Color originalColor = g.getColor();

        // Draw the ring
        g.setColor(new Color(200, 200, 200, 100));
        g.drawOval(position.x - scaledSize, position.y - scaledSize / 4, scaledSize * 2, scaledSize / 2);
        g.setColor(originalColor);
    }
}

// ===============================================================
// Moon - represents a moon orbiting a planet
// ===============================================================
class Moon extends CelestialBody {
    private final Planet parentPlanet;

    public Moon(String name, double baseSize, double baseOrbitRadius, double realPeriod, Color color, Planet parentPlanet) {
        super(name, baseSize, baseOrbitRadius, realPeriod, color);
        this.parentPlanet = parentPlanet;
    }

    public Planet getParentPlanet() {
        return parentPlanet;
    }

    protected void drawOrbit(Graphics g) {
        Point parentPos = parentPlanet.getPosition();
        g.drawOval(parentPos.x - scaledOrbitRadius, parentPos.y - scaledOrbitRadius, scaledOrbitRadius * 2, scaledOrbitRadius * 2);
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

    // Base dimensions for reference scaling
    private final int baseWidth = 1000;
    private final int baseHeight = 800;

    // To determine the largest orbit that needs to fit
    private final double largestOrbitRadius = 420; // Neptune's orbit radius
    private final double orbitBufferFactor = 1.1; // Extra 10% buffer space

    private Dimension currentSize = new Dimension(baseWidth, baseHeight);
    private double lastScaleFactor = 1.0;

    public SolarSystemPane() {
        setBackground(Color.BLACK);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        startTime = System.currentTimeMillis();

        // Initialize sun
        sun = new Sun("Sun", 30, Color.YELLOW);

        // Initialize celestial bodies
        planets = new Planet[]{
                new Planet("Mercury", 6, 50, 7600530.0, new Color(200, 200, 200)),
                new Planet("Venus", 10 , 80, 19414166.0, new Color(255, 190, 100)),
                new Planet("Earth", 12, 120, 31558149.0, new Color(50, 130, 255)),
                new Planet("Mars", 8, 180, 59355036.0, new Color(255, 80, 30)),
                new Planet("Jupiter", 24 , 260, 374335776.0, new Color(255, 200, 150)),
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

    // Calculate the scale factor based on the current window size
    private double calculateScaleFactor() {
        Dimension size = getSize();

        // Only update if size has changed
        if (!size.equals(currentSize)) {
            currentSize = size;

            // Determine the effective space available
            int effectiveHeight = size.height - 30; // Subtract space for UI controls

            // Calculate the minimum dimension
            int minDimension = Math.min(size.width, effectiveHeight);

            // Calculate the required space for the largest orbit (including buffer)
            double requiredSpace = largestOrbitRadius * 2 * orbitBufferFactor;

            // Calculate scale factor based on the minimum dimension
            lastScaleFactor = minDimension / requiredSpace;

            return lastScaleFactor;
        }

        return lastScaleFactor;
    }

    // Scale all celestial bodies
    private void scaleAllBodies(double scaleFactor) {
        sun.scale(scaleFactor);

        for (Planet planet : planets) {
            planet.scale(scaleFactor);
        }

        for (Moon moon : moons) {
            moon.scale(scaleFactor);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        long elapsedTime = System.currentTimeMillis() - startTime;

        Dimension size = getSize();
        int centerX = size.width / 2;
        int centerY = size.height / 2;

        // Calculate scale factor and update all bodies
        double scaleFactor = calculateScaleFactor();
        scaleAllBodies(scaleFactor);

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
        int x = (int) (centerX + radius * Math.cos(angle)); //x = x0 + r * cos(angle)
        int y = (int) (centerY + radius * Math.sin(angle)); //y = y0 + r * sin(angle)
        return new Point(x, y);
    }

    public double calculateAngle(long elapsedTime, double period, double timeScale, double initialAngle) {
        return (2 * Math.PI * elapsedTime) / (period * 1000 / timeScale) + initialAngle; // 2 * PI * t / T
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
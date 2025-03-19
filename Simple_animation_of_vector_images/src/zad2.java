import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class zad2 {
    public static void main(String[] args) {
        // Create the window of the solar system
        SolarSystemWindow window = new SolarSystemWindow();

        // Closing window terminates the program
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the initial position of the window on the screen
        // and make the window visible
        window.setBounds(50, 50, 1000, 800);
        window.setVisible(true);


        // Start the infinite loop of animation.
        while (true) {
            try {
                // Wait x milliseconds before the system is redisplayed
                Thread.sleep(1);
            } catch (InterruptedException e) {
                System.out.println("Program interrupted");
            }
            // Redraw the system
            window.repaint();
        }
    }
}

// ===============================================================
// SolarSystemPane - implements the content pane of the window
// in which the animated solar system is displayed
// ===============================================================

class SolarSystemPane extends JPanel {
    // Coordinates of the center of the system (the Sun)
    private int centerX, centerY;

    // Timescale - the speed of the simulation
    private double timeScale = 2_000_000; // Default value 2 million

    // Real periods of the planets' orbits (in seconds)
    private final double MERCURY_REAL_PERIOD = 7600530.0;      // 88 days
    private final double VENUS_REAL_PERIOD = 19414166.0;       // 225 days
    private final double EARTH_REAL_PERIOD = 31558149.0;       // 365.25 days
    private final double MARS_REAL_PERIOD = 59355036.0;        // 687 days
    private final double JUPITER_REAL_PERIOD = 374335776.0;    // 11.86 years
    private final double SATURN_REAL_PERIOD = 929596608.0;     // 29.46 years
    private final double URANUS_REAL_PERIOD = 2651370019.0;    // 84.02 years
    private final double NEPTUNE_REAL_PERIOD = 5200418560.0;   // 164.79 years

    // Real period of the Moon's orbit (in seconds)
    private final double MOON_REAL_PERIOD = 2360591.0;         // 27.3 days

    // Periods of the planets' orbits (in milliseconds) calculated in updatePeriods()
    private double mercuryPeriod;
    private double venusPeriod;
    private double earthPeriod;
    private double marsPeriod;
    private double jupiterPeriod;
    private double saturnPeriod;
    private double uranusPeriod;
    private double neptunePeriod;
    private double moonPeriod;

    // Radii of the planets' orbits
    private final int MERCURY_ORBIT_RADIUS = 50;
    private final int VENUS_ORBIT_RADIUS = 80;
    private final int EARTH_ORBIT_RADIUS = 120;
    private final int MARS_ORBIT_RADIUS = 180;
    private final int JUPITER_ORBIT_RADIUS = 260;
    private final int SATURN_ORBIT_RADIUS = 320;
    private final int URANUS_ORBIT_RADIUS = 370;
    private final int NEPTUNE_ORBIT_RADIUS = 420;

    // Radius of the Moon's orbit
    private final int MOON_ORBIT_RADIUS = 20;

    // Sizes of the planets and the Moon (Diameters)
    private final int SUN_SIZE = 30;
    private final int MERCURY_SIZE = 6;
    private final int VENUS_SIZE = 10;
    private final int EARTH_SIZE = 12;
    private final int MARS_SIZE = 8;
    private final int JUPITER_SIZE = 24;
    private final int SATURN_SIZE = 22;
    private final int URANUS_SIZE = 16;
    private final int NEPTUNE_SIZE = 16;
    private final int MOON_SIZE = 4;

    // Starting time (for calculating orbital angles)
    private final long startTime;

    // Buttons for controlling simulation speed
    private JButton speedUpButton;
    private JButton slowDownButton;
    private JLabel speedLabel;

    SolarSystemPane() {
        super();
        setBackground(Color.BLACK);
        startTime = System.currentTimeMillis();

        // Calculate orbital periods based on time scale
        updatePeriods();

        // Add controls to change speed
        setLayout(new FlowLayout(FlowLayout.LEFT));

        speedUpButton = new JButton("Increase Speed");
        slowDownButton = new JButton("Decrease Speed");
        speedLabel = new JLabel("Time acceleration: " + (int)timeScale + "x");
        speedLabel.setForeground(Color.WHITE);

        add(speedUpButton);
        add(slowDownButton);
        add(speedLabel);

        speedUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeScale *= 2;
                updatePeriods();
                updateSpeedLabel();
            }
        });

        slowDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeScale /= 2;
                if (timeScale < 1) timeScale = 1;
                updatePeriods();
                updateSpeedLabel();
            }
        });
    }

    // Method to update orbital periods based on time scale
    private void updatePeriods() {
        mercuryPeriod = MERCURY_REAL_PERIOD * 1000 / timeScale;
        venusPeriod = VENUS_REAL_PERIOD * 1000 / timeScale;
        earthPeriod = EARTH_REAL_PERIOD * 1000 / timeScale;
        marsPeriod = MARS_REAL_PERIOD * 1000 / timeScale;
        jupiterPeriod = JUPITER_REAL_PERIOD * 1000 / timeScale;
        saturnPeriod = SATURN_REAL_PERIOD * 1000 / timeScale;
        uranusPeriod = URANUS_REAL_PERIOD * 1000 / timeScale;
        neptunePeriod = NEPTUNE_REAL_PERIOD * 1000 / timeScale;
        moonPeriod = MOON_REAL_PERIOD * 1000 / timeScale;
    }

    // Method to update the speed label
    private void updateSpeedLabel() {
        speedLabel.setText("Time acceleration: " + (int)timeScale + "x");
    }

    // Calculate position of an object in orbit
    private Point calculatePosition(int centerX, int centerY, int radius, double angle) {
        int x = (int) (centerX + radius * Math.cos(angle));
        int y = (int) (centerY + radius * Math.sin(angle));
        return new Point(x, y);
    }

    // Draw an orbit (circle)
    private void drawOrbit(Graphics g, int centerX, int centerY, int radius) {
        g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    // Draw a celestial body (planet or moon)
    private void drawPlanet(Graphics g, Point position, int size, Color color) {
        g.setColor(color);
        g.fillOval(position.x - size/2, position.y - size/2, size, size);
    }

    // Draw a planet with rings (Saturn)
    private void drawPlanetWithRing(Graphics g, Point position, int size, Color planetColor, Color ringColor) {
        // Draw the planet
        g.setColor(planetColor);
        g.fillOval(position.x - size/2, position.y - size/2, size, size);

        // Draw the rings
        g.setColor(ringColor);
        g.drawOval(position.x - size, position.y - size/4, size * 2, size/2);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable antialiasing for better graphics quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Get current window size
        Dimension size = getSize();

        // Calculate center of the system
        centerX = size.width / 2;
        centerY = size.height / 2;

        // Calculate time elapsed since start
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        // Calculate angles for each planet
        double mercuryAngle = (2 * Math.PI * elapsedTime) / mercuryPeriod;
        double venusAngle = (2 * Math.PI * elapsedTime) / venusPeriod;
        double earthAngle = (2 * Math.PI * elapsedTime) / earthPeriod;
        double marsAngle = (2 * Math.PI * elapsedTime) / marsPeriod;
        double jupiterAngle = (2 * Math.PI * elapsedTime) / jupiterPeriod;
        double saturnAngle = (2 * Math.PI * elapsedTime) / saturnPeriod;
        double uranusAngle = (2 * Math.PI * elapsedTime) / uranusPeriod;
        double neptuneAngle = (2 * Math.PI * elapsedTime) / neptunePeriod;
        double moonAngle = (2 * Math.PI * elapsedTime) / moonPeriod;

        // Draw planet orbits
        g2d.setColor(new Color(100, 100, 100, 70));
        drawOrbit(g2d, centerX, centerY, MERCURY_ORBIT_RADIUS);
        drawOrbit(g2d, centerX, centerY, VENUS_ORBIT_RADIUS);
        drawOrbit(g2d, centerX, centerY, EARTH_ORBIT_RADIUS);
        drawOrbit(g2d, centerX, centerY, MARS_ORBIT_RADIUS);
        drawOrbit(g2d, centerX, centerY, JUPITER_ORBIT_RADIUS);
        drawOrbit(g2d, centerX, centerY, SATURN_ORBIT_RADIUS);
        drawOrbit(g2d, centerX, centerY, URANUS_ORBIT_RADIUS);
        drawOrbit(g2d, centerX, centerY, NEPTUNE_ORBIT_RADIUS);

        // Calculate planet positions
        Point mercuryPos = calculatePosition(centerX, centerY, MERCURY_ORBIT_RADIUS, mercuryAngle);
        Point venusPos = calculatePosition(centerX, centerY, VENUS_ORBIT_RADIUS, venusAngle);
        Point earthPos = calculatePosition(centerX, centerY, EARTH_ORBIT_RADIUS, earthAngle);
        Point marsPos = calculatePosition(centerX, centerY, MARS_ORBIT_RADIUS, marsAngle);
        Point jupiterPos = calculatePosition(centerX, centerY, JUPITER_ORBIT_RADIUS, jupiterAngle);
        Point saturnPos = calculatePosition(centerX, centerY, SATURN_ORBIT_RADIUS, saturnAngle);
        Point uranusPos = calculatePosition(centerX, centerY, URANUS_ORBIT_RADIUS, uranusAngle);
        Point neptunePos = calculatePosition(centerX, centerY, NEPTUNE_ORBIT_RADIUS, neptuneAngle);

        // Draw Moon's orbit around Earth
        g2d.setColor(new Color(100, 100, 100, 40));
        drawOrbit(g2d, earthPos.x, earthPos.y, MOON_ORBIT_RADIUS);

        // Calculate Moon's position relative to Earth
        Point moonPos = calculatePosition(earthPos.x, earthPos.y, MOON_ORBIT_RADIUS, moonAngle);

        // Draw the Sun
        drawPlanet(g2d, new Point(centerX, centerY), SUN_SIZE, Color.YELLOW);

        // Draw planets
        drawPlanet(g2d, mercuryPos, MERCURY_SIZE, new Color(200, 200, 200));  // Mercury - gray
        drawPlanet(g2d, venusPos, VENUS_SIZE, new Color(255, 190, 100));      // Venus - orange
        drawPlanet(g2d, earthPos, EARTH_SIZE, new Color(50, 130, 255));       // Earth - blue
        drawPlanet(g2d, marsPos, MARS_SIZE, new Color(255, 80, 30));          // Mars - red
        drawPlanet(g2d, jupiterPos, JUPITER_SIZE, new Color(255, 200, 150));  // Jupiter - light orange
        drawPlanetWithRing(g2d, saturnPos, SATURN_SIZE, new Color(230, 220, 130), new Color(210, 190, 120)); // Saturn with rings
        drawPlanet(g2d, uranusPos, URANUS_SIZE, new Color(180, 230, 230));    // Uranus - light blue
        drawPlanet(g2d, neptunePos, NEPTUNE_SIZE, new Color(100, 150, 255));  // Neptune - dark blue

        // Draw the Moon
        drawPlanet(g2d, moonPos, MOON_SIZE, new Color(200, 200, 200));        // Moon - gray

        // Add planet information
        g2d.setColor(Color.WHITE);
        drawPlanet(g2d, new Point(70, 130), MERCURY_SIZE, new Color(200, 200, 200));
        g2d.drawString("Mercury", 10, 140);
        drawPlanet(g2d, new Point(70, 150), VENUS_SIZE, new Color(255, 190, 100));
        g2d.drawString("Venus", 10, 160);
        drawPlanet(g2d, new Point(70, 170), EARTH_SIZE, new Color(50, 130, 255));
        g2d.drawString("Earth", 10, 180);
        drawPlanet(g2d, new Point(70, 190), MARS_SIZE, new Color(255, 80, 30));
        g2d.drawString("Mars", 10, 200);
        drawPlanet(g2d, new Point(70, 210), JUPITER_SIZE, new Color(255, 200, 150));
        g2d.drawString("Jupiter", 10, 220);
        drawPlanetWithRing(g2d, new Point(70, 230), SATURN_SIZE, new Color(230, 220, 130), new Color(210, 190, 120));
        g2d.drawString("Saturn", 10, 240);
        drawPlanet(g2d, new Point(70, 250), URANUS_SIZE, new Color(180, 230, 230));
        g2d.drawString("Uranus", 10, 260);
        drawPlanet(g2d, new Point(70, 270), NEPTUNE_SIZE, new Color(100, 150, 255));
        g2d.drawString("Neptune", 10, 280);
        drawPlanet(g2d, new Point(70, 290), MOON_SIZE, new Color(200, 200, 200));
        g2d.drawString("Moon", 10, 300);
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
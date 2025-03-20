import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class zad3 {
    public static void main(String[] args) {
        // Create the billiards table window
        BilliardWindow window = new BilliardWindow();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setBounds(50, 50, 800, 600);
        window.setVisible(true);

        // Variables for time-based animation
        long lastTime = System.currentTimeMillis();

        // Start the infinite loop of animation.
        while (true) {
            try {
                // Wait x milliseconds before the system is redisplayed
                Thread.sleep(5);
            } catch (InterruptedException e) {
                System.out.println("Program interrupted");
            }

            // Calculate delta time in seconds
            long currentTime = System.currentTimeMillis();
            double deltaTime = (currentTime - lastTime) / 1000.0; // Convert to seconds
            lastTime = currentTime;

            // Redraw the system with the time delta
            window.update(deltaTime);
            window.repaint();
        }
    }
}

// ===============================================================
// Ball - represents a billiard ball with physics properties
// ===============================================================
class Ball {
    private double x, y;         // Position of the center of the ball
    private double vx, vy;       // Velocity of the ball
    private final double radius;       // Radius of the ball
    private final Color color;         // Color of the ball
    private final double mass = 1;     // Mass of the ball

    public Ball(double x, double y, double radius, double vx, double vy, Color color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getVX() { return vx; }
    public double getVY() { return vy; }
    public double getRadius() { return radius; }
    public Color getColor() { return color; }
    public double getMass() { return mass; }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setVX(double vx) { this.vx = vx; }
    public void setVY(double vy) { this.vy = vy; }

    // Detect and resolve collision between two balls
    public void collide(Ball other) {
        // Calculate distance between ball centers
        double dx = other.getX() - this.x;
        double dy = other.getY() - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        //Check if balls are colliding
        if (distance < this.radius + other.getRadius()) {
            // Normal vector pointing from center of this ball to center of other ball
            double nx = dx / distance;  // nx = (x2-x1)/d
            double ny = dy / distance;  // ny = (y2-y1)/d

            // Vector 90 degrees to the normal vector (tanget vector)
            double tx = -ny;  // tx = -ny
            double ty = nx;   // ty = nx

            // Calculate relative velocity in normal direction
            double v1n = this.vx * nx + this.vy * ny;  // v1n = v1x·nx + v1y·ny
            double v1t = this.vx * tx + this.vy * ty;  // v1t = v1x·tx + v1y·ty
            double v2n = other.getVX() * nx + other.getVY() * ny;  // v2n = v2x·nx + v2y·ny
            double v2t = other.getVX() * tx + other.getVY() * ty;  // v2t = v2x·tx + v2y·ty

            double m1 = this.mass;
            double m2 = other.getMass();

            double v1nAfter, v2nAfter;

            if (m1 == m2) {
                //For equal masses
                v1nAfter = v2n;  // v1n' = v2n
                v2nAfter = v1n;  // v2n' = v1n
            } else {
                //For different masses
                v1nAfter = ((m1 - m2) / (m1 + m2)) * v1n + ((2 * m2) / (m1 + m2)) * v2n;  // v1n' = ((m1-m2)/(m1+m2))·v1n + (2m2/(m1+m2))·v2n
                v2nAfter = ((2 * m1) / (m1 + m2)) * v1n + ((m2 - m1) / (m1 + m2)) * v2n;  // v2n' = (2m1/(m1+m2))·v1n + ((m2-m1)/(m1+m2))·v2n
            }


            //New velocities after collision
            double v1xAfter = v1nAfter * nx + v1t * tx;  // v1x' = v1n'·nx + v1t·tx
            double v1yAfter = v1nAfter * ny + v1t * ty;  // v1y' = v1n'·ny + v1t·ty
            double v2xAfter = v2nAfter * nx + v2t * tx;  // v2x' = v2n'·nx + v2t·tx
            double v2yAfter = v2nAfter * ny + v2t * ty;  // v2y' = v2n'·ny + v2t·ty

            // Set new velocities
            this.vx = v1xAfter;
            this.vy = v1yAfter;
            other.setVX(v2xAfter);
            other.setVY(v2yAfter);

            //Prevent balls from overlapping
            this.preventOverlap(other);
        }
    }

    // Prevent balls from overlapping
    public void preventOverlap(Ball other) {
        double dx = other.getX() - this.x;
        double dy = other.getY() - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < this.radius + other.getRadius()) {
            // Calculate minimum separation needed
            double overlap = (this.radius + other.getRadius() - distance) / 2.0;

            // Calculate unit vector of separation
            double nx = dx / distance;
            double ny = dy / distance;

            // Move balls apart along the collision normal
            this.x -= overlap * nx;
            this.y -= overlap * ny;
            other.setX(other.getX() + overlap * nx);
            other.setY(other.getY() + overlap * ny);
        }
    }

    // Apply friction to slow down the ball
    public void applyFriction(double frictionCoefficient, double deltaTime) {
        vx *= Math.pow(frictionCoefficient, deltaTime);
        vy *= Math.pow(frictionCoefficient, deltaTime);
    }


    // Check if ball velocity is below the minimum
    public boolean isAlmostStopped(double minVelocity) {
        return Math.abs(vx) < minVelocity && Math.abs(vy) < minVelocity;
    }

    // Stop the ball completely
    public void stop() {
        vx = 0;
        vy = 0;
    }

    // Update ball position based on current velocity and elapsed time
    public void move(double deltaTime) {
        System.out.println("vx: " + vx + " vy: " + vy + " deltaTime: " + deltaTime + " x: " + x + " y: " + y);
        x += vx * deltaTime;
        y += vy * deltaTime;
    }

    // Handle collision with table borders
    public void handleBorderCollision(int leftBorder, int rightBorder, int topBorder, int bottomBorder) {
        if (x - radius < leftBorder) {
            x = leftBorder + radius;
            vx = -vx;
        } else if (x + radius > rightBorder) {
            x = rightBorder - radius;
            vx = -vx;
        }

        if (y - radius < topBorder) {
            y = topBorder + radius;
            vy = -vy;
        } else if (y + radius > bottomBorder) {
            y = bottomBorder - radius;
            vy = -vy;
        }
    }

    // Draw the ball with a shiny effect
    public void draw(Graphics2D g2d, int index) {
        // Draw main ball
        g2d.setColor(color);
        g2d.fillOval((int)(x - radius), (int)(y - radius), (int)(2 * radius), (int)(2 * radius));

        // Draw ball number (except for white ball)
        if (!color.equals(Color.WHITE)) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, (int)(radius * 0.8)));
            String num = Integer.toString(index);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(num);
            int textHeight = fm.getHeight();
            g2d.drawString(num, (int)(x - textWidth/2), (int)(y + textHeight/4));
        }
    }
}

// ===============================================================
// BilliardTable - represents the physics and visual aspects of the table
// ===============================================================
class BilliardTable {
    // Constants
    private final int NUM_BALLS = 15; // Number of balls on the table
    private final double FRICTION = 0.7;   // Friction coefficient
    private final double MIN_VELOCITY = 0.1; // Minimum velocity before ball stops completely
    private final double MAX_INITIAL_VELOCITY = 10000; // Maximum initial velocity of balls

    // Colors of the table and border
    private final Color TABLE_COLOR = new Color(0, 100, 0);
    private final Color BORDER_COLOR = new Color(100, 60, 20);

    // Width of the border around the table
    private final int BORDER_WIDTH = 20;

    // Radius of the balls
    private final double BALL_RADIUS = 15;

    // List of balls on the table
    private final ArrayList<Ball> balls;

    private final int pocketRadius;
    private int width;
    private int height;

    public BilliardTable(int width, int height) {
        this.width = width;
        this.height = height;
        this.balls = new ArrayList<>();
        this.pocketRadius = BORDER_WIDTH;
        initializeBalls();
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    private void initializeBalls() {
        Random random = new Random();

        // Create balls with random positions, velocities, and colors
        for (int i = 0; i < NUM_BALLS; i++) {
            // Initial random position for ball
            double x = 100 + random.nextDouble() * 500; //TODO change to constants
            double y = 100 + random.nextDouble() * 300; //TODO change to constants

            // Ensure balls don't overlap
            boolean overlap;
            do {
                overlap = false;
                for (Ball ball : balls) {
                    double dx = x - ball.getX();
                    double dy = y - ball.getY();
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    if (distance < ball.getRadius() + ball.getRadius()) {
                        x = 100 + random.nextDouble() * 500;
                        y = 100 + random.nextDouble() * 300;
                        overlap = true;
                        break;
                    }
                }
            } while (overlap);

            //Initial random velocity for ball
            double vx = (random.nextDouble() - 0.5) * (MAX_INITIAL_VELOCITY / 2); //TODO czemu jak to daje 0 a drugie nie to nie ida tylko w x
            double vy = (random.nextDouble() - 0.5) * (MAX_INITIAL_VELOCITY / 2);

            //Initial color for ball
            Color color;
            if (i == 0) {
                color = Color.WHITE; // White ball (cue ball)
            } else if (i == 9) {
                color = Color.BLACK; // Black ball (8-ball)
            } else {
                color = new Color(
                        random.nextInt(156) + 100,
                        random.nextInt(156) + 100,
                        random.nextInt(156) + 100
                );
            }

            // Create ball and add it to the list
            Ball ball = new Ball(x, y, BALL_RADIUS, vx, vy, color);
            balls.add(ball);
        }
    }

    // Update the physics of all balls
    public void update(double deltaTime) {
        // Update positions of all balls
        for (Ball ball : balls) {
            // Apply friction with respect to time
            ball.applyFriction(FRICTION, deltaTime);

            // Stop ball if velocity is below threshold
            if (ball.isAlmostStopped(MIN_VELOCITY)) {
                ball.stop();
            }

            // Update position with respect to time
            ball.move(deltaTime);

            // Handle border collisions
            ball.handleBorderCollision(
                    BORDER_WIDTH,
                    width - BORDER_WIDTH,
                    BORDER_WIDTH,
                    height - BORDER_WIDTH
            );
        }

        // Detect and resolve collisions between balls
        handleBallCollisions();
    }

    private void handleBallCollisions() {
        for (int i = 0; i < balls.size(); i++) {
            Ball ball1 = balls.get(i);

            for (int j = i + 1; j < balls.size(); j++) {
                Ball ball2 = balls.get(j);
                ball1.collide(ball2);
            }
        }
    }

    // Draw the table and all balls
    public void draw(Graphics2D g2d) {
        // Draw table
        g2d.setColor(TABLE_COLOR);
        g2d.fillRect(BORDER_WIDTH, BORDER_WIDTH,
                width - 2 * BORDER_WIDTH,
                height - 2 * BORDER_WIDTH);

        // Draw pockets (holes in corners and middle sides)
        g2d.setColor(Color.BLACK);
        drawPocket(g2d, BORDER_WIDTH, BORDER_WIDTH);
        drawPocket(g2d, width - BORDER_WIDTH, BORDER_WIDTH);
        drawPocket(g2d, BORDER_WIDTH, height - BORDER_WIDTH);
        drawPocket(g2d, width - BORDER_WIDTH, height - BORDER_WIDTH);
        drawPocket(g2d, width/2, BORDER_WIDTH);
        drawPocket(g2d, width/2, height - BORDER_WIDTH);

        // Draw all balls
        for (int i = 0; i < balls.size(); i++) {
            balls.get(i).draw(g2d, i);
        }
    }

    // Helper method to draw a pocket
    private void drawPocket(Graphics2D g2d, int x, int y) {
        g2d.fillOval(x - pocketRadius/2, y - pocketRadius/2, pocketRadius, pocketRadius);
    }

    public Color getBorderColor() {
        return BORDER_COLOR;
    }

    public int getBORDER_WIDTH() {
        return BORDER_WIDTH;
    }
}

// ===============================================================
// BilliardPane - implements the content pane of the window
// in which the animated billiard table is displayed
// ===============================================================
class BilliardPane extends JPanel {
    private BilliardTable billiardTable;

    BilliardPane() {
        super();
        billiardTable = new BilliardTable(800, 600);
        setBackground(billiardTable.getBorderColor());
    }

    public void update(double deltaTime) {
        Dimension size = getSize();
        billiardTable.setDimensions(size.width, size.height);
        billiardTable.update(deltaTime);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        billiardTable.draw(g2d);
    }
}

// ===============================================================
// BilliardWindow - class implementing the window containing
// the animated billiard table
// ===============================================================
class BilliardWindow extends JFrame {
    private BilliardPane billiardPane;

    public BilliardWindow() {
        billiardPane = new BilliardPane();
        setContentPane(billiardPane);
        setTitle("Billiard Table Simulation");
    }

    public void update(double deltaTime) {
        billiardPane.update(deltaTime);
    }
}

//TODO czy moze byc tak ze jak jest zbyt szybki to moze pominac zderzenie czy nawet przejsc przez sciane (chyba tak) jak to naprawic
//TODO czemu nagle jest wolniejsze
//TODO apply friction jest cos źle
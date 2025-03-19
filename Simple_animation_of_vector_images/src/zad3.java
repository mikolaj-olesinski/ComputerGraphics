import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class zad3 {
    public static void main(String[] args) {
        BilardWindow window = new BilardWindow();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setBounds(50, 50, 800, 600);
        window.setVisible(true);

        // Uruchom animację
        Timer timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.update();
                window.repaint();
            }
        });
        timer.start();
    }
}

class BilardPane extends JPanel {
    // Parametry stołu
    private final Color TABLE_COLOR = new Color(0, 100, 0);
    private final Color BORDER_COLOR = new Color(100, 60, 20);
    private int borderWidth = 20;

    // Lista kul
    private ArrayList<Ball> balls;

    // Parametry symulacji
    private final double FRICTION = 0.99; // Współczynnik tarcia (0.99 = 1% spowolnienia na każdą aktualizację)
    private final double MIN_VELOCITY = 0.1; // Minimalna prędkość, poniżej której kula zostaje zatrzymana

    BilardPane() {
        super();
        setBackground(BORDER_COLOR);
        initializeBalls();
    }

    private void initializeBalls() {
        balls = new ArrayList<>();
        Random random = new Random();

        // Utwórz 10 kul o losowych pozycjach, prędkościach i kolorach
        for (int i = 0; i < 10; i++) {
            double radius = 15;
            double x = 100 + random.nextDouble() * 500;
            double y = 100 + random.nextDouble() * 300;

            // Upewnij się, że kule nie nakładają się na siebie
            boolean overlap;
            do {
                overlap = false;
                for (Ball ball : balls) {
                    double dx = x - ball.x;
                    double dy = y - ball.y;
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    if (distance < radius + ball.radius) {
                        x = 100 + random.nextDouble() * 500;
                        y = 100 + random.nextDouble() * 300;
                        overlap = true;
                        break;
                    }
                }
            } while (overlap);

            double vx = (random.nextDouble() - 0.5) * 10;
            double vy = (random.nextDouble() - 0.5) * 10;

            Color color;
            if (i == 0) {
                color = Color.WHITE; // Bila biała
            } else if (i == 9) {
                color = Color.BLACK; // Bila czarna
            } else {
                color = new Color(
                        random.nextInt(156) + 100,
                        random.nextInt(156) + 100,
                        random.nextInt(156) + 100
                );
            }

            Ball ball = new Ball(x, y, radius, vx, vy, color);
            balls.add(ball);
        }
    }

    public void update() {
        Dimension size = getSize();
        int tableWidth = size.width - 2 * borderWidth;
        int tableHeight = size.height - 2 * borderWidth;

        // Aktualizuj pozycje wszystkich kul
        for (Ball ball : balls) {
            // Zastosuj tarcie
            ball.vx *= FRICTION;
            ball.vy *= FRICTION;

            // Zatrzymaj kulę, jeśli prędkość jest poniżej progu
            if (Math.abs(ball.vx) < MIN_VELOCITY && Math.abs(ball.vy) < MIN_VELOCITY) {
                ball.vx = 0;
                ball.vy = 0;
            }

            // Aktualizuj pozycję
            ball.x += ball.vx;
            ball.y += ball.vy;

            // Wykryj kolizje ze ścianami
            if (ball.x - ball.radius < borderWidth) {
                ball.x = borderWidth + ball.radius;
                ball.vx = -ball.vx;
            } else if (ball.x + ball.radius > size.width - borderWidth) {
                ball.x = size.width - borderWidth - ball.radius;
                ball.vx = -ball.vx;
            }

            if (ball.y - ball.radius < borderWidth) {
                ball.y = borderWidth + ball.radius;
                ball.vy = -ball.vy;
            } else if (ball.y + ball.radius > size.height - borderWidth) {
                ball.y = size.height - borderWidth - ball.radius;
                ball.vy = -ball.vy;
            }
        }

        // Wykryj kolizje między kulami
        for (int i = 0; i < balls.size(); i++) {
            Ball ball1 = balls.get(i);

            for (int j = i + 1; j < balls.size(); j++) {
                Ball ball2 = balls.get(j);

                double dx = ball2.x - ball1.x;
                double dy = ball2.y - ball1.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                // Sprawdź czy kule się zderzają
                if (distance < ball1.radius + ball2.radius) {
                    // Normalizacja wektora kolizji
                    double nx = dx / distance;
                    double ny = dy / distance;

                    // Głębokość penetracji
                    double penetration = (ball1.radius + ball2.radius - distance) / 2.0;

                    // Skoryguj pozycje, aby uniknąć nakładania się kul
                    ball1.x -= nx * penetration;
                    ball1.y -= ny * penetration;
                    ball2.x += nx * penetration;
                    ball2.y += ny * penetration;

                    // Oblicz względną prędkość w kierunku normalnym
                    double vRelativeX = ball2.vx - ball1.vx;
                    double vRelativeY = ball2.vy - ball1.vy;
                    double vDotProduct = vRelativeX * nx + vRelativeY * ny;

                    // Jeśli kule oddalają się, nie obliczaj impulsu
                    if (vDotProduct > 0) {
                        continue;
                    }

                    // Równania zderzenia doskonale elastycznego (współczynnik odbicia = 1)
                    double impulseMagnitude = -(1.0 + 1.0) * vDotProduct / 2.0;

                    // Zastosuj impuls
                    ball1.vx -= impulseMagnitude * nx;
                    ball1.vy -= impulseMagnitude * ny;
                    ball2.vx += impulseMagnitude * nx;
                    ball2.vy += impulseMagnitude * ny;
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Dimension size = getSize();

        // Narysuj stół
        g2d.setColor(TABLE_COLOR);
        g2d.fillRect(borderWidth, borderWidth,
                size.width - 2 * borderWidth,
                size.height - 2 * borderWidth);

        // Narysuj łuzy (dziury w rogach)
        g2d.setColor(Color.BLACK);
        int pocketRadius = borderWidth;
        g2d.fillOval(borderWidth - pocketRadius/2, borderWidth - pocketRadius/2, pocketRadius, pocketRadius);
        g2d.fillOval(size.width - borderWidth - pocketRadius/2, borderWidth - pocketRadius/2, pocketRadius, pocketRadius);
        g2d.fillOval(borderWidth - pocketRadius/2, size.height - borderWidth - pocketRadius/2, pocketRadius, pocketRadius);
        g2d.fillOval(size.width - borderWidth - pocketRadius/2, size.height - borderWidth - pocketRadius/2, pocketRadius, pocketRadius);
        g2d.fillOval(size.width/2 - pocketRadius/2, borderWidth - pocketRadius/2, pocketRadius, pocketRadius);
        g2d.fillOval(size.width/2 - pocketRadius/2, size.height - borderWidth - pocketRadius/2, pocketRadius, pocketRadius);

        // Narysuj kule
        for (Ball ball : balls) {
            g2d.setColor(ball.color);
            g2d.fillOval((int)(ball.x - ball.radius), (int)(ball.y - ball.radius),
                    (int)(2 * ball.radius), (int)(2 * ball.radius));

            // Dodaj efekt połysku
            g2d.setColor(new Color(255, 255, 255, 120));
            g2d.fillOval((int)(ball.x - ball.radius * 0.3), (int)(ball.y - ball.radius * 0.3),
                    (int)(ball.radius * 0.4), (int)(ball.radius * 0.4));

            // Narysuj numer na bili (oprócz białej)
            if (!ball.color.equals(Color.WHITE)) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, (int)(ball.radius * 0.8)));
                String num = Integer.toString(balls.indexOf(ball));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(num);
                int textHeight = fm.getHeight();
                g2d.drawString(num, (int)(ball.x - textWidth/2), (int)(ball.y + textHeight/4));
            }
        }
    }
}

class Ball {
    double x, y;           // Pozycja środka kuli
    double vx, vy;         // Prędkość
    double radius;         // Promień
    Color color;           // Kolor

    public Ball(double x, double y, double radius, double vx, double vy, Color color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
    }
}

class BilardWindow extends JFrame {
    private BilardPane bilardPane;

    public BilardWindow() {
        bilardPane = new BilardPane();
        setContentPane(bilardPane);
        setTitle("Symulacja stołu bilardowego");
    }

    public void update() {
        bilardPane.update();
    }
}
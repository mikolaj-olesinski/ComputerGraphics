import java.awt.*;

// Base class for all shapes
abstract class Shape {
    protected Color color;

    public Shape(Color color) {
        this.color = color;
    }

    public abstract void draw(Graphics2D g2d);
    public abstract boolean isNearCenter(int x, int y);
    public abstract void move(int dx, int dy);
    // Nowa metoda move z ograniczeniami obszaru
    public abstract void move(int dx, int dy, int maxWidth, int maxHeight, int controlPanelHeight);
    public abstract String toFileString();
    public abstract Point getCenter();

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

// Line shape implementation
class Line extends Shape {
    private int x1, y1, x2, y2;
    private static final int SENSITIVITY = 10;

    public Line(int x1, int y1, int x2, int y2, Color color) {
        super(color);
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.drawLine(x1, y1, x2, y2);
    }

    @Override
    public boolean isNearCenter(int x, int y) {
        // Check if point is near the middle of the line
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        return Math.sqrt(Math.pow(x - midX, 2) + Math.pow(y - midY, 2)) <= SENSITIVITY;
    }

    public boolean isNearEnd1(int x, int y) {
        return Math.sqrt(Math.pow(x - x1, 2) + Math.pow(y - y1, 2)) <= SENSITIVITY;
    }

    public boolean isNearEnd2(int x, int y) {
        return Math.sqrt(Math.pow(x - x2, 2) + Math.pow(y - y2, 2)) <= SENSITIVITY;
    }

    public void setEnd1(int x, int y) {
        this.x1 = x;
        this.y1 = y;
    }

    public void setEnd2(int x, int y) {
        this.x2 = x;
        this.y2 = y;
    }

    // Ograniczone ustawienie końca 1 linii
    public void setEnd1(int x, int y, int maxWidth, int maxHeight, int controlPanelHeight) {
        this.x1 = Math.max(0, Math.min(x, maxWidth));
        this.y1 = Math.max(0, Math.min(y, maxHeight - controlPanelHeight));
    }

    // Ograniczone ustawienie końca 2 linii
    public void setEnd2(int x, int y, int maxWidth, int maxHeight, int controlPanelHeight) {
        this.x2 = Math.max(0, Math.min(x, maxWidth));
        this.y2 = Math.max(0, Math.min(y, maxHeight - controlPanelHeight));
    }

    @Override
    public void move(int dx, int dy) {
        x1 += dx;
        y1 += dy;
        x2 += dx;
        y2 += dy;
    }

    @Override
    public void move(int dx, int dy, int maxWidth, int maxHeight, int controlPanelHeight) {
        int newX1 = x1 + dx;
        int newY1 = y1 + dy;
        int newX2 = x2 + dx;
        int newY2 = y2 + dy;

        // Sprawdź czy po przesunięciu linia nie wyjdzie poza obszar rysowania
        if (newX1 >= 0 && newX1 <= maxWidth && newX2 >= 0 && newX2 <= maxWidth &&
                newY1 >= 0 && newY1 <= maxHeight - controlPanelHeight &&
                newY2 >= 0 && newY2 <= maxHeight - controlPanelHeight) {
            x1 = newX1;
            y1 = newY1;
            x2 = newX2;
            y2 = newY2;
        } else {
            // Jeśli wychodzi poza ekran, przesuń maksymalnie jak się da
            if (newX1 < 0) {
                int shift = -newX1;
                newX1 += shift;
                newX2 += shift;
            } else if (newX2 < 0) {
                int shift = -newX2;
                newX1 += shift;
                newX2 += shift;
            }

            if (newX1 > maxWidth) {
                int shift = newX1 - maxWidth;
                newX1 -= shift;
                newX2 -= shift;
            } else if (newX2 > maxWidth) {
                int shift = newX2 - maxWidth;
                newX1 -= shift;
                newX2 -= shift;
            }

            if (newY1 < 0) {
                int shift = -newY1;
                newY1 += shift;
                newY2 += shift;
            } else if (newY2 < 0) {
                int shift = -newY2;
                newY1 += shift;
                newY2 += shift;
            }

            int maxY = maxHeight - controlPanelHeight;
            if (newY1 > maxY) {
                int shift = newY1 - maxY;
                newY1 -= shift;
                newY2 -= shift;
            } else if (newY2 > maxY) {
                int shift = newY2 - maxY;
                newY1 -= shift;
                newY2 -= shift;
            }

            x1 = newX1;
            y1 = newY1;
            x2 = newX2;
            y2 = newY2;
        }
    }

    @Override
    public String toFileString() {
        return "LINE," + x1 + "," + y1 + "," + x2 + "," + y2 + "," +
                color.getRed() + "," + color.getGreen() + "," + color.getBlue();
    }

    @Override
    public Point getCenter() {
        return new Point((x1 + x2) / 2, (y1 + y2) / 2);
    }

    public Point getEnd1() {
        return new Point(x1, y1);
    }

    public Point getEnd2() {
        return new Point(x2, y2);
    }
}

// Rectangle shape implementation
class Rectangle extends Shape {
    private int x, y, width, height;
    private static final int SENSITIVITY = 10;

    public Rectangle(int x, int y, int width, int height, Color color) {
        super(color);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.drawRect(x, y, width, height);
    }

    @Override
    public boolean isNearCenter(int px, int py) {
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        return Math.sqrt(Math.pow(px - centerX, 2) + Math.pow(py - centerY, 2)) <= SENSITIVITY;
    }

    @Override
    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    @Override
    public void move(int dx, int dy, int maxWidth, int maxHeight, int controlPanelHeight) {
        int newX = x + dx;
        int newY = y + dy;

        // Check if rectangle is within bounds after moving
        if (newX >= 0 && newX + width <= maxWidth &&
                newY >= 0 && newY + height <= maxHeight - controlPanelHeight) {
            x = newX;
            y = newY;
        } else {
            // If it goes out of bounds, move as much as possible
            if (newX < 0) {
                newX = 0;
            } else if (newX + width > maxWidth) {
                newX = maxWidth - width;
            }

            if (newY < 0) {
                newY = 0;
            } else if (newY + height > maxHeight - controlPanelHeight) {
                newY = (maxHeight - controlPanelHeight) - height;
            }

            x = newX;
            y = newY;
        }
    }

    @Override
    public String toFileString() {
        return "RECTANGLE," + x + "," + y + "," + width + "," + height + "," +
                color.getRed() + "," + color.getGreen() + "," + color.getBlue();
    }

    @Override
    public Point getCenter() {
        return new Point(x + width / 2, y + height / 2);
    }
}

// Circle shape implementation
class Circle extends Shape {
    private int centerX, centerY, radius;
    private static final int SENSITIVITY = 10;

    public Circle(int centerX, int centerY, int radius, Color color) {
        super(color);
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    @Override
    public boolean isNearCenter(int x, int y) {
        // Check if point is near the center of the circle
        return Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)) <= SENSITIVITY;
    }

    @Override
    public void move(int dx, int dy) {
        centerX += dx;
        centerY += dy;
    }

    @Override
    public void move(int dx, int dy, int maxWidth, int maxHeight, int controlPanelHeight) {
        int newX = centerX + dx;
        int newY = centerY + dy;

        //Check if circle is within bounds after moving
        if (newX - radius >= 0 && newX + radius <= maxWidth &&
                newY - radius >= 0 && newY + radius <= maxHeight - controlPanelHeight) {
            centerX = newX;
            centerY = newY;
        } else {
            // If it goes out of bounds, move as much as possible
            if (newX - radius < 0) {
                newX = radius;
            } else if (newX + radius > maxWidth) {
                newX = maxWidth - radius;
            }

            if (newY - radius < 0) {
                newY = radius;
            } else if (newY + radius > maxHeight - controlPanelHeight) {
                newY = (maxHeight - controlPanelHeight) - radius;
            }

            centerX = newX;
            centerY = newY;
        }
    }

    @Override
    public String toFileString() {
        return "CIRCLE," + centerX + "," + centerY + "," + radius + "," +
                color.getRed() + "," + color.getGreen() + "," + color.getBlue();
    }

    @Override
    public Point getCenter() {
        return new Point(centerX, centerY);
    }
}
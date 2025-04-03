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

    @Override
    public void move(int dx, int dy) {
        x1 += dx;
        y1 += dy;
        x2 += dx;
        y2 += dy;
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
        System.out.println("Checking if point (" + x + ", " + y + ") is near circle center (" + centerX + ", " + centerY + ")");
        return Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)) <= SENSITIVITY;
    }

    @Override
    public void move(int dx, int dy) {
        centerX += dx;
        centerY += dy;
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
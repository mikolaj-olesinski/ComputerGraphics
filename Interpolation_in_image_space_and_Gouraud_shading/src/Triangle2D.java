import java.awt.*;
import java.awt.image.BufferedImage;

public class Triangle2D {
    // Współrzędne wierzchołków
    private int x1, y1, x2, y2, x3, y3;

    // Kolory wierzchołków
    private Color color1, color2, color3;

    /**
     * Konstruktor trójkąta
     */
    public Triangle2D(int x1, int y1, int x2, int y2, int x3, int y3,
                      Color color1, Color color2, Color color3) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }

    /**
     * Zwraca interpolowany kolor dla punktu (x,y) wewnątrz trójkąta
     */
    private Color interpolateColor(int x, int y) {
        // Obliczanie współrzędnych barycentrycznych
        double denominator = ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));

        // Zabezpieczenie przed dzieleniem przez zero
        if (Math.abs(denominator) < 0.0001) {
            return Color.BLACK; // Domyślny kolor w przypadku degeneracji trójkąta
        }

        double lambda1 = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / denominator;
        double lambda2 = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / denominator;
        double lambda3 = 1 - lambda1 - lambda2;

        // Zabezpieczenie przed błędami numerycznymi
        if (lambda1 < 0) lambda1 = 0;
        if (lambda2 < 0) lambda2 = 0;
        if (lambda3 < 0) lambda3 = 0;

        // Normalizacja współczynników
        double sum = lambda1 + lambda2 + lambda3;
        if (sum > 0) {
            lambda1 /= sum;
            lambda2 /= sum;
            lambda3 /= sum;
        }

        // Interpolacja kolorów
        int r = (int)(lambda1 * color1.getRed() + lambda2 * color2.getRed() + lambda3 * color3.getRed());
        int g = (int)(lambda1 * color1.getGreen() + lambda2 * color2.getGreen() + lambda3 * color3.getGreen());
        int b = (int)(lambda1 * color1.getBlue() + lambda2 * color2.getBlue() + lambda3 * color3.getBlue());

        // Zabezpieczenie przed przekroczeniem zakresu
        r = Math.min(255, Math.max(0, r));
        g = Math.min(255, Math.max(0, g));
        b = Math.min(255, Math.max(0, b));

        return new Color(r, g, b);
    }

    /**
     * Sprawdza czy punkt (x,y) znajduje się wewnątrz trójkąta
     */
    private boolean isPointInTriangle(int x, int y) {
        // Obliczanie współrzędnych barycentrycznych
        double denominator = ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));

        // Degeneracja trójkąta
        if (Math.abs(denominator) < 0.0001) {
            return false;
        }

        double lambda1 = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / denominator;
        double lambda2 = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / denominator;
        double lambda3 = 1 - lambda1 - lambda2;

        // Punkt jest wewnątrz trójkąta, jeśli wszystkie współczynniki są nieujemne
        return lambda1 >= 0 && lambda2 >= 0 && lambda3 >= 0;
    }

    /**
     * Znajduje bounding box trójkąta
     */
    private Rectangle getBoundingBox() {
        int minX = Math.min(Math.min(x1, x2), x3);
        int minY = Math.min(Math.min(y1, y2), y3);
        int maxX = Math.max(Math.max(x1, x2), x3);
        int maxY = Math.max(Math.max(y1, y2), y3);

        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    /**
     * Rysuje trójkąt z cieniowaniem Gourauda do BufferedImage
     */
    public void drawGouraudToImage(BufferedImage image) {
        if (image == null) return;

        Rectangle bbox = getBoundingBox();
        int minX = Math.max(0, bbox.x);
        int minY = Math.max(0, bbox.y);
        int maxX = Math.min(image.getWidth() - 1, bbox.x + bbox.width - 1);
        int maxY = Math.min(image.getHeight() - 1, bbox.y + bbox.height - 1);

        // Dla każdego piksela w bounding box sprawdź czy jest wewnątrz trójkąta
        // i zastosuj cieniowanie Gourauda
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isPointInTriangle(x, y)) {
                    Color interpolatedColor = interpolateColor(x, y);
                    image.setRGB(x, y, interpolatedColor.getRGB());
                }
            }
        }
    }

    /**
     * Rysuje trójkąt z cieniowaniem Gourauda na ekranie
     */
    public void drawGouraudToScreen(Graphics g) {
        if (g == null) return;

        Rectangle bbox = getBoundingBox();

        // Dla każdego piksela w bounding box sprawdź czy jest wewnątrz trójkąta
        // i zastosuj cieniowanie Gourauda
        for (int y = bbox.y; y < bbox.y + bbox.height; y++) {
            for (int x = bbox.x; x < bbox.x + bbox.width; x++) {
                if (isPointInTriangle(x, y)) {
                    Color interpolatedColor = interpolateColor(x, y);
                    g.setColor(interpolatedColor);
                    g.fillRect(x, y, 1, 1);
                }
            }
        }
    }

    /**
     * Zoptymalizowana wersja rysowania z cieniowaniem Gourauda do BufferedImage
     * używająca algorytmu skanowania linii (scanline)
     */
    public void drawGouraudToImageOptimized(BufferedImage image) {
        if (image == null) return;

        // Sortowanie wierzchołków względem współrzędnej y (od najmniejszej)
        int[] xValues = {x1, x2, x3};
        int[] yValues = {y1, y2, y3};
        Color[] colors = {color1, color2, color3};

        // Proste sortowanie bąbelkowe
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2 - i; j++) {
                if (yValues[j] > yValues[j + 1]) {
                    // Zamiana współrzędnych y
                    int tempY = yValues[j];
                    yValues[j] = yValues[j + 1];
                    yValues[j + 1] = tempY;

                    // Zamiana współrzędnych x
                    int tempX = xValues[j];
                    xValues[j] = xValues[j + 1];
                    xValues[j + 1] = tempX;

                    // Zamiana kolorów
                    Color tempColor = colors[j];
                    colors[j] = colors[j + 1];
                    colors[j + 1] = tempColor;
                }
            }
        }

        // Przypisz posortowane wartości
        int topX = xValues[0], midX = xValues[1], bottomX = xValues[2];
        int topY = yValues[0], midY = yValues[1], bottomY = yValues[2];
        Color topColor = colors[0], midColor = colors[1], bottomColor = colors[2];

        // Obcinanie do granic obrazu
        int minY = Math.max(0, topY);
        int maxY = Math.min(image.getHeight() - 1, bottomY);

        // Obliczenie współczynników nachylenia dla każdej krawędzi
        double dx1 = topY == midY ? 0 : (double)(midX - topX) / (midY - topY);
        double dx2 = topY == bottomY ? 0 : (double)(bottomX - topX) / (bottomY - topY);
        double dx3 = midY == bottomY ? 0 : (double)(bottomX - midX) / (bottomY - midY);

        // Współczynniki przyrostu dla składowych kolorów
        double dr1 = topY == midY ? 0 : (double)(midColor.getRed() - topColor.getRed()) / (midY - topY);
        double dg1 = topY == midY ? 0 : (double)(midColor.getGreen() - topColor.getGreen()) / (midY - topY);
        double db1 = topY == midY ? 0 : (double)(midColor.getBlue() - topColor.getBlue()) / (midY - topY);

        double dr2 = topY == bottomY ? 0 : (double)(bottomColor.getRed() - topColor.getRed()) / (bottomY - topY);
        double dg2 = topY == bottomY ? 0 : (double)(bottomColor.getGreen() - topColor.getGreen()) / (bottomY - topY);
        double db2 = topY == bottomY ? 0 : (double)(bottomColor.getBlue() - topColor.getBlue()) / (bottomY - topY);

        double dr3 = midY == bottomY ? 0 : (double)(bottomColor.getRed() - midColor.getRed()) / (bottomY - midY);
        double dg3 = midY == bottomY ? 0 : (double)(bottomColor.getGreen() - midColor.getGreen()) / (bottomY - midY);
        double db3 = midY == bottomY ? 0 : (double)(bottomColor.getBlue() - midColor.getBlue()) / (bottomY - midY);

        // Górna część trójkąta (pomiędzy topY i midY)
        double xl = topX;  // Lewa granica aktywnej linii
        double xr = topX;  // Prawa granica aktywnej linii

        double rl = topColor.getRed();  // Kolor na lewej granicy
        double gl = topColor.getGreen();
        double bl = topColor.getBlue();

        double rr = topColor.getRed();  // Kolor na prawej granicy
        double gr = topColor.getGreen();
        double br = topColor.getBlue();

        // Rysowanie górnej części trójkąta
        for (int y = minY; y < midY && y <= maxY; y++) {
            if (y >= topY) {  // Zaczynamy dopiero od topY
                int startX = (int)Math.ceil(Math.min(xl, xr));
                int endX = (int)Math.floor(Math.max(xl, xr));

                startX = Math.max(0, startX);
                endX = Math.min(image.getWidth() - 1, endX);

                // Inkrementalne cieniowanie dla każdego piksela w linii
                if (startX <= endX) {
                    double dx = endX == startX ? 0 : 1.0 / (endX - startX);
                    double dr = (rr - rl) * dx;
                    double dg = (gr - gl) * dx;
                    double db = (br - bl) * dx;

                    double r = rl;
                    double g = gl;
                    double b = bl;

                    for (int x = startX; x <= endX; x++) {
                        int rValue = Math.min(255, Math.max(0, (int)r));
                        int gValue = Math.min(255, Math.max(0, (int)g));
                        int bValue = Math.min(255, Math.max(0, (int)b));

                        image.setRGB(x, y, new Color(rValue, gValue, bValue).getRGB());

                        r += dr;
                        g += dg;
                        b += db;
                    }
                }

                // Aktualizacja granic aktywnej linii i kolorów
                xl += dx1;
                xr += dx2;

                rl += dr1;
                gl += dg1;
                bl += db1;

                rr += dr2;
                gr += dg2;
                br += db2;
            }
        }

        // Dolna część trójkąta (pomiędzy midY i bottomY)
        // Resetujemy lewą granicę, jeśli używamy krawędzi od środkowego do dolnego wierzchołka
        if (midY < bottomY) {
            xl = midX;
            rl = midColor.getRed();
            gl = midColor.getGreen();
            bl = midColor.getBlue();
        }

        // Rysowanie dolnej części trójkąta
        for (int y = Math.max(minY, midY); y <= maxY; y++) {
            int startX = (int)Math.ceil(Math.min(xl, xr));
            int endX = (int)Math.floor(Math.max(xl, xr));

            startX = Math.max(0, startX);
            endX = Math.min(image.getWidth() - 1, endX);

            // Inkrementalne cieniowanie dla każdego piksela w linii
            if (startX <= endX) {
                double dx = endX == startX ? 0 : 1.0 / (endX - startX);
                double dr = (rr - rl) * dx;
                double dg = (gr - gl) * dx;
                double db = (br - bl) * dx;

                double r = rl;
                double g = gl;
                double b = bl;

                for (int x = startX; x <= endX; x++) {
                    int rValue = Math.min(255, Math.max(0, (int)r));
                    int gValue = Math.min(255, Math.max(0, (int)g));
                    int bValue = Math.min(255, Math.max(0, (int)b));

                    image.setRGB(x, y, new Color(rValue, gValue, bValue).getRGB());

                    r += dr;
                    g += dg;
                    b += db;
                }
            }

            // Aktualizacja granic aktywnej linii i kolorów
            xl += dx3;
            xr += dx2;

            rl += dr3;
            gl += dg3;
            bl += db3;

            rr += dr2;
            gr += dg2;
            br += db2;
        }
    }

    /**
     * Zoptymalizowana wersja rysowania z cieniowaniem Gourauda na ekranie
     * używająca algorytmu skanowania linii (scanline)
     */
    public void drawGouraudToScreenOptimized(Graphics graphics) {
        if (graphics == null) return;

        // Sortowanie wierzchołków względem współrzędnej y (od najmniejszej)
        int[] xValues = {x1, x2, x3};
        int[] yValues = {y1, y2, y3};
        Color[] colors = {color1, color2, color3};

        // Proste sortowanie bąbelkowe
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2 - i; j++) {
                if (yValues[j] > yValues[j + 1]) {
                    // Zamiana współrzędnych y
                    int tempY = yValues[j];
                    yValues[j] = yValues[j + 1];
                    yValues[j + 1] = tempY;

                    // Zamiana współrzędnych x
                    int tempX = xValues[j];
                    xValues[j] = xValues[j + 1];
                    xValues[j + 1] = tempX;

                    // Zamiana kolorów
                    Color tempColor = colors[j];
                    colors[j] = colors[j + 1];
                    colors[j + 1] = tempColor;
                }
            }
        }

        // Przypisz posortowane wartości
        int topX = xValues[0], midX = xValues[1], bottomX = xValues[2];
        int topY = yValues[0], midY = yValues[1], bottomY = yValues[2];
        Color topColor = colors[0], midColor = colors[1], bottomColor = colors[2];

        // Obliczenie współczynników nachylenia dla każdej krawędzi
        double dx1 = topY == midY ? 0 : (double)(midX - topX) / (midY - topY);
        double dx2 = topY == bottomY ? 0 : (double)(bottomX - topX) / (bottomY - topY);
        double dx3 = midY == bottomY ? 0 : (double)(bottomX - midX) / (bottomY - midY);

        // Współczynniki przyrostu dla składowych kolorów
        double dr1 = topY == midY ? 0 : (double)(midColor.getRed() - topColor.getRed()) / (midY - topY);
        double dg1 = topY == midY ? 0 : (double)(midColor.getGreen() - topColor.getGreen()) / (midY - topY);
        double db1 = topY == midY ? 0 : (double)(midColor.getBlue() - topColor.getBlue()) / (midY - topY);

        double dr2 = topY == bottomY ? 0 : (double)(bottomColor.getRed() - topColor.getRed()) / (bottomY - topY);
        double dg2 = topY == bottomY ? 0 : (double)(bottomColor.getGreen() - topColor.getGreen()) / (bottomY - topY);
        double db2 = topY == bottomY ? 0 : (double)(bottomColor.getBlue() - topColor.getBlue()) / (bottomY - topY);

        double dr3 = midY == bottomY ? 0 : (double)(bottomColor.getRed() - midColor.getRed()) / (bottomY - midY);
        double dg3 = midY == bottomY ? 0 : (double)(midColor.getGreen() - midColor.getGreen()) / (bottomY - midY);
        double db3 = midY == bottomY ? 0 : (double)(bottomColor.getBlue() - midColor.getBlue()) / (bottomY - midY);

        // Górna część trójkąta (pomiędzy topY i midY)
        double xl = topX;  // Lewa granica aktywnej linii
        double xr = topX;  // Prawa granica aktywnej linii

        double rl = topColor.getRed();  // Kolor na lewej granicy
        double gl = topColor.getGreen();
        double bl = topColor.getBlue();

        double rr = topColor.getRed();  // Kolor na prawej granicy
        double gr = topColor.getGreen();
        double br = topColor.getBlue();

        // Rysowanie górnej części trójkąta
        for (int y = topY; y < midY; y++) {
            int startX = (int)Math.ceil(Math.min(xl, xr));
            int endX = (int)Math.floor(Math.max(xl, xr));

            // Inkrementalne cieniowanie dla każdego piksela w linii
            if (startX <= endX) {
                double dx = endX == startX ? 0 : 1.0 / (endX - startX);
                double dr = (rr - rl) * dx;
                double dg = (gr - gl) * dx;
                double db = (br - bl) * dx;

                double r = rl;
                double g = gl;
                double b = bl;

                for (int x = startX; x <= endX; x++) {
                    int rValue = Math.min(255, Math.max(0, (int)r));
                    int gValue = Math.min(255, Math.max(0, (int)g));
                    int bValue = Math.min(255, Math.max(0, (int)b));

                    graphics.setColor(new Color(rValue, gValue, bValue));
                    graphics.fillRect(x, y, 1, 1);

                    r += dr;
                    g += dg;
                    b += db;
                }
            }

            // Aktualizacja granic aktywnej linii i kolorów
            xl += dx1;
            xr += dx2;

            rl += dr1;
            gl += dg1;
            bl += db1;

            rr += dr2;
            gr += dg2;
            br += db2;
        }

        // Dolna część trójkąta (pomiędzy midY i bottomY)
        // Resetujemy lewą granicę, jeśli używamy krawędzi od środkowego do dolnego wierzchołka
        if (midY < bottomY) {
            xl = midX;
            rl = midColor.getRed();
            gl = midColor.getGreen();
            bl = midColor.getBlue();
        }

        // Rysowanie dolnej części trójkąta
        for (int y = midY; y <= bottomY; y++) {
            int startX = (int)Math.ceil(Math.min(xl, xr));
            int endX = (int)Math.floor(Math.max(xl, xr));

            // Inkrementalne cieniowanie dla każdego piksela w linii
            if (startX <= endX) {
                double dx = endX == startX ? 0 : 1.0 / (endX - startX);
                double dr = (rr - rl) * dx;
                double dg = (gr - gl) * dx;
                double db = (br - bl) * dx;

                double r = rl;
                double g = gl;
                double b = bl;

                for (int x = startX; x <= endX; x++) {
                    int rValue = Math.min(255, Math.max(0, (int)r));
                    int gValue = Math.min(255, Math.max(0, (int)g));
                    int bValue = Math.min(255, Math.max(0, (int)b));

                    graphics.setColor(new Color(rValue, gValue, bValue));
                    graphics.fillRect(x, y, 1, 1);

                    r += dr;
                    g += dg;
                    b += db;
                }
            }

            // Aktualizacja granic aktywnej linii i kolorów
            xl += dx3;
            xr += dx2;

            rl += dr3;
            gl += dg3;
            bl += db3;

            rr += dr2;
            gr += dg2;
            br += db2;
        }
    }
}
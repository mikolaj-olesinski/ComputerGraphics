import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;

public class Triangle2D {
    // Wierzchołki trójkąta (współrzędne x, y)
    private int[] x;
    private int[] y;

    // Kolory wierzchołków (R, G, B)
    private Color[] colors;

    public Triangle2D(int[] x, int[] y, Color[] colors) {
        if (x.length != 3 || y.length != 3 || colors.length != 3) {
            throw new IllegalArgumentException("Trójkąt musi mieć dokładnie 3 wierzchołki!");
        }

        this.x = x.clone();
        this.y = y.clone();
        this.colors = colors.clone();

        // Sortowanie wierzchołków według wartości y (od najmniejszej do największej)
        sortVerticesByY();
    }

    private void sortVerticesByY() {
        Integer[] idx = {0, 1, 2};
        // Sortujemy indeksy wg wartości y[idx[i]]
        Arrays.sort(idx, Comparator.comparingInt(i -> y[i]));

        // Tworzymy nowe, posortowane tablice
        int[] newX = new int[3], newY = new int[3];
        Color[] newC = new Color[3];
        for (int i = 0; i < 3; i++) {
            newX[i] = x[idx[i]];
            newY[i] = y[idx[i]];
            newC[i] = colors[idx[i]];
        }
        // Podmieniamy oryginały
        x = newX;
        y = newY;
        colors = newC;
    }

    public void gouraudShadeToImage(BufferedImage image) {
        // Sprawdzenie czy obraz istnieje
        if (image == null) {
            throw new IllegalArgumentException("Obraz nie może być null!");
        }

        // Szerokość i wysokość obrazu
        int width = image.getWidth();
        int height = image.getHeight();

        // Po sortowaniu wierzchołków mamy:
        // y[0] <= y[1] <= y[2]

        // Podział na dwa przypadki: trójkąt "płaski" i ogólny
        // Przypadek gdy środkowy wierzchołek ma inną wartość y niż górny i dolny
        if (y[0] != y[1] && y[1] != y[2]) {
            // Znajdź x dla punktu na krawędzi 0-2 o tej samej wysokości co środkowy wierzchołek
            float t = (float)(y[1] - y[0]) / (y[2] - y[0]);
            int x4 = Math.round(x[0] + t * (x[2] - x[0]));

            // Interpolacja koloru dla nowego punktu
            int r4 = Math.round(colors[0].getRed() + t * (colors[2].getRed() - colors[0].getRed()));
            int g4 = Math.round(colors[0].getGreen() + t * (colors[2].getGreen() - colors[0].getGreen()));
            int b4 = Math.round(colors[0].getBlue() + t * (colors[2].getBlue() - colors[0].getBlue()));
            Color color4 = new Color(
                    Math.max(0, Math.min(255, r4)),
                    Math.max(0, Math.min(255, g4)),
                    Math.max(0, Math.min(255, b4))
            );

            // Cieniowanie górnego trójkąta
            fillFlatTriangle(image, x[0], y[0], x[1], y[1], x4, y[1],
                    colors[0], colors[1], color4, width, height);

            // Cieniowanie dolnego trójkąta
            fillFlatTriangle(image, x[1], y[1], x4, y[1], x[2], y[2],
                    colors[1], color4, colors[2], width, height);
        }
        // Przypadek gdy dwa wierzchołki mają tę samą wartość y (trójkąt "płaski")
        else if (y[0] == y[1]) {
            // Przypadek z płaskim wierzchem
            fillFlatTopTriangle(image, x[0], y[0], x[1], y[1], x[2], y[2],
                    colors[0], colors[1], colors[2], width, height);
        }
        else { // y[1] == y[2]
            // Przypadek z płaskim spodem
            fillFlatBottomTriangle(image, x[0], y[0], x[1], y[1], x[2], y[2],
                    colors[0], colors[1], colors[2], width, height);
        }
    }

    // Rysuje trójkąt z płaskim wierzchołkiem (dwa górne wierzchołki mają tę samą wartość y)
    private void fillFlatTopTriangle(BufferedImage image, int x1, int y1, int x2, int y2, int x3, int y3,
                                     Color c1, Color c2, Color c3, int width, int height) {
        // Upewniamy się, że y1 == y2 < y3
        assert y1 == y2 && y2 < y3;

        // Wyznaczenie zakresu y do rysowania
        int yStart = Math.max(0, y1);
        int yEnd = Math.min(height - 1, y3);

        // Obliczamy odwrotność wysokości do interpolacji
        float invHeight = 1.0f / (y3 - y1);

        for (int y = yStart; y <= yEnd; y++) {
            // Współczynnik interpolacji dla krawędzi
            float t = (float)(y - y1) * invHeight;

            // Obliczenie lewej i prawej krawędzi
            int xLeft = Math.round(x1 + t * (x3 - x1));
            int xRight = Math.round(x2 + t * (x3 - x2));

            // Interpolacja kolorów
            Color colorLeft = interpolateColor(c1, c3, t);
            Color colorRight = interpolateColor(c2, c3, t);

            // Upewniamy się, że xLeft <= xRight
            if (xLeft > xRight) {
                int tempX = xLeft; xLeft = xRight; xRight = tempX;
                Color tempColor = colorLeft; colorLeft = colorRight; colorRight = tempColor;
            }

            // Ograniczenie do szerokości obrazu
            xLeft = Math.max(0, Math.min(width - 1, xLeft));
            xRight = Math.max(0, Math.min(width - 1, xRight));

            // Rysowanie scanline z interpolacją kolorów
            drawScanline(image, y, xLeft, xRight, colorLeft, colorRight);
        }
    }

    // Rysuje trójkąt z płaskim spodem (dwa dolne wierzchołki mają tę samą wartość y)
    private void fillFlatBottomTriangle(BufferedImage image, int x1, int y1, int x2, int y2, int x3, int y3,
                                        Color c1, Color c2, Color c3, int width, int height) {
        // Upewniamy się, że y1 < y2 == y3
        assert y1 < y2 && y2 == y3;

        // Wyznaczenie zakresu y do rysowania
        int yStart = Math.max(0, y1);
        int yEnd = Math.min(height - 1, y2);

        // Obliczamy odwrotność wysokości do interpolacji
        float invHeight = 1.0f / (y2 - y1);

        for (int y = yStart; y <= yEnd; y++) {
            // Współczynnik interpolacji dla krawędzi
            float t = (float)(y - y1) * invHeight;

            // Obliczenie lewej i prawej krawędzi
            int xLeft = Math.round(x1 + t * (x2 - x1));
            int xRight = Math.round(x1 + t * (x3 - x1));

            // Interpolacja kolorów
            Color colorLeft = interpolateColor(c1, c2, t);
            Color colorRight = interpolateColor(c1, c3, t);

            // Upewniamy się, że xLeft <= xRight
            if (xLeft > xRight) {
                int tempX = xLeft; xLeft = xRight; xRight = tempX;
                Color tempColor = colorLeft; colorLeft = colorRight; colorRight = tempColor;
            }

            // Ograniczenie do szerokości obrazu
            xLeft = Math.max(0, Math.min(width - 1, xLeft));
            xRight = Math.max(0, Math.min(width - 1, xRight));

            // Rysowanie scanline z interpolacją kolorów
            drawScanline(image, y, xLeft, xRight, colorLeft, colorRight);
        }
    }

    // Rysuje ogólny trójkąt płaski (jeden z wierzchołków ma taką samą wartość y jak inny)
    private void fillFlatTriangle(BufferedImage image, int x1, int y1, int x2, int y2, int x3, int y3,
                                  Color c1, Color c2, Color c3, int width, int height) {
        // Wyznaczenie zakresu y do rysowania
        int yStart = Math.max(0, Math.min(height - 1, y1));
        int yEnd = Math.max(0, Math.min(height - 1, y3));

        // Czy to jest trójkąt z płaskim wierzchem czy spodem?
        if (y1 == y2) {
            fillFlatTopTriangle(image, x1, y1, x2, y2, x3, y3, c1, c2, c3, width, height);
        } else if (y2 == y3) {
            fillFlatBottomTriangle(image, x1, y1, x2, y2, x3, y3, c1, c2, c3, width, height);
        } else {
            // W przeciwnym razie używamy ogólnego przypadku z interpolacją
            float invHeight = 1.0f / (y3 - y1);

            for (int y = yStart; y <= yEnd; y++) {
                float t = (float)(y - y1) * invHeight;

                // Określenie, czy jesteśmy w górnej czy dolnej części trójkąta
                boolean topPart = y < y2;

                int xLeft, xRight;
                Color colorLeft, colorRight;

                if (topPart) {
                    // Górna część trójkąta
                    float t1 = (y1 == y2) ? 0 : (float)(y - y1) / (y2 - y1);
                    float t2 = (float)(y - y1) / (y3 - y1);

                    xLeft = Math.round(x1 + t1 * (x2 - x1));
                    xRight = Math.round(x1 + t2 * (x3 - x1));

                    colorLeft = interpolateColor(c1, c2, t1);
                    colorRight = interpolateColor(c1, c3, t2);
                } else {
                    // Dolna część trójkąta
                    float t1 = (y2 == y3) ? 0 : (float)(y - y2) / (y3 - y2);
                    float t2 = (float)(y - y1) / (y3 - y1);

                    xLeft = Math.round(x2 + t1 * (x3 - x2));
                    xRight = Math.round(x1 + t2 * (x3 - x1));

                    colorLeft = interpolateColor(c2, c3, t1);
                    colorRight = interpolateColor(c1, c3, t2);
                }

                // Upewniamy się, że xLeft <= xRight
                if (xLeft > xRight) {
                    int tempX = xLeft; xLeft = xRight; xRight = tempX;
                    Color tempColor = colorLeft; colorLeft = colorRight; colorRight = tempColor;
                }

                // Ograniczenie do szerokości obrazu
                xLeft = Math.max(0, Math.min(width - 1, xLeft));
                xRight = Math.max(0, Math.min(width - 1, xRight));

                // Rysowanie scanline z interpolacją kolorów
                drawScanline(image, y, xLeft, xRight, colorLeft, colorRight);
            }
        }
    }

    // Pomocnicza metoda do interpolacji kolorów
    private Color interpolateColor(Color c1, Color c2, float t) {
        int r = Math.round(c1.getRed() + t * (c2.getRed() - c1.getRed()));
        int g = Math.round(c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
        int b = Math.round(c1.getBlue() + t * (c2.getBlue() - c1.getBlue()));

        return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b))
        );
    }

    // Metoda rysująca pojedynczą linię scanline z interpolacją kolorów
    private void drawScanline(BufferedImage image, int y, int xLeft, int xRight, Color colorLeft, Color colorRight) {
        float pixelCount = xRight - xLeft + 1;
        if (pixelCount <= 0) return;

        // Przyrosty kolorów
        float rIncrement = (colorRight.getRed() - colorLeft.getRed()) / pixelCount;
        float gIncrement = (colorRight.getGreen() - colorLeft.getGreen()) / pixelCount;
        float bIncrement = (colorRight.getBlue() - colorLeft.getBlue()) / pixelCount;

        // Początkowe wartości kolorów
        float r = colorLeft.getRed();
        float g = colorLeft.getGreen();
        float b = colorLeft.getBlue();

        // Wypełnienie wiersza pikselami z interpolowanymi kolorami
        for (int x = xLeft; x <= xRight; x++) {
            Color pixelColor = new Color(
                    Math.max(0, Math.min(255, Math.round(r))),
                    Math.max(0, Math.min(255, Math.round(g))),
                    Math.max(0, Math.min(255, Math.round(b)))
            );

            image.setRGB(x, y, pixelColor.getRGB());

            // Inkrementacja kolorów
            r += rIncrement;
            g += gIncrement;
            b += bIncrement;
        }
    }


    /**
     * Wykonuje cieniowanie Gourauda dla trójkąta bezpośrednio na ekranie
     * @param graphics obiekt Graphics do rysowania
     */
    public void gouraudShadeToScreen(Graphics graphics) {
        // Sprawdzenie czy obiekt Graphics istnieje
        if (graphics == null) {
            throw new IllegalArgumentException("Obiekt Graphics nie może być null!");
        }

        // Zakładamy, że obszar rysowania jest wystarczająco duży, więc nie sprawdzamy granic

        // Podział na dwa przypadki: trójkąt "płaski" i ogólny
        // Przypadek gdy środkowy wierzchołek ma inną wartość y niż górny i dolny
        if (y[0] != y[1] && y[1] != y[2]) {
            // Znajdź x dla punktu na krawędzi 0-2 o tej samej wysokości co środkowy wierzchołek
            float t = (float)(y[1] - y[0]) / (y[2] - y[0]);
            int x4 = Math.round(x[0] + t * (x[2] - x[0]));

            // Interpolacja koloru dla nowego punktu
            int r4 = Math.round(colors[0].getRed() + t * (colors[2].getRed() - colors[0].getRed()));
            int g4 = Math.round(colors[0].getGreen() + t * (colors[2].getGreen() - colors[0].getGreen()));
            int b4 = Math.round(colors[0].getBlue() + t * (colors[2].getBlue() - colors[0].getBlue()));
            Color color4 = new Color(
                    Math.max(0, Math.min(255, r4)),
                    Math.max(0, Math.min(255, g4)),
                    Math.max(0, Math.min(255, b4))
            );

            // Cieniowanie górnego trójkąta
            drawGouraudTrianglePartToScreen(graphics, x[0], y[0], x[1], y[1], x4, y[1],
                    colors[0], colors[1], color4);

            // Cieniowanie dolnego trójkąta
            drawGouraudTrianglePartToScreen(graphics, x[1], y[1], x4, y[1], x[2], y[2],
                    colors[1], color4, colors[2]);
        }
        // Przypadek gdy dwa wierzchołki mają tę samą wartość y
        else {
            // Jeśli górne dwa wierzchołki mają tę samą wartość y
            if (y[0] == y[1]) {
                drawGouraudTrianglePartToScreen(graphics, x[0], y[0], x[1], y[1], x[2], y[2],
                        colors[0], colors[1], colors[2]);
            }
            // Jeśli dolne dwa wierzchołki mają tę samą wartość y
            else {
                drawGouraudTrianglePartToScreen(graphics, x[0], y[0], x[1], y[1], x[2], y[2],
                        colors[0], colors[1], colors[2]);
            }
        }
    }

    /**
     * Cieniowanie Gourauda dla pojedynczej części trójkąta bezpośrednio na ekranie
     */
    private void drawGouraudTrianglePartToScreen(Graphics graphics, int x1, int y1, int x2, int y2, int x3, int y3,
                                                 Color c1, Color c2, Color c3) {

        // Wyznaczenie zakresu y do rysowania
        int yStart = y1;
        int yEnd = y3;

        // Dla każdego wiersza skanowania
        for (int y = yStart; y <= yEnd; y++) {
            // Współczynnik interpolacji dla krawędzi
            float beta = (y1 == y3) ? 0 : (float)(y - y1) / (y3 - y1);

            // Obliczenie lewej i prawej krawędzi
            int xLeft, xRight;
            Color colorLeft, colorRight;

            if (y < y2) {
                // Górna część trójkąta
                float t1 = (y1 == y2) ? 0 : (float)(y - y1) / (y2 - y1);
                float t2 = (y1 == y3) ? 0 : (float)(y - y1) / (y3 - y1);

                int xl = Math.round(x1 + t1 * (x2 - x1));
                int xr = Math.round(x1 + t2 * (x3 - x1));

                // Interpolacja kolorów
                int rl = Math.round(c1.getRed() + t1 * (c2.getRed() - c1.getRed()));
                int gl = Math.round(c1.getGreen() + t1 * (c2.getGreen() - c1.getGreen()));
                int bl = Math.round(c1.getBlue() + t1 * (c2.getBlue() - c1.getBlue()));

                int rr = Math.round(c1.getRed() + t2 * (c3.getRed() - c1.getRed()));
                int gr = Math.round(c1.getGreen() + t2 * (c3.getGreen() - c1.getGreen()));
                int br = Math.round(c1.getBlue() + t2 * (c3.getBlue() - c1.getBlue()));

                colorLeft = new Color(
                        Math.max(0, Math.min(255, rl)),
                        Math.max(0, Math.min(255, gl)),
                        Math.max(0, Math.min(255, bl))
                );

                colorRight = new Color(
                        Math.max(0, Math.min(255, rr)),
                        Math.max(0, Math.min(255, gr)),
                        Math.max(0, Math.min(255, br))
                );

                if (xl <= xr) {
                    xLeft = xl;
                    xRight = xr;
                } else {
                    xLeft = xr;
                    xRight = xl;
                    Color temp = colorLeft;
                    colorLeft = colorRight;
                    colorRight = temp;
                }
            } else {
                // Dolna część trójkąta
                float t1 = (y2 == y3) ? 0 : (float)(y - y2) / (y3 - y2);
                float t2 = (y1 == y3) ? 0 : (float)(y - y1) / (y3 - y1);

                int xl = Math.round(x2 + t1 * (x3 - x2));
                int xr = Math.round(x1 + t2 * (x3 - x1));

                // Interpolacja kolorów
                int rl = Math.round(c2.getRed() + t1 * (c3.getRed() - c2.getRed()));
                int gl = Math.round(c2.getGreen() + t1 * (c3.getGreen() - c2.getGreen()));
                int bl = Math.round(c2.getBlue() + t1 * (c3.getBlue() - c2.getBlue()));

                int rr = Math.round(c1.getRed() + t2 * (c3.getRed() - c1.getRed()));
                int gr = Math.round(c1.getGreen() + t2 * (c3.getGreen() - c1.getGreen()));
                int br = Math.round(c1.getBlue() + t2 * (c3.getBlue() - c1.getBlue()));

                colorLeft = new Color(
                        Math.max(0, Math.min(255, rl)),
                        Math.max(0, Math.min(255, gl)),
                        Math.max(0, Math.min(255, bl))
                );

                colorRight = new Color(
                        Math.max(0, Math.min(255, rr)),
                        Math.max(0, Math.min(255, gr)),
                        Math.max(0, Math.min(255, br))
                );

                if (xl <= xr) {
                    xLeft = xl;
                    xRight = xr;
                } else {
                    xLeft = xr;
                    xRight = xl;
                    Color temp = colorLeft;
                    colorLeft = colorRight;
                    colorRight = temp;
                }
            }

            // Optymalizacja z użyciem podejścia inkrementalnego
            float pixelCount = xRight - xLeft + 1;
            if (pixelCount > 0) {
                // Przyrosty kolorów
                float rIncrement = (colorRight.getRed() - colorLeft.getRed()) / pixelCount;
                float gIncrement = (colorRight.getGreen() - colorLeft.getGreen()) / pixelCount;
                float bIncrement = (colorRight.getBlue() - colorLeft.getBlue()) / pixelCount;

                // Początkowe wartości kolorów
                float r = colorLeft.getRed();
                float g = colorLeft.getGreen();
                float b = colorLeft.getBlue();

                // Wypełnienie wiersza pikselami z interpolowanymi kolorami
                for (int x = xLeft; x <= xRight; x++) {
                    Color pixelColor = new Color(
                            Math.max(0, Math.min(255, Math.round(r))),
                            Math.max(0, Math.min(255, Math.round(g))),
                            Math.max(0, Math.min(255, Math.round(b)))
                    );

                    graphics.setColor(pixelColor);
                    graphics.fillRect(x, y, 1, 1);

                    // Inkrementacja kolorów
                    r += rIncrement;
                    g += gIncrement;
                    b += bIncrement;
                }
            }
        }
    }
}
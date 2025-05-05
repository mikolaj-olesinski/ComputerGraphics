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

        // Sprawdzamy przypadek, gdy trójkąt jest zdegenerowany (wszystkie wierzchołki w jednej linii)
        if (y[0] == y[2]) {
            // Rysowanie poziomej linii z gradientem koloru
            int yPos = Math.max(0, Math.min(height - 1, y[0]));

            // Sortowanie wierzchołków według x
            int[] xSorted = {x[0], x[1], x[2]};
            Color[] cSorted = {colors[0], colors[1], colors[2]};

            // Proste sortowanie bąbelkowe
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2 - i; j++) {
                    if (xSorted[j] > xSorted[j + 1]) {
                        // Zamiana x
                        int tempX = xSorted[j];
                        xSorted[j] = xSorted[j + 1];
                        xSorted[j + 1] = tempX;

                        // Zamiana kolorów
                        Color tempC = cSorted[j];
                        cSorted[j] = cSorted[j + 1];
                        cSorted[j + 1] = tempC;
                    }
                }
            }

            // Rysowanie linii od lewego do prawego punktu
            drawScanline(image, yPos, xSorted[0], xSorted[2], cSorted[0], cSorted[2]);
            return;
        }

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

            // Cieniowanie górnego trójkąta (z płaskim dolnym bokiem)
            fillFlatTriangle(image, x[0], y[0], x[1], y[1], x4, y[1],
                    colors[0], colors[1], color4, width, height);

            // Cieniowanie dolnego trójkąta (z płaskim górnym bokiem)
            fillFlatTriangle(image, x[1], y[1], x4, y[1], x[2], y[2],
                    colors[1], color4, colors[2], width, height);
        }
        // Przypadek gdy dwa wierzchołki mają tę samą wartość y (trójkąt już jest "płaski")
        else {
            // Cieniowanie całego trójkąta
            fillFlatTriangle(image, x[0], y[0], x[1], y[1], x[2], y[2],
                    colors[0], colors[1], colors[2], width, height);
        }
    }

    // Uproszczona metoda do rysowania trójkąta, obsługująca tylko przypadki płaskich trójkątów
    private void fillFlatTriangle(BufferedImage image, int x1, int y1, int x2, int y2, int x3, int y3,
                                  Color c1, Color c2, Color c3, int width, int height) {
        // Przypadek trójkąta z płaskim górnym bokiem (y1 == y2 < y3)
        if (y1 == y2 && y2 < y3) {
            // Obliczamy odwrotność wysokości do interpolacji
            float invHeight = 1.0f / (y3 - y1);

            for (int y = Math.max(0, y1); y <= Math.min(height - 1, y3); y++) {
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
        // Przypadek trójkąta z płaskim dolnym bokiem (y1 < y2 == y3)
        else if (y1 < y2 && y2 == y3) {
            // Obliczamy odwrotność wysokości do interpolacji
            float invHeight = 1.0f / (y2 - y1);

            for (int y = Math.max(0, y1); y <= Math.min(height - 1, y2); y++) {
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
        // Dla poprawności - nie powinien nigdy wystąpić w naszym algorytmie
        else {
            System.err.println("Uwaga: Wywołanie fillFlatTriangle z trójkątem nie-płaskim!");
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
}
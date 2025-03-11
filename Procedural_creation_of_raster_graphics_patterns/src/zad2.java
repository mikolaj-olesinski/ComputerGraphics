/*
 * Computer graphics courses at Wroclaw University of Technology
 * (C) Wroclaw University of Technology, 2010
 *
 * Description:
 * This demo shows basic raster operations on raster image
 * represented by BufferedImage object. Image is created
 * on pixel-by-pixel basis and then stored in a file.  */

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class zad2 {
    public static void main(String[] args)
    {
        System.out.println("Image pattern overlay");

        // Default values
        String patternType = "grid"; // default pattern: rings, grid, or checkerboard

        // Parse command line arguments if provided
        if (args.length >= 1) {
            patternType = args[0].toLowerCase();
        }

        BufferedImage inputImage;

        try {
            inputImage = ImageIO.read(new File("images/input.jpg"));
            System.out.println("Obraz wczytany pomyślnie");
        } catch (IOException e) {
            System.out.println("Nie można wczytać obrazu: " + e.getMessage());
            return;
        }

        // Generate the appropriate pattern based on input
        if (patternType.equals("rings") || patternType.equals("a")) {
            System.out.println("Nakładanie wzoru pierścieni");
            applyRingPattern(inputImage);
        } else if (patternType.equals("grid") || patternType.equals("b")) {
            System.out.println("Nakładanie wzoru siatki");
            applyGridPattern(inputImage);
        } else if (patternType.equals("checkerboard") || patternType.equals("c")) {
            System.out.println("Nakładanie wzoru szachownicy");
            applyCheckerboardPattern(inputImage);
        } else {
            System.out.println("Nieznany typ wzoru. Dostępne opcje: rings, grid, checkerboard");
            System.out.println("Użycie domyślnego: grid");
            applyGridPattern(inputImage);
        }

        // Save the created image in the 'images' folder'         
        try {
            // Create 'images' directory if it doesn't exist             
            File imagesDir = new File("images");
            if (!imagesDir.exists()) {
                imagesDir.mkdir();
            }

            //Save the image in the 'images' folder             
            ImageIO.write(inputImage, "jpg", new File(imagesDir, "zad2_wynik.jpg"));
            System.out.println("Obraz z wzorem został utworzony pomyślnie w folderze 'images'");
        } catch (IOException e) {
            System.out.println("Nie można zapisać obrazu: " + e.getMessage());
        }
    }

    // Apply a ring pattern to the image
    private static void applyRingPattern(BufferedImage image) {
        // Image resolution
        int x_res = image.getWidth();
        int y_res = image.getHeight();

        // Ring center coordinates
        int x_c = x_res / 2;
        int y_c = y_res / 2;

        // Loop variables - indices of the current row and column
        int i, j;

        int color = int2RGB(255, 0, 0); // Czerwony

        // Fixed ring width
        final int w = 50;

        // Process the image, pixel by pixel
        for (i = 0; i < y_res; i++)
            for (j = 0; j < x_res; j++) {
                double d = Math.sqrt((double)(i - y_c)*(i - y_c) + (j - x_c)*(j - x_c));

                if (Math.abs(Math.sin(Math.PI * d / w)) < 0.2) {
                    image.setRGB(j, i, color);
                }
            }
    }

    // Apply a grid pattern to the image
    private static void applyGridPattern(BufferedImage image) {
        // Image resolution
        int x_res = image.getWidth();
        int y_res = image.getHeight();

        // Loop variables - indices of the current row and column
        int i, j;

        // Grid parameters
        int replacementColor = int2RGB(255, 0, 0); // Czerwony
        int gridWidth = 10;
        int spacing = 100;

        // Process the image, pixel by pixel
        for (i = 0; i < y_res; i++)
            for (j = 0; j < x_res; j++) {
                // Check if we are in the grid area and set the color
                if ((i % (spacing + gridWidth) < gridWidth) || (j % (spacing + gridWidth) < gridWidth)) {
                    image.setRGB(j, i, replacementColor);
                }
            }
    }

    // Apply a checkerboard pattern to the image
    private static void applyCheckerboardPattern(BufferedImage image) {
        // Image resolution
        int x_res = image.getWidth();
        int y_res = image.getHeight();

        // Loop variables - indices of the current row and column
        int i, j;

        // Checkerboard parameters
        int replacementColor = int2RGB(255, 255, 255); // Czerwony
        int fieldSize = 100;
        int row, col;

        // Process the image, pixel by pixel
        for (i = 0; i < y_res; i++)
            for (j = 0; j < x_res; j++) {
                row = i / fieldSize;
                col = j / fieldSize;

                if ((row + col) % 2 == 0) {
                    // Only replace the border of each square to preserve the original image content
                    if (i % fieldSize < 5 || i % fieldSize >= fieldSize - 5 ||
                            j % fieldSize < 5 || j % fieldSize >= fieldSize - 5) {
                        image.setRGB(j, i, replacementColor);
                    }
                }
            }
    }

    // This method assembles RGB color intensities into single     
    // packed integer. Arguments must be in <0..255> range     
    static int int2RGB(int red, int green, int blue)
    {
        // Make sure that color intensities are in 0..255 range         
        red = red & 0x000000FF;
        green = green & 0x000000FF;
        blue = blue & 0x000000FF;

        // Assemble packed RGB using bit shift operations         
        return (red << 16) + (green << 8) + blue;
    }
}
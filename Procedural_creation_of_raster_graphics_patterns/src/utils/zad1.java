package utils;/*
 * Computer graphics courses at Wroclaw University of Technology
 * (C) Wroclaw University of Technology, 2010
 *
 * Description:
 * This demo shows basic raster operations on raster image
 * represented by BufferedImage object. Image is created
 * on pixel-by-pixel basis and then stored in a file.
 */

import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class zad1 {
    public static void main(String[] args) {

        // Default values
        int x_res = 2000;
        int y_res = 2000;
        String outputFile = "output.bmp";
        String patternType = "rings";

        // Parse command line arguments if provided
        if (args.length >= 2) {
            try {
                x_res = Integer.parseInt(args[0].trim());
                y_res = Integer.parseInt(args[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid resolution arguments. Using defaults: 2000x2000");
            }
        }

        if (args.length >= 3) {
            patternType = args[2].toLowerCase();
        }

        if (args.length >= 4) {
            outputFile = args[3];
        }

        // Generate the appropriate pattern based on input
        if (patternType.equals("rings") || patternType.equals("a")) {
            System.out.println("Generating ring pattern");

            // Default ring width
            int w = 50;

            // If ring width is specified
            if (args.length >= 5) {
                try {
                    w = Integer.parseInt(args[4].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid ring width. Using default: 50");
                }
            }

            System.out.println("Ring width: " + w);
            generateRingPattern(x_res, y_res, outputFile, w);

        } else if (patternType.equals("grid") || patternType.equals("b")) {
            System.out.println("Generating grid pattern");

            // Default grid parameters
            int gridColor = 0x000000; // black
            int bgColor = 0xFFFFFF;   // white
            int gridWidth = 50;
            int spacing = 100;

            // Parse additional arguments if provided
            if (args.length >= 5) {
                try {
                    gridColor = Integer.parseInt(args[4].trim(), 16);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid grid color. Using default: black");
                }
            }

            if (args.length >= 6) {
                try {
                    bgColor = Integer.parseInt(args[5].trim(), 16);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid background color. Using default: white");
                }
            }

            if (args.length >= 7) {
                try {
                    gridWidth = Integer.parseInt(args[6].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid grid width. Using default: 50");
                }
            }

            if (args.length >= 8) {
                try {
                    spacing = Integer.parseInt(args[7].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid spacing. Using default: 100");
                }
            }

            System.out.println("Grid color: 0x" + Integer.toHexString(gridColor));
            System.out.println("Background color: 0x" + Integer.toHexString(bgColor));
            System.out.println("Grid width: " + gridWidth);
            System.out.println("Spacing: " + spacing);

            generateGridPattern(x_res, y_res, outputFile, gridColor, bgColor, gridWidth, spacing);

        } else if (patternType.equals("checkerboard") || patternType.equals("c")) {
            System.out.println("Generating checkerboard pattern");

            // Default checkerboard parameters
            int firstColor = 0x000000; // black
            int secondColor = 0xFFFFFF; // white
            int fieldSize = 200;

            // Parse additional arguments if provided
            if (args.length >= 5) {
                try {
                    firstColor = Integer.parseInt(args[4].trim(), 16);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid first color. Using default: black");
                }
            }

            if (args.length >= 6) {
                try {
                    secondColor = Integer.parseInt(args[5].trim(), 16);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid second color. Using default: white");
                }
            }

            if (args.length >= 7) {
                try {
                    fieldSize = Integer.parseInt(args[6].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid field size. Using default: 200");
                }
            }

            System.out.println("First color: 0x" + Integer.toHexString(firstColor));
            System.out.println("Second color: 0x" + Integer.toHexString(secondColor));
            System.out.println("Field size: " + fieldSize);

            generateCheckerboardPattern(x_res, y_res, outputFile, firstColor, secondColor, fieldSize);

        } else {
            System.out.println("Unknown pattern type. Available options: rings, grid, checkerboard");
            System.out.println("Using default: rings");
            generateRingPattern(x_res, y_res, outputFile, 50);
        }
    }

    // Generate a ring pattern (from zad1.zad1a)
    private static void generateRingPattern(int x_res, int y_res, String outputFile, int w) {
        System.out.println("Ring pattern synthesis");

        BufferedImage image;

        // Ring center coordinates
        int x_c, y_c;

        // Loop variables - indices of the current row and column
        int i, j;

        // Initialize an empty image, use pixel format
        // with RGB packed in the integer data type
        image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);

        // Find coordinates of the image center
        x_c = x_res / 2;
        y_c = y_res / 2;

        int color;

        // Process the image, pixel by pixel
        for (i = 0; i < y_res; i++)
            for (j = 0; j < x_res; j++) {
                double d = Math.sqrt((double)(i - y_c)*(i - y_c) +
                        (j - x_c)*(j - x_c));

                double angle = Math.PI * d / w;
                int intensity = (int)(128 * (Math.sin(angle) + 1));
                intensity = Math.min(255, Math.max(0, intensity));
                color = int2RGB(intensity, intensity, intensity);
                image.setRGB(j, i, color);
            }

        // Save the created image
        saveImage(image, outputFile);
    }

    // Generate a grid pattern (from zad1.zad1b)
    private static void generateGridPattern(int x_res, int y_res, String outputFile,
                                            int gridColor, int bgColor, int gridWidth, int spacing) {
        System.out.println("Grid pattern synthesis");

        BufferedImage image;

        // Loop variables - indices of the current row and column
        int i, j;

        // Initialize an empty image, use pixel format
        // with RGB packed in the integer data type
        image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);

        // Process the image, pixel by pixel
        for (i = 0; i < y_res; i++)
            for (j = 0; j < x_res; j++) {
                if ((i % (spacing + gridWidth) < gridWidth) || (j % (spacing + gridWidth) < gridWidth)) {
                    image.setRGB(j, i, gridColor);
                } else {
                    image.setRGB(j, i, bgColor);
                }
            }

        // Save the created image
        saveImage(image, outputFile);
    }

    // Generate a checkerboard pattern (from zad1.zad1c)
    private static void generateCheckerboardPattern(int x_res, int y_res, String outputFile,
                                                    int firstColor, int secondColor, int fieldSize) {
        System.out.println("Checkerboard pattern synthesis");

        BufferedImage image;

        // Loop variables - indices of the current row and column
        int i, j;

        // Initialize an empty image, use pixel format
        // with RGB packed in the integer data type
        image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);

        int row, col;

        // Process the image, pixel by pixel
        for (i = 0; i < y_res; i++)
            for (j = 0; j < x_res; j++) {
                row = i / fieldSize;
                col = j / fieldSize;

                if ((row + col) % 2 == 0) {
                    image.setRGB(j, i, firstColor);
                } else {
                    image.setRGB(j, i, secondColor);
                }
            }

        // Save the created image
        saveImage(image, outputFile);
    }

    // Save the image to the specified file
    private static void saveImage(BufferedImage image, String fileName) {
        try {
            // Create the 'images' directory if it does not exist
            File imagesDir = new File("images");
            if (!imagesDir.exists()) {
                imagesDir.mkdir();
            }

            // Save the image inside the 'images' folder
            File outputFile = new File(imagesDir, fileName);
            ImageIO.write(image, "bmp", outputFile);
            System.out.println("Image created successfully in 'images' folder as " + fileName);
        } catch (IOException e) {
            System.out.println("The image cannot be stored: " + e.getMessage());
        }
    }

    // This method assembles RGB color intensities into single
    // packed integer. Arguments must be in <0..255> range
    static int int2RGB(int red, int green, int blue) {
        // Make sure that color intensities are in 0..255 range
        red = red & 0x000000FF;
        green = green & 0x000000FF;
        blue = blue & 0x000000FF;

        // Assemble packed RGB using bit shift operations
        return (red << 16) + (green << 8) + blue;
    }
}


//examples
//java utils.zad1 [szerokość] [wysokość] rings [nazwa_pliku] [szerokość_pierścienia]
//java utils.zad1 2000 2000 rings rings.bmp 30

//java utils.zad1 [szerokość] [wysokość] grid [nazwa_pliku] [kolor_siatki] [kolor_tła] [szerokość_siatki] [odstęp]
//java utils.zad1 2000 2000 grid grid.bmp 000000 FFFFFF 50 100

//java utils.zad1 [szerokość] [wysokość] checkerboard [nazwa_pliku] [kolor1] [kolor2] [rozmiar_pola]
//java utils.zad1 2000 2000 checkerboard checker.bmp 000000 FFFFFF 200
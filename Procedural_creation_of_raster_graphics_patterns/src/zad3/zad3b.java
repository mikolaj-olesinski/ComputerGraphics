package zad3;
/*
 * Computer graphics courses at Wroclaw University of Technology
 * (C) Wroclaw University of Technology, 2010
 *
 * Description:
 * This demo shows basic raster operations on raster image
 * represented by BufferedImage object. Image is created
 * on pixel-by-pixel basis and then stored in a file.
 */

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class zad3b
{
    public static void main(String[] args)
    {
        System.out.println("Ring pattern synthesis");

        BufferedImage image;

        // Image resolution
        int x_res, y_res;

        // Predefined black and white RGB representations
        // packed as integers
        int black, white;

        // Loop variables - indices of the current row and column
        int i, j;


        // Get required image resolution from command line arguments
//        x_res = Integer.parseInt( args[0].trim() );
//        y_res = Integer.parseInt( args[1].trim() );
        x_res = 2000;
        y_res = 2000;

        // Initialize an empty image, use pixel format
        // with RGB packed in the integer data type
        image = new BufferedImage( x_res, y_res, BufferedImage.TYPE_INT_RGB);

        // Create packed RGB representation of black and white colors
        black = int2RGB(0, 0, 0);
        white = int2RGB(255, 255, 255);

        int firstColor = white;
        int secondColor = black;
        int fieldSize = 100;
        int centerX = x_res / 2;
        int centerY = y_res / 2;


        // Process the image, pixel by pixel
        for (i = 0; i < y_res; i++) {
            for (j = 0; j < x_res; j++) {
                // Obliczenie obróconych współrzędnych z offsetem
                int u = j - centerX;
                int v = i - centerY;

                // Obrót o 45 stopni (π/4 radianów)
                double angle = Math.PI / 4.0;
                double rotatedU = u * Math.cos(angle) - v * Math.sin(angle);
                double rotatedV = u * Math.sin(angle) + v * Math.cos(angle);

                // Określenie, w którym polu szachownicy znajduje się piksel
                int fieldX = (int)Math.floor(rotatedU / fieldSize);
                int fieldY = (int)Math.floor(rotatedV / fieldSize);

                // Określenie koloru na podstawie parzystości sumy współrzędnych pola
                if ((fieldX + fieldY) % 2 == 0) {
                    image.setRGB(j, i, firstColor);
                } else {
                    image.setRGB(j, i, secondColor);
                }
            }
        }

        // Save the created image in the 'images' folder
        try
        {
            // Create the 'images' directory if it does not exist
            File imagesDir = new File("images");
            if (!imagesDir.exists()) {
                imagesDir.mkdir();
            }

            // Save the image as "zad1.zad1a.bmp" inside the 'images' folder
            ImageIO.write(image, "bmp", new File(imagesDir, "zad3b.bmp"));
            System.out.println("Ring image created successfully in 'images' folder");
        }
        catch (IOException e)
        {
            System.out.println("The image cannot be stored");
        }
    }

    // This method assembles RGB color intensities into single
    // packed integer. Arguments must be in <0..255> range
    static int int2RGB( int red, int green, int blue)
    {
        // Make sure that color intensities are in 0..255 range
        red = red & 0x000000FF;
        green = green & 0x000000FF;
        blue = blue & 0x000000FF;

        // Assemble packed RGB using bit shift operations
        return (red << 16) + (green << 8) + blue;
    }
}

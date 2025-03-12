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

public class zad3a
{
    public static void main(String[] args)
    {
        System.out.println("Ring pattern synthesis");

        BufferedImage image;

        // Image resolution
        int x_res, y_res;

        // Predefined black and white RGB representations
        // packed as integers
        int black, gray;

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

        // Create packed RGB representation of black and gray color;
        black = int2RGB(0, 0, 0);
        gray = int2RGB(180, 180, 180);


        // initial values
        int firstColor = gray;
        int secondColor = black;
        int fieldSize = 200;
        int radius = 80;

        //initial row, column, centerX, centerY, dx (delta x), dy (delta y)
        int row, col, centerX, centerY, dx, dy;

        // Process the image, pixel by pixel
        for (i = 0; i < y_res; i++) {
            for (j = 0; j < x_res; j++) {

                // Calculate the row and column of the current pixel
                row = i / fieldSize;
                col = j / fieldSize;

                // Calculate the center of the current field
                centerX = col * fieldSize + fieldSize / 2;
                centerY = row * fieldSize + fieldSize / 2;

                // Calculate the distance from the center of the field
                dx = j - centerX;
                dy = i - centerY;

                // Make decision on the pixel color
                if (dx * dx + dy * dy <= radius * radius) {
                    //If the pixel is inside the circle, set the second color
                    image.setRGB(j, i, secondColor);
                } else {
                    image.setRGB(j, i, firstColor);
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

            // Save the image inside the 'images' folder
            ImageIO.write(image, "bmp", new File(imagesDir, "zad3a.bmp"));
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

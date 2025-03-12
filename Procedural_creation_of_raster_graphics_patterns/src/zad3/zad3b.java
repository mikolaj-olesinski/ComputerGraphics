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

        //initial values
        int firstColor = white;
        int secondColor = black;
        int fieldSize = 100;



        // Process the image, pixel by pixel
        for (i = 0; i < y_res; i++) {
            for (j = 0; j < x_res; j++) {


                //Roate the point by 45 degrees
                double angle = Math.PI / 4.0;
                double rotatedX = j * Math.cos(angle) - i * Math.sin(angle);
                double rotatedY = j * Math.sin(angle) + i * Math.cos(angle);

                // Calculate the field coordinates
                int fieldX = (int)Math.floor(rotatedX / fieldSize);
                int fieldY = (int)Math.floor(rotatedY / fieldSize);

                // Make decision on the pixel color
                if ((fieldX + fieldY) % 2 == 0) {
                    //If the sum of the field coordinates is even, set the first color
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

            // Save the image inside the 'images' folder
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

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

import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class zad3c
{
    public static void main(String[] args) {
        System.out.println("Ring pattern synthesis");

        BufferedImage image;

        // Image resolution
        int x_res, y_res;

        // Ring center coordinates
        int x_c, y_c;

        // Loop variables - indices of the current row and column
        int i, j;

        int black, white;

        black = int2RGB(0, 0, 0);
        white = int2RGB(255, 255, 255);


        // Get required image resolution from command line arguments
//        x_res = Integer.parseInt( args[0].trim() );
//        y_res = Integer.parseInt( args[1].trim() );
        x_res = 2000;
        y_res = 2000;

        // Initialize an empty image, use pixel format
        // with RGB packed in the integer data type
        image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);


        // Find coordinates of the image center
        x_c = x_res / 2;
        y_c = y_res / 2;

        //Calculate the maximum distance from the center of the image
        double d_max = Math.sqrt(x_c * x_c + y_c * y_c);

        // Initial values for the highest and lowest width of the ring
        double min_w = 5;
        double max_w = 15;

        // Process the image, pixel by pixel
        for (i = 0; i < y_res; i++){
            for (j = 0; j < x_res; j++) {

                //Calculate distance to the image center
                double d = Math.sqrt((i - y_c) * (i - y_c) + (j - x_c) * (j - x_c));

                // Interpolate the width of the ring
                double w = min_w + (max_w - min_w) * (d / d_max);

                // Find the ring index
                int r = (int)(d / w);

                // Make decision on the pixel color
                if (r % 2 == 0) {
                    //If the ring index is even, set the black color
                    image.setRGB(j, i, black);
                }
                else {
                    image.setRGB(j, i, white);
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

            // Save the image as inside the 'images' folder
            ImageIO.write(image, "bmp", new File(imagesDir, "zad3c.bmp"));
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

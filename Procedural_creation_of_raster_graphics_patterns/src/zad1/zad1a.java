package zad1;
/*
 * Computer graphics courses at Wroclaw University of Technology
 * (C) Wroclaw University of Technology, 2010
 *
 * Description:
 * This demo shows basic raster operations on raster image
 * represented by BufferedImage object. Image is created
 * on pixel-by-pixel basis and then stored in a file.
 */

//Example
//javac -d out src/zad1/zad1a.java
//java -cp out zad1.zad1a 2000 2000 50
//java <x_res> <y_res> <w>

import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class zad1a
{
    public static void main(String[] args)
    {
        System.out.println("Ring pattern synthesis");

        BufferedImage image;

        // Image resolution
        int x_res, y_res;

        // Ring center coordinates
        int x_c, y_c;

        // Loop variables - indices of the current row and column
        int i, j;


        // Get required image resolution from command line arguments
//        x_res = Integer.parseInt( args[0].trim() );
//        y_res = Integer.parseInt( args[1].trim() );

        //Default image resolution values
        x_res = 2000;
        y_res = 2000;

        // Default ring width
        int w = 50;


        // Override defaults with command line arguments if provided
        try {
            // Check if resolution is provided
            if (args.length >= 2) {
                x_res = Integer.parseInt(args[0].trim());
                y_res = Integer.parseInt(args[1].trim());
            }

            // Check if ring width is provided
            if (args.length >= 3) {
                w = Integer.parseInt(args[2].trim());
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid command line arguments. Using default values.");
        }

        // Initialize an empty image, use pixel format
        // with RGB packed in the integer data type
        image = new BufferedImage(x_res, y_res, BufferedImage.TYPE_INT_RGB);


        // Find coordinates of the image center
        x_c = x_res / 2;
        y_c = y_res / 2;

        int  color;

        // Process the image, pixel by pixel
        for ( i = 0; i < y_res; i++)
            for ( j = 0; j < x_res; j++)
            {
                //Calculate distance to the image center
                double d = Math.sqrt((double)(i - y_c)*(i - y_c) + (j - x_c)*(j - x_c));

                //Calculate intensity of the pixel
                double angle = Math.PI * d / w;
                int intensity = (int)(128 * (Math.sin(angle) + 1));
                intensity = Math.min(255, Math.max(0, intensity));

                // Set the pixel value
                color = int2RGB(intensity, intensity, intensity);
                image.setRGB( j, i, color );
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
            ImageIO.write(image, "bmp", new File(imagesDir, "zad1a.bmp"));
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

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
//javac -d out src/zad1/za1b.java
//java -cp out zad1.zad1b 2000 2000 50 100 100 0 0 0 255 255 255
//java <x_res> <y_res> <grid_width> <spacing_x> <spacing_y> <grid_red> <grid_green> <grid_blue> <bg_red> <bg_green> <bg_blue>
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class zad1b
{
    public static void main(String[] args)
    {
        System.out.println("Grid pattern synthesis");

        BufferedImage image;

        // Image resolution
        int x_res, y_res;

        // Default values
        x_res = 2000;
        y_res = 2000;

        // Default grid parameters
        int gridWidth = 50;      // Width of grid lines
        int spacingX = 100;      // Distance between grid lines along X axis
        int spacingY = 100;      // Distance between grid lines along Y axis

        // Default colors (black grid on white background)
        int gridRed = 0, gridGreen = 0, gridBlue = 0;  // Grid color (black)
        int bgRed = 255, bgGreen = 255, bgBlue = 255;  // Background color (white)

        // Parse command line arguments
        try {
            // Parse resolution
            if (args.length >= 2) {
                x_res = Integer.parseInt(args[0].trim());
                y_res = Integer.parseInt(args[1].trim());
            }

            // Parse grid parameters
            if (args.length >= 3) {
                gridWidth = Integer.parseInt(args[2].trim());
            }

            if (args.length >= 5) {
                spacingX = Integer.parseInt(args[3].trim());
                spacingY = Integer.parseInt(args[4].trim());
            }

            // Parse grid color (RGB)
            if (args.length >= 8) {
                gridRed = Integer.parseInt(args[5].trim());
                gridGreen = Integer.parseInt(args[6].trim());
                gridBlue = Integer.parseInt(args[7].trim());
            }

            // Parse background color (RGB)
            if (args.length >= 11) {
                bgRed = Integer.parseInt(args[8].trim());
                bgGreen = Integer.parseInt(args[9].trim());
                bgBlue = Integer.parseInt(args[10].trim());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid command line arguments. Using default values.");
        }


        // Initialize an empty image, use pixel format
        // with RGB packed in the integer data type
        image = new BufferedImage( x_res, y_res, BufferedImage.TYPE_INT_RGB);

        // Loop variables - indices of the current row and column
        int i, j;

        int gridColor = int2RGB(gridRed, gridGreen, gridBlue);
        int bgColor = int2RGB(bgRed, bgGreen, bgBlue);

        // Process the image, pixel by pixel
        for ( i = 0; i < y_res; i++){

            for ( j = 0; j < x_res; j++) {

                // Make decision on the pixel color
                if ((i % (spacingY + gridWidth) < gridWidth) || (j % (spacingX + gridWidth) < gridWidth))
                {
                    // Check if we are in the grid area and set the color
                    image.setRGB(j, i, gridColor);
                } else
                {
                    image.setRGB(j, i, bgColor);
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
            ImageIO.write(image, "bmp", new File(imagesDir, "zad1b.bmp"));
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

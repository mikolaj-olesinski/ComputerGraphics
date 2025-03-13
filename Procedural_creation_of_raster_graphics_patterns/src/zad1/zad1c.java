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

//javac -d out src/zad1/zad1c.java
//java -cp out zad1.zad1c 2000 2000 200 0 0 0 255 255 255
//java <x_res> <y_res> <fieldSize> <main_Red> <main_Green> <main_Blue> <bgRed> <bgGreen> <bgBlue>

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class zad1c
{
    public static void main(String[] args)
    {
        System.out.println("Checkboard pattern synthesis");

        BufferedImage image;


        // Image resolution
        int x_res, y_res;

        // Default values
        x_res = 2000;
        y_res = 2000;

        // Default checkerboard parameters
        int fieldSize = 200;      // Size of the checkerboard field

        // Default colors
        int main_Red = 0, main_Green = 0, main_Blue = 0;  // Grid color (black)
        int bgRed = 255, bgGreen = 255, bgBlue = 255;  // Background color (white)


        // Parse command line arguments
        try {
            // Parse resolution
            if (args.length >= 2) {
                x_res = Integer.parseInt(args[0].trim());
                y_res = Integer.parseInt(args[1].trim());
            }

            // Parse field size
            if (args.length >= 3) {
                fieldSize = Integer.parseInt(args[2].trim());
            }

            // Parse grid color (RGB)
            if (args.length >= 6) {
                main_Red = Integer.parseInt(args[3].trim());
                main_Green = Integer.parseInt(args[4].trim());
                main_Blue = Integer.parseInt(args[5].trim());
            }

            // Parse background color (RGB)
            if (args.length >= 9) {
                bgRed = Integer.parseInt(args[6].trim());
                bgGreen = Integer.parseInt(args[7].trim());
                bgBlue = Integer.parseInt(args[8].trim());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid command line arguments. Using default values.");
        }


        // Initialize an empty image, use pixel format
        // with RGB packed in the integer data type
        image = new BufferedImage( x_res, y_res, BufferedImage.TYPE_INT_RGB);


        // Loop variables - indices of the current row and column
        int i, j;

        //Initial row and column
        int row, col;

        //Initial color
        int firstColor = int2RGB(main_Red, main_Green, main_Blue);
        int secondColor = int2RGB(bgRed, bgGreen, bgBlue);

        // Process the image, pixel by pixel
        for ( i = 0; i < y_res; i++)
            for ( j = 0; j < x_res; j++)
            {
                // Calculate the row and column of the current pixel
                row = i / fieldSize;
                col = j / fieldSize;

                //Make the decision on pixel color
                if ((row + col) % 2 == 0)
                {
                    //If the row plus column is even, set the first color
                    image.setRGB(j, i, firstColor);
                } else
                {
                    image.setRGB(j, i, secondColor);
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
            ImageIO.write(image, "bmp", new File(imagesDir, "zad1c.bmp"));
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

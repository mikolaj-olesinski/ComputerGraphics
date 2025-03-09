/*
 * Computer graphics courses at Wroclaw University of Technology
 * (C) by Jurek Sas, 2009
 *
 * Description:
 *   This demo shows basic raster operations on raset image
 *   represented by BufferedImage object. Image is created
 *   on pixel-by-pixel basis and then stored in a file.
 *
 *   Ten program demonstracyjny pokazuje sposób wykonywania
 *   podstawowych operacji grafiki rastrowej z uzyciem klasy
 *   BufferedImage. Wyjaśniono wykonywanie podstawowych operacji
 *   rastrowych na obiekcie BufefredImage
 */

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class zad1c {

    /**
     * @param args
     */
    private static Scanner in;

    public static void main(String[] args)
    {
        String imagesDir = "images";

        BufferedImage  image;

        image = new BufferedImage( 2000, 2000,
                BufferedImage.TYPE_INT_RGB );

        int  color;
        int  i, j;
        int  height = image.getHeight();
        int  width  = image.getWidth();

        int black = byte2RGB(0, 0, 0);
        int white = byte2RGB(255, 255, 255);

        int firstColor = black;
        int secondColor = white;
        int fieldSize = 200;
        int row, col;

        for (i = 0; i < height; i++) {
            for (j = 0; j < width; j++) {

                row = i / fieldSize;
                col = j / fieldSize;

                if ((row + col) % 2 == 0)
                {
                    image.setRGB(j, i, firstColor);
                } else
                {
                    image.setRGB(j, i, secondColor);
                }

            }
        }

        try
        {
            File grayImageFile = new File(imagesDir + File.separator + "zad1c.bmp");
            ImageIO.write(image, "bmp", grayImageFile);
            System.out.println("Gray image created successfully at: " + grayImageFile.getAbsolutePath());
        }
        catch (IOException e)
        {
            System.out.println("Gray image cannot be stored in BMP file: " + e.getMessage());
        };


        System.exit( 0 );
    }

    static int byte2RGB( int red, int green, int blue)
    {
        // Color components must be in range 0 - 255
        red   = 0xff & red;
        green = 0xff & green;
        blue  = 0xff & blue;
        return (red << 16) + (green << 8) + blue;
    }







    //=======================================================================
    //=======================================================================
    // Console functions - not strictly related to CG but make the work easier
    //=======================================================================
    //=======================================================================

    static void writeln( String stg )
    {
        System.out.println( stg );
    }

    static void readln()
    {
        try
        {
            while( System.in.read() != '\n' );
        }
        catch( Throwable obj )
        {
        }
    }

    static String readStr()	{
        return in.next();
    }

    static int readInt()
    {
        return (in.nextInt());
    }

    static double readDouble()
    {
        return (in.nextDouble() );
    }
}

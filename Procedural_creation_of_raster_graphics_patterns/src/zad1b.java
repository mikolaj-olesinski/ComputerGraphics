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

public class zad1b {

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

        int gridColor = black;
        int bgColor = white;
        int gridWidth = 50;
        int spacing = 100;

        BufferedImage  input_image;
        String image_name = "images/lena.bmp";
        try
        {
            input_image = ImageIO.read( new File( image_name ) );
        }
        catch (IOException e) {
            System.out.println( "Cannot read this image" );
        }

        for (i = 0; i < height; i++) {
            for (j = 0; j < width; j++) {

                if ((i % (spacing + gridWidth) < gridWidth)  || (j % (spacing + gridWidth) < gridWidth))
                {
                    image.setRGB(j, i, gridColor);
                } else
                {
                    image.setRGB(j, i, bgColor);
                }

            }
        }

        try
        {
            File grayImageFile = new File(imagesDir + File.separator + "zad1b.bmp");
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

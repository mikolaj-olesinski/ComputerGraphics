package utils;/*
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
 *   BufferedImage. Wyjaœniono wykonywanie podstawowych operacji
 *   rastrowych na obiekcie BufefredImage
 */

import java.io.*;
import java.util.Random;
import java.util.Scanner;
import java.awt.image.*;
import java.awt.Color;
import javax.imageio.*;

public class Demo0 {

	/**
	 * @param args
	 */
	private static Scanner in;

	public static void main(String[] args)
	{
    	String  dirname = "c:\\GK";
		String imagesDir = "images";

		// TODO Auto-generated method stub
      System.out.println("help.Demo0 just started" );


	   BufferedImage  image;

      // Reading image from the file. By using ImageIO
      // =================================================================
		String         image_name;
		BufferedImage  input_image;
		int img_width, img_height;


	   // Create an empty image
	   image = new BufferedImage( 2000, 2000,
	      		                   BufferedImage.TYPE_INT_RGB );

	   // Fill it with a gray-shaded pattern
	   int  color;
		int  gray;
		int  i, j;
		double  a, f;
		a = 20.0;   f = 5.0;
		int  w = 30;
		int  height = image.getHeight();
		int  width  = image.getWidth();

		color = 0;
		w = width;
		int  w1 = 50;
		int ri = 50;
		int ro = 150;
      Random rand = new Random();
		for ( i = 0; i < height; i++)
		   for ( j = 0; j < width; j++)
		   {

		   //color = byte2RGB( 255, 255, 255 );


            // Alternately Color class can be used to assemble INT_RGB
            // gray = 45;
            // color = (new Color( gray, gray, gray  )).getRGB();

            // IN order to read R,G,B from a pixel
		  	image.setRGB( j, i, color );


            // IN order to read R,G,B from a pixel
		    int  in_c = image.getRGB(i, j);
		  	Color cc_in = new Color( in_c );
            int rc = cc_in.getRed();
            int gc = cc_in.getGreen();
		  	int bc = cc_in.getBlue();


		   }

		// Create images directory if it doesn't exist
		File imagesFolder = new File(imagesDir);
		if (!imagesFolder.exists()) {
			imagesFolder.mkdir();
			System.out.println("Created directory: " + imagesDir);
		}

		// Save image in graphics file
		try
		{
			File grayImageFile = new File(imagesDir + File.separator + "out_img_gray.bmp");
			ImageIO.write(image, "bmp", grayImageFile);
			System.out.println("Gray image created successfully at: " + grayImageFile.getAbsolutePath());
		}
		catch (IOException e)
		{
			System.out.println("Gray image cannot be stored in BMP file: " + e.getMessage());
		};

      // Now make color image
		for ( i = 0; i < height; i++)
		   for ( j = 0; j < width; j++)
		   {
			   gray = (byte)(j % 256);
	         color = byte2RGB( gray, (256 - gray), (i%256) );
		      image.setRGB( j, i, color );
		   }

		// Save image in graphics file
		try
		{
			File colorImageFile = new File(imagesDir + File.separator + "out_img_color.jpg");
			ImageIO.write(image, "jpg", colorImageFile);
			System.out.println("Color image created successfully at: " + colorImageFile.getAbsolutePath());
		}
		catch (IOException e)
		{
			System.out.println("Color image cannot be stored in JPG file: " + e.getMessage());
		};


//      // Not used here but you should know how ...
//      // Acquire the list of formats currently supported by ImageIO
//      String  formats[] = ImageIO.getReaderFormatNames();
//      writeln( "You can use the following image file formats:" );
//      for ( i = 0; i < formats.length; i++ )
//         writeln( formats[i] );
//
//      // Not used here but you should know how ...
//      // Lines below show how to query supported file types
//      // and how to read a graphic file
//      in = new Scanner(System.in);
//      input_image = null;
//      writeln( "Type the image location and name to read:" );
//      image_name = readStr();
//
//      try
//      {
//          input_image = ImageIO.read( new File( image_name ) );
//      }
//      catch (IOException e) {
//         System.out.println( "Cannot read this image" );
//      }

      /*
      // IN order to read R,G,B from a pixel
      int  in_c = input_image.getRGB(i, j);
      Color cc_in = new Color( in_c );
      int rc = cc_in.getRed();
      int gc = cc_in.getGreen();
      int bc = cc_in.getBlue();
      */
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

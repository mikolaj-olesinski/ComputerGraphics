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

import java.util.Scanner;
import java.awt.Color;
import java.lang.Math;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
 *   BufferedImage. Wyjaœniono wykonywanie podstawowych operacji
 *   rastrowych na obiekcie BufefredImage
 */

import javax.imageio.ImageIO;


public class Demo0a {
   public static void main(String[] args) {
      BufferedImage  image;

      image = new BufferedImage( 256, 256, BufferedImage.TYPE_INT_RGB );


      // Fill it with a gray-shaded pattern 
      int  color;
      int  gray;
      int  w = 30;
      int  height = image.getHeight();
      int  width  = image.getWidth();

      color = 0;
      w = width;
      int  w1 = 50;
      int ri = 50;
      int ro = 150;

      for ( int i = 0; i < height; i++)
         for ( int j = 0; j < width; j++)
         {
            double d = Math.sqrt((double)(i - width/2)*(i-width/2) +
                    (j-height/2)*(j-height/2));
            ri = ((int)d)/20;

            double angle = Math.atan2((double)(j-height/2), (double)(i-width/2)) + 3.1415;
            int si = (int)(8*angle/(2*3.1415));
            if ( (ri%2) == (si%2) )
            {
               //color = byte2RGB( 0, 0, 0 );
               color = new Color( 255, 255, 0 ).getRGB();
            }
            else
               color = new Color( 0, 0, 255 ).getRGB();
            //color = byte2RGB( 255, 255, 255 );

            image.setRGB( j, i, color );
         }

      try
      {
         File imagesDir = new File("images");
         if (!imagesDir.exists()) {
            imagesDir.mkdir();
         }

         // Save the image as "zad1.zad1a.bmp" inside the 'images' folder
         ImageIO.write(image, "bmp", new File(imagesDir, "test.bmp"));
         System.out.println("Ring image created successfully in 'images' folder");
      }
      catch (IOException e)
      {
         System.out.println( "Gray image cannot be stored in BMP file" );
      };
      
      
      // Now make color image
      for ( int i = 0; i < height; i++)
         for ( int j = 0; j < width; j++)     
         {
            gray = (byte)(j % 256);
            color = byte2RGB( gray, (256 - gray), (i%256) );
            image.setRGB( j, i, color );
         }
      
      // Save image in graphics file
//      try
//      {
//          ImageIO.write( image, "jpg", new File( "d:\\test_color.jpg") );
//          System.out.println( "Color image created successfully" );
//      }
//      catch (IOException e)
//      {
//          System.out.println( "Color image cannot be stored in JPG file" );
//      };
      
      
      // Not used here but you should know how ...      
      // Acquire the list of formats currently supported by ImageIO
      String  formats[] = ImageIO.getReaderFormatNames();
      writeln( "You can use the following image file formats:" );
      for ( int i = 0; i < formats.length; i++ )
         writeln( formats[i] );
      
      // Not used here but you should know how ...
      // Lines below show how to query supported file types
      // and how to read a graphic file
      in = new Scanner(System.in);  
      BufferedImage  input_image = null;
      writeln( "Type the image location and name to read:" );    
      String image_name = readStr();
       
      try 
      {
          input_image = ImageIO.read( new File( image_name ) );
          System.out.println("Image successfully read");
      }    
      catch (IOException e) {
         System.out.println( "Cannot read this image" );
      }   
      
      /*
      // IN order to read R,G,B from a pixel          
      int  in_c = input_image.getRGB(i, j);
      Color cc_in = new Color( in_c );
      int rc = cc_in.getRed();
      int gc = cc_in.getGreen();          
      int bc = cc_in.getBlue();
      */      
   }

   private static Scanner in; 
   
   static int byte2RGB( int red, int green, int blue)
   {  
      // Color components must be in range 0 - 255
      red   = 0xff & red;
      green = 0xff & green;
      blue  = 0xff & blue;
      return (red << 16) + (green << 8) + blue;
   }
   
   
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
   
   static String readStr() {
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



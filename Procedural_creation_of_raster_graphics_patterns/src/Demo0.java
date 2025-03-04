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

import java.io.*; 
import java.util.Random;
import java.util.Scanner;
import java.awt.image.*;
import java.awt.Color;
import java.lang.Math;
import javax.imageio.*; 

public class Demo0 {

	/**
	 * @param args
	 */
	private static Scanner in; 
	
	public static void main(String[] args) 
	{
    	String  dirname = "c:\\GK";
    	
		// TODO Auto-generated method stub
      System.out.println("Demo0 just started" );
        
 
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
		      /* Pattern 0 - random patern
		      long seed = i*j+i;
		      rand.setSeed( seed );
		      color = rand.nextInt( 255);
		      color = new Color( color, color, color  ).getRGB();
		      */
		      
            // Pattern 1 - Basic sharp rings pattern    
		      /*
            double d = Math.sqrt((double)(i - width/2)*(i-width/2) + 
                                         (j-height/2)*(j-height/2));
            ri = ((int)d)/20;
            if ( (ri%2)==0)
            {
               color = byte2RGB( 0, 0, 0 );
               color = new Color( 0, 0, 0 ).getRGB();
            }
            else
               color = byte2RGB( 255, 255, 255 );
            */
		      
            
            /* Mixed ring/angle zones 
            
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
           */ 		      
		      
		      /* Patern 7 - colour waves
		      int  yp = (int)(i + 1.5*w1*Math.sin( 2*3.1425*3*(j/(double)width) ));
            yp = (yp + 2*w1) % w1;
		      
		      if (yp < w1/2 )
               color = new Color( 255, 0, 0 ).getRGB();
		      else
               color = new Color( 0, 255, 0 ).getRGB();
		      */
		      
		      
		      /* Pattern 5 - faded triangle
		      // transformed coordinates: xp yp
            int xp = j;
            int yp = i;
            if ( xp > width/2)
               xp = width - 1 - xp;
            xp = 2*xp;

            if ( xp + yp > width )
               color = new Color( 0, 0, 0 ).getRGB();
            else
            {               
                int     br;
                double  av,bv;
                av = width - 1 - yp;
                bv = xp;
                br = (int)(255*bv/av);              
                if ( br > 255 )
                   br = 255;
                br = 255 - br;
                color = new Color( br, br, br ).getRGB();
            }
		      */
		      
            // Basic sharp rings pattern
		      
            double d1 = Math.sqrt((double)(i - width/2)*(i-width/2) + 
                                         (j-height/2)*(j-height/2));
            d1 /= 1000;            
            d1 += 0.05;
            double d = 1.0 /d1;
            ri = ((int)d)/20;
            System.out.println( d1 );
            System.out.println( d );
            
            int intensity = (int)( ((0.5 + (Math.sin(d) + 1.0)) * 256.0) );
            if ( intensity < 0 )
               intensity = 0;
            if ( intensity > 255 )
               intensity = 255;
            color = new Color( intensity, intensity, intensity ).getRGB();
            /*
            if ( (ri%2)==0)
            {
               color = byte2RGB( 0, 0, 0 );
               color = new Color( 0, 0, 0 ).getRGB();
            }
            else
               color = byte2RGB( 255, 255, 255 );
            */
		      
            /* Mixed ring/angle zones
		      
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
            */
		      
		        
		       /* Pattern - faded frame
	          double ff, gg;
	          double margin = width/7;
	          
	          if ( i < margin)
	             ff = ((double)i) / margin;
	          else
	          if (i > (w - 1 - margin))
	          {
	             int k = w - 1 - i;
	             ff = k/margin;
	          }
	          else
	             ff = 1;

             if ( j < margin)
                gg = ((double)j) / margin;
             else
             if (j > (w - 1 - margin))
             {
                int k = w - 1 - j;
                gg = k/margin;
             }
             else
                gg = 1;
	          
             ff *= gg;
	          
	          int g = (int)(255 * ff);
	          if ( g > 255)
	             g = 255; 
	          color = new Color( g, g, g ).getRGB();
	          image.setRGB( j, i, color );
	          */
		      
		      
		      /* Patter - uniform faded out
		      double ff;
		      if ( i < width/2)
		         ff = ((double)i) / (width/2);
		      else
		         ff = ((double)( w - i ))/(w/2);
            int g = (int)(255 * ff);
            if ( g > 255)
               g = 255; 
            color = new Color( g, g, g ).getRGB();
            image.setRGB( j, i, color );
		      */
		      
		      
		      /* Pattern - fuzzy circle   
            double d = Math.sqrt((double)(i - width/2)*(i-width/2) + 
                                         (j-height/2)*(j-height/2));
            if ( d < ri )
               color = new Color( 0, 0, 0 ).getRGB();
            else
               if ( d > ro )
                  color = new Color( 255, 255, 255 ).getRGB();
               else
               {
                  double ff = (d - ri) / ( ro - ri );
                  int g = (int)(255 * ff);
                  if ( g > 255)
                     g = 255; 
                  color = new Color( g, g, g ).getRGB();
               }
              */   
		      
	                             
	           /* Pattren 3 - Concave caro
		        double d; 
		        d = Math.sqrt((double)(i - width)*(i-width) + 
                                         (j-height)*(j-height));
		        if ( d < w/2 )
                 color = new Color( 255, 0, 0 ).getRGB();
		        else
		        {
	              d = Math.sqrt((double)(i*i + j*j));
	              if ( d < w/2 )
	                 color = new Color( 255, 0, 0 ).getRGB();
	              else
	              {
	                 d = Math.sqrt((double)(i*i + (j-height)*(j-height)));
	                 if ( d < w/2 )
	                    color = new Color( 255, 0, 0 ).getRGB();
	                 else
	                 {
	                    d = Math.sqrt((double)(i - width)*(i-width) + j*j );

	                    if ( d < w/2 )
	                       color = new Color( 255, 0, 0 ).getRGB();
	                    else
                          color = new Color( 0, 0, 0 ).getRGB();                       	                    
	                 }
	              }
		        }
		        */   
		           
		      /*
		      // Pattern 4 - single vawed horizontal band
            double ip = i + a*Math.sin( f * j/(double)(width) * 2 * Math.PI ) + a;
            //ip = i;
            int  ipw; 
            if ( ip >= 0 )
            {  
               ipw = ((int)ip) % w;
               if ( ipw < w/2 )
                  color = new Color( 255, 0, 0 ).getRGB();
               else                    
                  color = new Color( 255, 255, 255 ).getRGB();
            }
            image.setRGB( j, i, color );
            */

	         /*
		      // Basic pattern
            double d = Math.sqrt((double)(i - width/2)*(i-width/2) + 
             		                       (j-height/2)*(j-height/2));
            ri = ((int)d)/20;
            if ( (ri%2)==0)
            {
               color = byte2RGB( 0, 0, 0 );
               color = new Color( 0, 0, 0 ).getRGB();
            }
            else
               color = byte2RGB( 255, 255, 255 );
            */
		      
            /* Pattern - fuzzy stars
            double angle = Math.atan2((double)(j-height/2), (double)(i-width/2)) + 3.1415;
            angle = 8*angle;
            double mod = (Math.sin(angle) + 1.0) / 2.0;

            double d_c = Math.sqrt((double)(i - width/2)*(i-width/2) + 
                  (j-height/2)*(j-height/2));
            d_c = d_c * (1.0 + mod/2);
            double d_n = 2*3.1415*(d_c/200.0);
            
            
            double intensity = 128*(Math.sin( d_n ) + 1);
            int int_i = (int)(intensity + 0.5);
            if ( int_i > 255 )
               int_i = 255;
            if ( int_i < 0 )
               int_i = 0;
            color = new Color( int_i, int_i, int_i ).getRGB();           
            */
            
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

		// Save image in graphics file
      try 
      {
         File  dir = new File ( dirname );
        	dir.mkdir();
         ImageIO.write( image, "bmp", new File("d:\\out_img_gray.bmp") );
         System.out.println( "Gray image created successfully"  );	
      } 
      catch (IOException e)
      {
         System.out.println( "Gray image cannot be stored in BMP file" );	
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
          ImageIO.write( image, "jpg", new File( "d:\\out_img_color.jpg") );
          System.out.println( "Color image created successfully" );	
      } 
      catch (IOException e)
      {
          System.out.println( "Color image cannot be stored in BMP file" );	
      };

      
      // Not used here but you should know how ...      
      // Acquire the list of formats currently supported by ImageIO
      String  formats[] = ImageIO.getReaderFormatNames();
      writeln( "You can use the following image file formats:" );
      for ( i = 0; i < formats.length; i++ )
         writeln( formats[i] );
      
      // Not used here but you should know how ...
      // Lines below show how to query supported file types
      // and how to read a graphic file
      in = new Scanner(System.in);  
      input_image = null;
      writeln( "Type the image location and name to read:" );    
      image_name = readStr();
       
      try 
      {
          input_image = ImageIO.read( new File( image_name ) );
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

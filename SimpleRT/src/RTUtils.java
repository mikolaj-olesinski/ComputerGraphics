import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Scanner;
import java.io.IOException;


public class RTUtils {
   
   public enum DRIVING_PLANES {EXCLUDE_X, EXCLUDE_Y, EXCLUDE_Z }; 
   
   public final static double EPS = 1.0E-10;   
   public final static double INFINITY = 1.0E+30;      
   public final static Color  DEF_BCKG_COLOR = new Color( 0, 0, 160 );
   public final static int    MAX_RAY_TREE_DEPTH = 20; 
   
   public final static int    DEF_BCOL_RED   = 0;
   public final static int    DEF_BCOL_GREEN = 0;
   public final static int    DEF_BCOL_BLUE  = 150;
   public final static double COS_25         = 0.906;
   
   public static void RemoveComments( String fname, String tmp_fname )
       throws IOException
   {
      BufferedReader inputStream1 = null;
      PrintWriter outputStream1 = null;      
       
      try 
      {
          inputStream1 = 
              new BufferedReader(new FileReader( fname ));
          outputStream1 = 
              new PrintWriter(new FileWriter( tmp_fname ));

          String  l;
          while ((l = inputStream1.readLine()) != null) 
          {
             int  ind = l.indexOf( "//" );
             if ( ind == 0 )
                continue;
             
             if ( ind > 0 )
                 l = l.substring(0, ind - 1);      
              outputStream1.println(l);             
          }
      } 
      finally 
      {
          if (inputStream1 != null) 
              inputStream1.close();
          
          if (outputStream1 != null) 
              outputStream1.close();
      }    
   }

   static int byte2RGB( int red, int green, int blue)
   {  
      // Color components must be in range 0 - 255
      if ( red > 255 )
         red = 255;
      if ( green > 255 )
         green = 255;
      if ( blue > 255 )
         blue = 255;
      red   = 0xff & red;
      green = 0xff & green;
      blue  = 0xff & blue;
      return (red << 16) + (green << 8) + blue;
   }     
   
}

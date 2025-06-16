import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SimpleRT {

   /**
    * @param args
    */
   public static void main(String[] args) 
   {
        World world;
        Scene scene;
        
        if ( args.length  < 2 )
        {
           System.out.println( "At least one parameter required (input scene file) " );
           return;
        }
        
        System.out.println( "Running in: " + System.getProperty("java.vm.name") + " " + 
                            System.getProperty("java.version"));
        
        world = new World();
        scene = new Scene();
        
        try
        {
           BufferedImage  image;       
           double         amb = 0.0;
           int            supersampling = 0;
           RGBColor       b_color ;
           
           if ( args.length >= 4 )
              b_color = new RGBColor( Integer.parseInt( args[1] ), 
                                      Integer.parseInt( args[2] ),
                                      Integer.parseInt( args[3] ));
           else
              b_color = new RGBColor( RTUtils.DEF_BCOL_RED, 
                                      RTUtils.DEF_BCOL_GREEN,
                                      RTUtils.DEF_BCOL_BLUE );
           if ( args.length >= 5 )
              amb = Double.parseDouble( args[4] );
           System.out.println( "Ambient light: " + amb );

           if ( args.length >= 6 )
              supersampling = Integer.parseInt( args[5] );
           
           // Load scene data
           scene.LoadFromFile( args[0], b_color, amb );
           world.GetData(scene);
           world.SetSupersampling( supersampling );
           
           // Render image
           image = world.RayTrace();
           
           // Write image to a file 
           String   image_fname;
           
           int idx = args[0].lastIndexOf('.');
           if ( idx > 0 )
              image_fname = args[0].substring(  0, idx ) + ".bmp";
           else
              image_fname = args[0] + ".bmp";
           
           System.out.println( "Saving image to the file: " + image_fname );
           try 
           {
               ImageIO.write( image, "bmp", new File( image_fname ) );
               System.out.println( "Rendered image created successfully" );  
           } 
           catch (IOException e)
           {
               throw new RTException( "Rendered image cannot be stored in BMP file" );   
           };           
        }
        catch ( IOException  ex )
        {
           System.out.println( "Cannot load scene data: " + args[0] );
        }
        catch ( RTException  ex )
        {
           System.out.println( ex.msg );
        }      
   }

}

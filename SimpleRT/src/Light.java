import java.util.Scanner;


public class Light 
{
   public enum LightType { POINT_LT, CONE_LT, SPHERE_LT };
   
   public String    name;
   public boolean   enabled;
   public LightType type;
   public RGBColorFloat rgb;  
   public double    E;
   public Point3D   position;
   public Vector3D  dir;
   public double    inner_angle;
   public double    outer_angle;
   public double    gonio[][];
   public double radius = 0.0;
   
   public void ReadFromFile( Scanner s )
   {     
      // Skip cam_name keyword
      s.next();
      name = s.next();
      
      // Enabled switch
      s.next();
      enabled = ( s.nextInt() == 1 );
      
      // Light type
      s.next();
      String stg = s.next();
      
      if ( stg.equals( "point" ))
         type = LightType.POINT_LT;
      else
         type = LightType.CONE_LT;
      
      s.next();
      rgb = new RGBColorFloat();
      rgb.ReadFromFile( s );
      
      s.next();
      E = s.nextDouble();
      
      s.next();
      position = new Point3D();
      position.ReadFromFile( s );
      
      s.next();
      dir = new Vector3D();
      dir.ReadFromFile( s );
      
      s.next();
      inner_angle = s.nextDouble();      
      s.next();
      outer_angle = s.nextDouble();      
      
      // Read gonio diagram
      s.next();
      int gonio_cnt = s.nextInt();
      
      gonio = new double[gonio_cnt][2];
      for ( int i = 0; i < gonio_cnt; i++ )
      {
         gonio[i][0] = s.nextDouble();
         gonio[i][1] = s.nextDouble();
      }

      if (s.hasNext("radius")) {
         s.next();
         radius = s.nextDouble();
         if (radius > 0) {
            type = LightType.SPHERE_LT;
         }
      }
   }
}

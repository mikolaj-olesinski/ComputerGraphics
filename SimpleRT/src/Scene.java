import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.Locale;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class Scene
{
   // Scene description
   public  Point3D            vertices[];
   public  ArrayList<Integer> adj_triangles[];   
   public  Shape3D            shapes[];
   public  Light              lights[];
   public  int                parts[];
   public  MatAttr            materials[];
   public  RTObserver         cams[];
   public  RTObserver         camera;
   public  double             ambient_light;
   public  RGBColorFloat      bckg_color_rgb = new RGBColorFloat();   
   
   public double LoadFromFile( String fname, RGBColor b_color, double amb_light )
         throws IOException, RTException
   {
      System.out.println( "Loading scene data from file: " + fname );
      
      long s_time = System.currentTimeMillis();    
      
      bckg_color_rgb.r = (float)(b_color.R/255.0);
      bckg_color_rgb.g = (float)(b_color.G/255.0);
      bckg_color_rgb.b = (float)(b_color.B/255.0);
      
      ambient_light = amb_light;      
       
      String tmp_fname = fname + ".tmp";
      
      RTUtils.RemoveComments( fname, tmp_fname );
      
      Scanner s = null;
      try {
          s = new Scanner(
                  new BufferedReader(new FileReader( tmp_fname )));
          s.useLocale(Locale.US);
          
          // Read vertices
          // skip keyword
          s.next();
          int vert_count = s.nextInt();
          vertices = new Point3D [ vert_count ];
          for ( int i = 0; i < vert_count; i++ )
          {
             vertices[i] = new Point3D();
             vertices[i].x = s.nextDouble();
             vertices[i].y = s.nextDouble();
             vertices[i].z = s.nextDouble();              
          }
          
          // Read triangles
          // skip keyword
          s.next();
          int trg_count = s.nextInt();
          
          // Create data for computing averaged normals
          adj_triangles = new ArrayList[ vert_count ];
          for ( int i = 0; i < vert_count; i++ )
             adj_triangles[i] = new ArrayList<Integer>();
          
          shapes = new Shape3D [ trg_count ];
          for ( int i = 0; i < trg_count; i++ )
          {
             Triangle trg = new Triangle();
             trg.ReadFromFile( s, vertices );
             adj_triangles[trg.i1].add(i);
             adj_triangles[trg.i2].add(i);
             adj_triangles[trg.i3].add(i);             
             shapes[i] = trg;           
          }
          
          // Create parts array
          // skip keyword
          s.next();
          int part_count = s.nextInt();
          parts = new int [ part_count ];
          for ( int i = 0; i < part_count; i++ )
             parts[i] = -1;
          
          // Read part indices of triangles          
          for ( int i = 0; i < trg_count; i++ )
          {
              shapes[i].part_index = s.nextInt();
          }
          
          // Read materials
          // skip keyword
          s.next();
          int mat_count = s.nextInt();
          materials = new MatAttr [ mat_count ];
          for ( int i = 0; i < mat_count; i++ )
          {
             MatAttr mat = new MatAttr();
             mat.ReadFromFile( s );
             materials[i] = mat;             
          }
       
          // Connect materials and parts
          for ( int i = 0; i < part_count; i++)
          {
             s.nextInt();
             String  mat_name = s.next();
             
             // Find material index
             int  j;
             for ( j = 0; j < mat_count; j++ )
                if ( materials[j].name.equals( mat_name ) )
                   break;
             if ( j >= mat_count )
                throw new RTException( "Cannot find material for a part" );
             
             parts[i] = j;      
          }
          
          // Update shapes with material references
          int shape_cnt = shapes.length;
          for( int i = 0; i < shape_cnt; i++ )
             shapes[i].material = materials[ parts[shapes[i].part_index] ];
          
          // Load lights
          s.next();
          int lights_count = s.nextInt();
          lights = new Light[lights_count];
          for ( int i = 0; i < lights_count; i++ )
          {
             Light l = new Light();
             l.ReadFromFile( s );
             lights[i] = l;
          }
          
          
          // Load camera data
          s.next();
          int cam_count = s.nextInt();
          cams = new RTObserver[cam_count];          
          s.next();
          int active_cam_index = s.nextInt();
          
          for ( int i = 0; i < cam_count; i++ )
          {
             RTObserver observer = new RTObserver();
             observer.ReadFromFile( s );
             cams[i] = observer;
             if ( i == active_cam_index )
                camera = observer;  
          }
          
          // Build averaged normal for triangles
          for ( int i = 0; i < trg_count; i++ )
             AddAvgNormals( i );
          
          System.out.println( "Scene data loaded." );          
      } 
      finally 
      {
          s.close();
      }            
      
      long e_time = System.currentTimeMillis();
      double  ltime = (e_time - s_time) * 0.001;
      
      return ltime;
   }
   
   
   /**
    * @param trg_index
    */
   public void AddAvgNormals( int trg_index )
   {
      Vector3D   normal, nnormal;
      
      // Do it only for triangles
      if ( ! ( shapes[trg_index] instanceof Triangle ) )
         return;
      
      Triangle trg = (Triangle)(shapes[trg_index]);
      nnormal = new Vector3D();
      
      // Get triangle geometric normal
      trg.GetNativeNormal( nnormal );
            
      // Find average normals for this triangle for each its vertex
      normal = FindAveragedNormal( nnormal, trg.i1 );
      trg.AddAvgNormal( normal, 1 );
      normal = FindAveragedNormal( nnormal, trg.i2 );
      trg.AddAvgNormal( normal, 2 );
      normal = FindAveragedNormal( nnormal, trg.i3 );
      trg.AddAvgNormal( normal, 3 );
      
      // Orient vertex normals uniformly
      trg.CorrectAvgNormals();     
     
      // Do other things related to vertex interpolation
      trg.PrepareNormalInterpolationData( vertices );      
   }


   public Vector3D FindAveragedNormal( Vector3D nnormal, int vrt_index )
   {
      double  x = 0;
      double  y = 0;
      double  z = 0;
      
      for ( int i = 0; i < adj_triangles[ vrt_index ].size(); i++)
      {
         Shape3D shp = shapes[ adj_triangles[vrt_index].get(i) ];
         if ( ! (shp instanceof Triangle)  )
            continue;
         Triangle trg = (Triangle)shp;
         
         Vector3D other_normal = new Vector3D(); 
         trg.GetNativeNormal( other_normal );
         
         // Test if this normal close enough to the native one
         double cos_a = nnormal.DotProduct( other_normal );
         if ( Math.abs( cos_a) < RTUtils.COS_25 )
            continue;

         if ( cos_a > 0.0 )
         {
            x += other_normal.x;
            y += other_normal.y;
            z += other_normal.z;
         }
         else
         {
            x -= other_normal.x;
            y -= other_normal.y;
            z -= other_normal.z;
         }
      }
      
      Vector3D avg_norm = new Vector3D();
      avg_norm.x = x;
      avg_norm.y = y;
      avg_norm.z = z;
      avg_norm.Normalize();
      
      return avg_norm;
   }
   

}

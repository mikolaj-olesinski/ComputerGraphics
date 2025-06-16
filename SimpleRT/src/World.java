import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


public class World 
{    
   // Scene description
   public  Point3D        vertices[];
   public  ArrayList<Integer> adj_triangles[];   
   public  Shape3D        shapes[];
   public  Light          lights[];
   public  int            parts[];
   public  MatAttr        materials[];
   public  RTObserver     cams[];
   public  RTObserver     camera;
   public  double         ambient_light;
   public  Color          bckg_color;
   public  RGBColorFloat  bckg_color_rgb = new RGBColorFloat();
   public  int            supersamples = 0;
   // Temporary data
   private Point3D        int_point      = new Point3D();
   private RGBColorFloat  attenuation    = new RGBColorFloat();
   private RGBColorFloat  ray_colors[];  
   
   private static int     MIN_SUPERSAMPLES = 10;
   private static double  COLOR_SMALL_FRACT = 0.2;
   
   // Statistics
   private long           prim_cnt       = 0;
   private long           refr_cnt       = 0;
   private long           refl_cnt       = 0; 
   private long           diffused_cnt   = 0;    
   private long           int_refl_cnt   = 0;
   private BVHNode        bvh            = null;
   public  boolean        use_BVH        = true;   
   public  boolean        use_stratified_supersampling = true;   
   
   
   public void GetData( Scene scene )
   {
      ray_colors = new RGBColorFloat[RTUtils.MAX_RAY_TREE_DEPTH];
      for ( int i = 0; i < RTUtils.MAX_RAY_TREE_DEPTH; i++ )
         ray_colors[i] = new RGBColorFloat( 0.0f, 0.0f, 0.0f );
      
      // Scene description
      vertices = scene.vertices;
      adj_triangles = scene.adj_triangles;   
      shapes = scene.shapes;
      lights = scene.lights;
      parts = scene.parts;
      materials = scene.materials;
      cams = scene.cams;
      camera = scene.camera;    
      ambient_light = scene.ambient_light;
      bckg_color_rgb = scene.bckg_color_rgb;
      bckg_color = RTUtils.DEF_BCKG_COLOR;
      
      // Get start time
      long s_time = System.currentTimeMillis();      
      bvh = BVHNode.CreateTreeSAH( shapes, 0, shapes.length - 1 );
      // bvh.DisplayTree(0);
      long e_time = System.currentTimeMillis();
      double  ltime = (e_time - s_time) * 0.001; 
      
      System.out.println( "BVH building time: " + ltime + " sec.");      
      System.out.println( "BVH statistics:");
      System.out.println( "   TRGS:   " + BVHNode.total_trgs);
      System.out.println( "   NODES:  " + BVHNode.total_nodes);
      System.out.println( "   LEAVES: " + BVHNode.leave_nodes);      
   }
   
   private boolean ContinueSupersampling( RGBColorFloat[] samples_color, int samples_cnt )
   {
      double  r_avg, r_max, r_min;
      double  g_avg, g_max, g_min;
      double  b_avg, b_max, b_min;
      
      r_avg = r_max = r_min = samples_color[0].r;
      g_avg = g_max = g_min = samples_color[0].g;
      b_avg = b_max = b_min = samples_color[0].b;
      
      for ( int j = 1; j < samples_cnt; j++ )
      {
         r_avg += samples_color[j].r;
         if ( samples_color[j].r > r_max  )
            r_max = samples_color[j].r;
         if ( samples_color[j].r < r_min  )
            r_min = samples_color[j].r;
            
         g_avg += samples_color[j].g;
         if ( samples_color[j].g > g_max  )
            g_max = samples_color[j].g;
         if ( samples_color[j].g < g_min  )
            g_min = samples_color[j].g;
         
         b_avg += samples_color[j].b;
         if ( samples_color[j].b > b_max  )
            b_max = samples_color[j].b;
         if ( samples_color[j].b < b_min  )
            b_min = samples_color[j].b;                   
      }
      
      r_avg /= samples_cnt;
      g_avg /= samples_cnt;
      b_avg /= samples_cnt;
      
      if ((r_max - r_min) > COLOR_SMALL_FRACT * r_avg) return true;
      if ((g_max - g_min) > COLOR_SMALL_FRACT * g_avg) return true;
      if ((b_max - b_min) > COLOR_SMALL_FRACT * b_avg) return true;
      
      return false;
   }

   public BufferedImage RayTrace()
   {
      BufferedImage  img;
      RTRay          primary_ray;
      int            x_res, y_res;
      int            bckg_color_int = bckg_color.getRGB();
      int            shape_color_int = new Color( 0,0,0 ).getRGB();
      int            supersamples_sqr = supersamples * supersamples;
      double         x_ray; 
      double         y_ray;
      double         recip_ss = 1.0/(supersamples+1);
      
      // Request the resolution of the active camera
      x_res = camera.x_res;
      y_res = camera.y_res;
      
      prim_cnt = refr_cnt = refl_cnt = int_refl_cnt = 0;
      
      System.out.println( "Ray tracing initialized ..." );
      
      // Get start time
      long s_time = System.currentTimeMillis();
      
      // Create the output image
      img = new BufferedImage( x_res, y_res, BufferedImage.TYPE_INT_RGB );
      primary_ray = new RTRay();
      RGBColorFloat  ray_color = new RGBColorFloat();  
     
      RGBColorFloat[] samples_color = new RGBColorFloat[ supersamples + 1];
      for( int i = 0; i <= supersamples; i++ )
         samples_color[i] = new RGBColorFloat();
      
      for ( int y = 0; y < y_res; y++ )
      {   
         for ( int x = 0; x < x_res; x++ )
         {
            int samples_cnt = 0;
            
            prim_cnt++;
            camera.SetRayParam(x, y, primary_ray);        
            primary_ray.Update();           
                        
            Shape3D int_shape = RayColor( primary_ray, 0, null );
            
            ray_color.r = samples_color[samples_cnt].r = ray_colors[0].r;
            ray_color.g = samples_color[samples_cnt].g = ray_colors[0].g;            
            ray_color.b = samples_color[samples_cnt].b = ray_colors[0].b;
            samples_cnt++;
            
            if ( supersamples > 0 )
            {   
               for( int y_d = 0; y_d < supersamples; y_d++ )
                  for( int x_d = 0; x_d < supersamples; x_d++ )
                  {
                     prim_cnt++;
                     if (use_stratified_supersampling)
                     {
                        x_ray = x + (Math.random() + x_d) * recip_ss; 
                        y_ray = y + (Math.random() + y_d) * recip_ss; 
                     }
                     else
                     {
                        x_ray = x + Math.random() - 0.5; 
                        y_ray = y + Math.random() - 0.5; 
                     }
                     camera.SetRayParam(x_ray, y_ray, primary_ray);       
                     primary_ray.Update();                
                     int_shape = RayColor( primary_ray, 0, null );
                     
                     ray_color.r += ray_colors[0].r;
                     ray_color.g += ray_colors[0].g;
                     ray_color.b += ray_colors[0].b;                     

                     samples_cnt++;                     
                  }
            }
               
            double gamma = 0.5;
            ray_colors[0].r = (float) Math.pow( ray_color.r/samples_cnt, gamma);
            ray_colors[0].g = (float) Math.pow( ray_color.g/samples_cnt, gamma);
            ray_colors[0].b = (float) Math.pow( ray_color.b/samples_cnt, gamma);
            ray_colors[0].r *= 255;
            ray_colors[0].g *= 255;
            ray_colors[0].b *= 255;
            int pixel_color = RTUtils.byte2RGB( (int)ray_colors[0].r, 
                                                (int)ray_colors[0].g, 
                                                (int)ray_colors[0].b );
            img.setRGB( x, y, pixel_color );
            
            
            /*
            int int_shape_ind = FindClosestIntersection( primary_ray );
            if ( int_shape_ind >= 0 )
               img.setRGB( x, y, shapes[int_shape_ind].native_color.as_int );
            else
               img.setRGB( x, y, bckg_color_int );
            */            
            
         }
         if ( y % 10 == 0 )
            System.out.print( "\rRow " + y + " completed");
      }

      System.out.println("");
      
      // Get start time
      long e_time = System.currentTimeMillis();
      double  rttime = (e_time - s_time) * 0.001; 
      
      System.out.println( "Rendering time: " + rttime + " sec.");
      System.out.println( "Triangles count: " + shapes.length );
      
      System.out.println( "Prmary rays: "    + prim_cnt );
      System.out.println( "Reflected rays: " + refl_cnt );
      System.out.println( "Refracted rays: " + refr_cnt );
      System.out.println( "Internal refl:  " + int_refl_cnt );
      System.out.println( "Diffused rays:  " + diffused_cnt );      
   
      System.out.println( "Intersection test count: " + Triangle.total_ints * 1.0e-6 + " mlns");      
      System.out.println( "M intersections/sec:  " +  (Triangle.total_ints * 1.0e-6) / rttime );
      System.out.println( "BV test count:        " + BoundingVolume.total_ints * 1.0e-6 + " mlns");     
      System.out.println( "M BV tests/sec:       " +  (BoundingVolume.total_ints * 1.0e-6) / rttime );
      System.out.println( "Avg samples per pixel " +  (prim_cnt/(x_res*y_res)) );
      return img;
   }
   
   private Shape3D FindClosestIntersection( RTRay ray, Shape3D excluded )
   {
      if ( use_BVH )
         return bvh.FindClosestIntersection(ray, excluded, int_point);
      else
         return FindClosestIntersectionBasic(ray, excluded );
   }
   
   private Shape3D FindClosestIntersectionBasic( RTRay ray, Shape3D excluded )
   {
      int intersected_shp_ind = -1;
      int shp_count = shapes.length;
      
      for ( int i = 0; i < shp_count; i++ )
      {
         // Do not test excluded face
         if ( shapes[i] == excluded )
            continue;
         if ( shapes[i].IsIntersected(ray) )
         {
            int_point.x = Triangle.int_point.x;
            int_point.y = Triangle.int_point.y;            
            int_point.z = Triangle.int_point.z;
            
            intersected_shp_ind = i;
         }
      }
      
      if (intersected_shp_ind == -1)
         return null;
      else
         return shapes[intersected_shp_ind];    
   }
  
   private boolean FindLightAttenuation( RTRay ray, RGBColorFloat attenuation, Shape3D excluded )
   {
      if ( use_BVH )
         return bvh.FindLightAttenuation(ray, attenuation, excluded);
      else
         return FindLightAttenuationBasic( ray, attenuation, excluded );
   }
   
   private boolean FindLightAttenuationBasic( RTRay ray, RGBColorFloat attenuation, Shape3D excluded )
   {
      int shp_count = shapes.length;
      attenuation.r = attenuation.g = attenuation.b = (float)1.0;
      ray.t_min = RTUtils.EPS;
      
      for ( int i = 0; i < shp_count; i++ )
      {
         // Do not test excluded face
         if ( shapes[i] == excluded )
            continue;

         if ( shapes[i].IsIntersected(ray) )
         {
            int mat_index = parts[shapes[i].part_index];
            attenuation.r *= materials[mat_index].ktCr;
            attenuation.g *= materials[mat_index].ktCg;
            attenuation.b *= materials[mat_index].ktCb;
            if ( ( attenuation.r < RTUtils.EPS ) && ( attenuation.g < RTUtils.EPS ) &&
                 ( attenuation.b < RTUtils.EPS ) )
               return false;
            ray.t_int = RTUtils.INFINITY;
         }
      }

      return true;    
   } 
  
   private Shape3D RayColor( RTRay ray, int level, Shape3D origin_face )
   {
      double rf, gf, bf;
      double dist_to_light;
      Shape3D    int_object = FindClosestIntersection( ray, origin_face );    
      
      if ( int_object == null )
      {
         bckg_color_rgb.CopyTo( ray_colors[level] );
         return null;
      }
      
      Vector3D   n = new Vector3D();
      RTRay      shadow_ray = new RTRay();
      Vector3D   glossy_dir = new Vector3D();
      
      Point3D    int_point = new Point3D();
      
      int_point.x = this.int_point.x;
      int_point.y = this.int_point.y;
      int_point.z = this.int_point.z;
      
      rf = gf = bf = 0.0;
      
      int mat_index = parts[int_object.part_index];
      
      // Get normal oriented towards observer - u is from the observer to the 
      // observed fragments      
      int_object.GetNormal( int_point, n );      
      if ( n.DotProduct( ray.u ) > 0 )
         n.Reverse();         

      Point3D shadow_ray_origin;
      
      shadow_ray_origin = int_point;
      if ( int_object instanceof Triangle )
      {
         Triangle  trg = (Triangle)int_object;
         if ( trg.use_interpolated_normals )
            shadow_ray_origin = trg.DisplaceIntPoint( int_point, ray.P, vertices );
      }
      
      // Object hit - compute diffused light
      for ( int l = 0; l < lights.length; l++ )
      {
         // Test shadowing
         shadow_ray.P.x = shadow_ray_origin.x;
         shadow_ray.P.y = shadow_ray_origin.y;
         shadow_ray.P.z = shadow_ray_origin.z;       
         
         //int_point.CopyTo( shadow_ray.P );
         shadow_ray.u.FromPoints( lights[l].position, int_point );
         
         // if the light on the opposite side than observer - skip the light, 
         // the only exception is the transparent-difuse surface which can be illuminated from
         // both sides
         if ( ( n.DotProduct( shadow_ray.u ) <= 0.0 ) &&  !materials[mat_index].is_transparent )
            continue;         
         
         shadow_ray.t_max = dist_to_light = shadow_ray.u.GetLength();
         shadow_ray.t_min = RTUtils.EPS;
         shadow_ray.t_int = RTUtils.INFINITY;
         shadow_ray.u.Normalize();         
         shadow_ray.Update();
         
         attenuation.r = attenuation.g = attenuation.b = 1.0f;
         if ( FindLightAttenuation( shadow_ray, attenuation, int_object ) )      
         {
            double dist_clipped = Math.max( 0.3, dist_to_light );
            
            // Light arrives to the observed point
            double dist_att = lights[l].E * Math.abs( shadow_ray.u.DotProduct( n ))  / dist_clipped;
            rf += dist_att * attenuation.r * lights[l].rgb.r * materials[mat_index].kdIr;
            gf += dist_att * attenuation.g * lights[l].rgb.g * materials[mat_index].kdIg;
            bf += dist_att * attenuation.b * lights[l].rgb.b * materials[mat_index].kdIb;  
            
            if ( materials[mat_index].is_specular_refl )
            {
               ray.ReflectedDir( n, glossy_dir );        
               double  g_factor = shadow_ray.u.DotProduct( glossy_dir );
               if ( g_factor < 0 )
                  g_factor = 0.0;
               
               dist_att = lights[l].E * Math.pow( g_factor, materials[mat_index].g ) / dist_clipped;
               
               rf += dist_att * attenuation.r * lights[l].rgb.r * materials[mat_index].ksIr;
               gf += dist_att * attenuation.g * lights[l].rgb.g * materials[mat_index].ksIg;
               bf += dist_att * attenuation.b * lights[l].rgb.b * materials[mat_index].ksIb;                        
            }
         }
      }
        
      
      // Sample diffused light
      
      if (( materials[mat_index].is_diffused ) && ( level < RTUtils.MAX_RAY_TREE_DEPTH - 1 ) 
           // This is to avoid diffused rays recursion
           && (!ray.is_diffused) )
      {
         RTRay  diffused = new RTRay();
         
         // Create reflected ray
         int_point.CopyTo( diffused.P );        
         RTRay.LambertianDir( n, diffused.u );
         diffused.is_inside = ray.is_inside;     
         diffused.is_diffused = true;
         diffused.Update();
         diffused_cnt++;
         
         RayColor( diffused, level + 1, int_object );
         
         rf += (float)(ray_colors[level+1].r * materials[mat_index].kdIr);  
         gf += (float)(ray_colors[level+1].g * materials[mat_index].kdIg);
         bf += (float)(ray_colors[level+1].b * materials[mat_index].kdIb);         
      }
            
      // Add ambient light
      rf += ambient_light * materials[mat_index].kaIr;
      gf += ambient_light * materials[mat_index].kaIg;
      bf += ambient_light * materials[mat_index].kaIb;
      
      
      if (( materials[mat_index].is_specular_refl ) && 
          ( level < RTUtils.MAX_RAY_TREE_DEPTH - 1 ) && 
          !ray.is_diffused && !ray.is_inside )
      {
         RTRay  reflected = new RTRay();
         
         // Create reflected ray
         int_point.CopyTo( reflected.P );        
         ray.ReflectedDir( n, reflected.u );
         reflected.is_inside = ray.is_inside;         
         reflected.Update();
         
         RayColor( reflected, level + 1, int_object );
         refl_cnt++;
         
         // Use Schlick approximation of Fresnel law
         double R;
         double m = Math.pow(1.0- Math.abs( ray.u.DotProduct( n ) ), 5.0);
         
         R = materials[mat_index].ksIr + (1.0 -  materials[mat_index].ksIr)*m;
         if ( R > 4 * materials[mat_index].ksIr )
            R = 4 * materials[mat_index].ksIr;
         rf += (float)(ray_colors[level+1].r * R);
         
         R = materials[mat_index].ksIg + (1.0 -  materials[mat_index].ksIg)*m;
         if ( R >  4 * materials[mat_index].ksIg )
            R = 4 * materials[mat_index].ksIg;
         gf += (float)(ray_colors[level+1].g * R);
         
         R = materials[mat_index].ksIb + (1.0 -  materials[mat_index].ksIb)*m;
         if ( R > 4 * materials[mat_index].ksIb )
            R = 4 * materials[mat_index].ksIb;
         bf += (float)(ray_colors[level+1].b * R);            
         
         /*
         rf += (float)(ray_colors[level+1].r * materials[mat_index].ksIr);  
         gf += (float)(ray_colors[level+1].g * materials[mat_index].ksIg);
         bf += (float)(ray_colors[level+1].b * materials[mat_index].ksIb);
         */
                 
      }
            
      if (( materials[mat_index].is_transparent ) && 
          ( level < RTUtils.MAX_RAY_TREE_DEPTH - 1 ) && !ray.is_diffused )
      {
         double   eta;
         
         RTRay  refracted = new RTRay();
         
         // Create refracted ray
         int_point.CopyTo( refracted.P ); 
         if ( ray.is_inside )
            eta = materials[mat_index].eta;
         else
            eta = 1.0 / materials[mat_index].eta;
         
         if ( ray.RefractedDir( n, refracted.u, eta ) )
         {
            // Proceed only in case there is no total internal reflection
            refracted.is_inside = ! ray.is_inside;
            refracted.t_min = RTUtils.EPS;
            refr_cnt++;
            refracted.Update();
            RayColor( refracted, level + 1, int_object );
            
            // Use transmitance attenuation
            rf += (float)(ray_colors[level+1].r * materials[mat_index].ktIr);  
            gf += (float)(ray_colors[level+1].g * materials[mat_index].ktIg);
            bf += (float)(ray_colors[level+1].b * materials[mat_index].ktIb);               
         }
         else
         {
            // Total internal reflection
            int_point.CopyTo( refracted.P );        
            ray.ReflectedDir( n, refracted.u );
            refracted.is_inside = ray.is_inside;    
            refracted.t_min = RTUtils.EPS;            
            int_refl_cnt++;
            refracted.Update();
            RayColor( refracted, level + 1, int_object );     
          
            // Compute the loss of energy in the media boundary using Frenel's principle
            double absorbtion = (eta - 1.0) / ( eta + 1.0 );
            absorbtion *= absorbtion;
            double  transmitance = 1.0 - absorbtion;
            
            // Use Schlick approximation of Fresnel law
            double R;
            double m = Math.pow(1.0- Math.abs( ray.u.DotProduct( n ) ), 5.0);
            
            R = transmitance + (1.0 -  transmitance)*m;
            if ( R > 1.0 )
               R = 1.0;
           
            // R = materials[mat_index].ksIr + (1.0 -  materials[mat_index].ksIr)*m;
            rf += (float)(ray_colors[level+1].r * R);
            
            // R = materials[mat_index].ksIg + (1.0 -  materials[mat_index].ksIg)*m;
            gf += (float)(ray_colors[level+1].g * R);
            
            // R = materials[mat_index].ksIb + (1.0 -  materials[mat_index].ksIb)*m;
            bf += (float)(ray_colors[level+1].b * R);               
         }     
      }   
      
      // Pass finally computed color
      ray_colors[level].r = (float)rf;  //1.0f;
      ray_colors[level].g = (float)gf;  //0.0f;
      ray_colors[level].b = (float)bf;  //0.0f;
      
      return int_object;
   } 
   
   
   public void SetSupersampling( int supersampling )
   {
      supersamples = supersampling;
   }
}

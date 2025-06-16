
public class BoundingVolume
{
   public static double EPSILON = 1.0e-6;
   
   public static long total_ints = 0;
   
   public  Interval   intv_x;
   public  Interval   intv_y;
   public  Interval   intv_z;  
   private Interval[] intv;
   
   public BoundingVolume()
   {
      intv_x = new Interval( 0.0, 0.0 );
      intv_y = new Interval( 0.0, 0.0 );
      intv_z = new Interval( 0.0, 0.0 );
      intv = new Interval[3];
      intv[0] = intv_x;
      intv[1] = intv_y;
      intv[2] = intv_z;
   }
 
   public double GetSize( int axis )
   {
      switch (axis )  {
         case 0 : return (intv_x.v_max - intv_x.v_min);
         case 1 : return (intv_y.v_max - intv_y.v_min);
         case 2 : return (intv_z.v_max - intv_z.v_min);
      }
      return 0.0;      
   }
   
   public double GetMin( int axis )
   {
      switch (axis )  {
         case 0 : return intv_x.v_min;
         case 1 : return intv_y.v_min;
         case 2 : return intv_z.v_min;
      }
      return 0.0;      
   }   

   public double GetMax( int axis )
   {
      switch (axis )  {
         case 0 : return intv_x.v_max;
         case 1 : return intv_y.v_max;
         case 2 : return intv_z.v_max;
      }
      return 0.0;      
   }   

   public BoundingVolume( BoundingVolume other )
   {
      intv_x = new Interval( other.intv_x );
      intv_y = new Interval( other.intv_y );
      intv_z = new Interval( other.intv_z );      
      intv = new Interval[3];
      intv[0] = intv_x;
      intv[1] = intv_y;
      intv[2] = intv_z;
   }   
   
   public static void MakeFromTriangles( )
   {
       BoundingVolume bv = new BoundingVolume();
       
   }
   public BoundingVolume( double xbbb_min, double xbbb_max, 
         double ybbb_min, double ybbb_max,
         double zbbb_min, double zbbb_max )   
   {
      intv_x = new Interval( xbbb_min, xbbb_max );
      intv_y = new Interval( ybbb_min, ybbb_max );
      intv_z = new Interval( zbbb_min, zbbb_max );
      intv = new Interval[3];
      intv[0] = intv_x;
      intv[1] = intv_y;
      intv[2] = intv_z;
   }
   
   public void PrapareForMerge()
   {
      intv_x.v_min =  RTUtils.INFINITY;
      intv_x.v_max = -RTUtils.INFINITY;
      
      intv_y.v_min =  RTUtils.INFINITY;
      intv_y.v_max = -RTUtils.INFINITY;
      
      intv_z.v_min =  RTUtils.INFINITY;
      intv_z.v_max = -RTUtils.INFINITY;
   }
   
   public void MergeInplace( BoundingVolume bv )
   {
      intv_x.MergeInplace( bv.intv_x );
      intv_y.MergeInplace( bv.intv_y );
      intv_z.MergeInplace( bv.intv_z );
   }
   
   public void AddMargin()
   {
      intv_x.v_min -= EPSILON;
      intv_x.v_max += EPSILON;
      intv_y.v_min -= EPSILON;
      intv_y.v_max += EPSILON;
      intv_z.v_min -= EPSILON;
      intv_z.v_max += EPSILON;      
   }
   
   public double SVH()
   {
      double x_size = intv_x.v_max - intv_x.v_min;
      double y_size = intv_y.v_max - intv_y.v_min;
      double z_size = intv_z.v_max - intv_z.v_min;
      
      return x_size*y_size + x_size*z_size + y_size*z_size; 
   }
   
   public boolean IsIntersected( RTRay ray)
   {  
      total_ints++;
      
         double ray_t_v_min = -RTUtils.INFINITY;
         double ray_t_v_max =  RTUtils.INFINITY;
         
         for (int axis = 0; axis < 3; axis++) {
             Interval ax = intv[axis];
             double adinv = ray.ad_inv[axis];
             double ray_p = ray.P_array[axis];

             double t0 = (ax.v_min - ray_p) * adinv;
             double t1 = (ax.v_max - ray_p) * adinv;

             if (t0 < t1) {
                 if (t0 > ray_t_v_min) ray_t_v_min = t0;
                 if (t1 < ray_t_v_max) ray_t_v_max = t1;
             }
             else {
                 if (t1 > ray_t_v_min) ray_t_v_min = t1;
                 if (t0 < ray_t_v_max) ray_t_v_max = t0;
             }

             if (ray_t_v_max <= ray_t_v_min)
                 return false;
         }       
 
      return ( !( 
                   (ray_t_v_max <= ray.t_min) || 
                   (ray_t_v_min >  ray.t_max) ||
                   (ray_t_v_min >  ray.t_int) 
                )
             );
   }
   
   public boolean IsIntersectedUnrolled( RTRay ray, Interval ray_t )
   {  
      ray_t.v_min = -RTUtils.INFINITY;
      ray_t.v_max =  RTUtils.INFINITY;        

      Interval ax = intv[0];
      double adinv = ray.ad_inv[0];
      double ray_p = ray.P_array[0];

      double t0 = (ax.v_min - ray_p) * adinv;
      double t1 = (ax.v_max - ray_p) * adinv;

      if (t0 < t1) {
          if (t0 > ray_t.v_min) ray_t.v_min = t0;
          if (t1 < ray_t.v_max) ray_t.v_max = t1;
      }
      else {
          if (t1 > ray_t.v_min) ray_t.v_min = t1;
          if (t0 < ray_t.v_max) ray_t.v_max = t0;
      }

      if (ray_t.v_max <= ray_t.v_min)
          return false;

      ax = intv[1];
      adinv = ray.ad_inv[1];
      ray_p = ray.P_array[1];

      t0 = (ax.v_min - ray_p) * adinv;
      t1 = (ax.v_max - ray_p) * adinv;

      if (t0 < t1) {
          if (t0 > ray_t.v_min) ray_t.v_min = t0;
          if (t1 < ray_t.v_max) ray_t.v_max = t1;
      }
      else {
          if (t1 > ray_t.v_min) ray_t.v_min = t1;
          if (t0 < ray_t.v_max) ray_t.v_max = t0;
      }

      if (ray_t.v_max <= ray_t.v_min)
          return false;
      
      ax = intv[2];
      adinv = ray.ad_inv[2];
      ray_p = ray.P_array[2];

      t0 = (ax.v_min - ray_p) * adinv;
      t1 = (ax.v_max - ray_p) * adinv;

      if (t0 < t1) {
          if (t0 > ray_t.v_min) ray_t.v_min = t0;
          if (t1 < ray_t.v_max) ray_t.v_max = t1;
      }
      else {
          if (t1 > ray_t.v_min) ray_t.v_min = t1;
          if (t0 < ray_t.v_max) ray_t.v_max = t0;
      }

      if (ray_t.v_max <= ray_t.v_min)
          return false;
         
      return ( !( 
                   (ray_t.v_max <= ray.t_min) || 
                   (ray_t.v_min >  ray.t_max) ||
                   (ray_t.v_min >  ray.t_int) 
                )
             );
   }   
}

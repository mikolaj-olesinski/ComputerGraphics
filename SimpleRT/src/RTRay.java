
public class RTRay {
   Point3D  P;   // ray begin
   Vector3D u;   // ray direction
   double   t_min;
   double   t_max;
   
   double   t_int;
   boolean  is_inside = false;
   boolean  is_diffused = false;
   Point3D  int_point;
   double[] ad_inv;
   double[] P_array;
   
   RTRay()
   {
      P           = new Point3D( 0.0, 0.0, 0.0 );
      int_point   = new Point3D( 0.0, 0.0, 0.0 );
      u           = new Vector3D( 0.0, 0.0, 1.0 );
      is_inside   = false;
      is_diffused = false;
      ad_inv      = new double[3];
      P_array     = new double[3];
      
      InitDistances();
   }
   
   public void Update()
   {
      ad_inv[0] = 1.0 / ( (u.x == 0) ? RTUtils.EPS : u.x );
      ad_inv[1] = 1.0 / ( (u.y == 0) ? RTUtils.EPS : u.y );
      ad_inv[2] = 1.0 / ( (u.z == 0) ? RTUtils.EPS : u.z );
      P_array[0] = P.x;
      P_array[1] = P.y;
      P_array[2] = P.z;
   }
   
   public void RayInit( Point3D P, Vector3D u )
   {
      this.P.x = P.x;
      this.P.y = P.y;
      this.P.z = P.z;

      this.u.x = u.x;
      this.u.y = u.y;
      this.u.z = u.z;      
      Update();
      
      InitDistances();     
   }
   
   public void InitDistances()
   {
      // t_min = RTUtils.INFINITY;
      t_min = RTUtils.EPS;
      t_max = RTUtils.INFINITY;
      t_int = 2* RTUtils.INFINITY;     
   }
   
   public void ReflectedDir( Vector3D N, Vector3D refl )
   {
      double vl = u.DotProduct(N);
      refl.x = u.x - 2*vl*N.x;
      refl.y = u.y - 2*vl*N.y;
      refl.z = u.z - 2*vl*N.z;
      
      refl.Normalize();
   }
   
   public static void LambertianDir( Vector3D N, Vector3D diffused )
   {     
      while (true) {
         diffused.Random(-1,1);
         double lensq = diffused.LengthSqr();
         if (1e-100 < lensq && lensq <= 1)
             break;
      }
      diffused.Normalize();
      if (diffused.DotProduct(N) < 0)
         diffused.Reverse();
   }

   public boolean RefractedDir( Vector3D N, Vector3D refr, double eta )
   {
      // Formulas according to P.Shirley: "Realistic Ray Tracing", pp. 176-177
      double a, b;
      
      double  cos_teta_i = -u.DotProduct(N);
      double  sin_teta_t_2 = eta*eta*(1.0-cos_teta_i*cos_teta_i);
      double  sqrt = 1.0 - sin_teta_t_2;
      if ( sqrt <= 0 )
         return false;
      sqrt = Math.sqrt( sqrt );
      
      a = eta;
      b = eta*cos_teta_i - sqrt;

      refr.x = a*u.x + b*N.x;
      refr.y = a*u.y + b*N.y;
      refr.z = a*u.z + b*N.z;
      refr.Normalize();
      
      /*
      double NI = u.DotProduct(N);
      double cos_th = 1.0 + eta*eta*(NI*NI - 1);
      if ( cos_th <= 0 )
         return false;
      
      refr.x = eta*u.x + (-eta*NI-Math.sqrt(cos_th))*N.x;
      refr.y = eta*u.y + (-eta*NI-Math.sqrt(cos_th))*N.y;
      refr.z = eta*u.z + (-eta*NI-Math.sqrt(cos_th))*N.z;
      refr.Normalize();
      */
      
      return true;

   }   
}

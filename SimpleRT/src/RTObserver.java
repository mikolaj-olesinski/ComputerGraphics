import java.util.Scanner;

public class RTObserver 
{
     String  name;
     
     Point3D  O;    // Eye position in scene coordinates
     Point3D  T;    // Target point
     Vector3D u;    // Looking directions    
     
     double   fov;           // Field of view angle in degrees
     double   alpha;         // Rotation angle with recpect to vertical Y axis     
     double   w_h;           // Aspect ratio: w/h
     
     int      x_res, y_res;  // image resolution
     double   x_invert;      // 1 / x_res
     double   y_invert;      // 1 / y_res
     
     public   Point3D  P_UL; // upper left screen parallelogram vertex
     public   Point3D  P_UR; // upper right screen parallelogram vertex
     public   Point3D  P_LL; // lower left screen parallelogram vertex   
  
     // Screen window edges in 3D scene coords
     //
     //                    h_edge
     //      P_UL -------------------------->  P_UR
     //       |
     //       |   v_edge
     //      \|/
     //      P_LL
     //
     Vector3D  h_edge;
     Vector3D  v_edge;
     
     public void ReadFromFile( Scanner s )
     {        
        // Skip cam_name keyword
        s.next();
        name = s.next();
        
        // Get resolution
        s.next();        
        x_res = s.nextInt();
        y_res = s.nextInt();
        
        // Get eye position
        s.next();
        O = new Point3D();
        O.ReadFromFile( s );
        s.next();
        T = new Point3D();
        T.ReadFromFile( s );
        
        // Get field of view
        s.next();
        fov = s.nextDouble();
        
        s.next();
        alpha = s.nextDouble();
        
        RasterData();
     }
     
     private void RasterData()
     {        
        // Vertical and horizontal half vectors
        Vector3D  vert_v;    
        Vector3D  hor_v;
        
        // Find aspect ratio: w/h
        w_h = x_res / (double)y_res;
        x_invert = 1.0 / x_res;
        y_invert = 1.0 / y_res;
        
        // Find view direction
        Vector3D u = new Vector3D( T, O );  // u = T - O;; 0 ----> T
        double len = u.GetLength();
        
        // Define normalized verticaL vector
        Vector3D vn = new Vector3D( 0.0, 1.0, 0.0 );  
        
        // h - normalized horizontal vector
        hor_v = u.CrossProduct( vn );
        hor_v.Normalize();
        
        // Find the vertical vector perpendicular to u nad h
        vert_v = u.CrossProduct( hor_v );
        vert_v.Normalize();
        
        // Inverse vertical normalized if necessary
        if ( vert_v .DotProduct( vn ) < 0 )
           vert_v.ScalarMult( -1.0 );
        
        // Find half vectors of window edges
        double hl = len * Math.tan( Math.PI * fov / 360.0 );
        //  w_h = hl/vl
        double vl = hl / w_h;
        
        P_UL = new Point3D( T );
        hor_v.ScalarMult( hl );
        vert_v.ScalarMult( vl );
        
        P_UL.Sub( hor_v );
        P_UL.Add( vert_v );
        
        P_LL = new Point3D( T );
        P_LL.Sub( hor_v );
        P_LL.Sub( vert_v );
        
        P_UR = new Point3D( T );
        P_UR.Add( hor_v );
        P_UR.Add( vert_v );    
        
        h_edge = new Vector3D( P_UR, P_UL );
        v_edge = new Vector3D( P_LL, P_UL );
        
        P_UL.Show( "P_UL: " );
        P_LL.Show( "P_LL: " );
        P_UR.Show( "P_UR: " );
     }

     public void SetRayParam( double x, double y, RTRay ray )
     {
        ray.P.x = O.x;
        ray.P.y = O.y;
        ray.P.z = O.z;
        
        double x_fract = x * x_invert;
        double y_fract = y * y_invert;
        
        double x_pix = P_UL.x + x_fract * h_edge.x + y_fract * v_edge.x;
        double y_pix = P_UL.y + x_fract * h_edge.y + y_fract * v_edge.y;
        double z_pix = P_UL.z + x_fract * h_edge.z + y_fract * v_edge.z;

        ray.u.x = x_pix - O.x;
        ray.u.y = y_pix - O.y;
        ray.u.z = z_pix - O.z;
        
        ray.u.Normalize();
        
        ray.InitDistances();
     }
}

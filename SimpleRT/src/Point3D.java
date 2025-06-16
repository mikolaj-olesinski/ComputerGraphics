import java.util.Scanner;


public class Point3D {
    public double x,y,z;
    
    Point3D()
    {
       x = y = z = 0.0;
    }
    
    Point3D( final Point3D p )
    {
       x = p.x;
       y = p.y;
       z = p.z;
    }
    
    public void CopyTo( Point3D p )
    {
       p.x = x;
       p.y = y;
       p.z = z;
    }
    
    Point3D( double x, double y, double z )
    {
       this.x = x;
       this.y = y;
       this.z = z;
    }    
    
    public void ReadFromFile( Scanner s )
    {
        x = s.nextDouble();
        y = s.nextDouble();
        z = s.nextDouble();
    }
    
    public void ConvertTo2D( RTUtils.DRIVING_PLANES driving_plane, Point2D p )
    {
      
       if ( driving_plane == RTUtils.DRIVING_PLANES.EXCLUDE_X )
       {
          p.x = y;
          p.y = z;
       }
       else
          if ( driving_plane == RTUtils.DRIVING_PLANES.EXCLUDE_Y )
          {
             p.x = x;
             p.y = z;
          }
          else
          {
             p.x = x;
             p.y = y;
          }            
    }
    
    public Point2D ConvertTo2D( RTUtils.DRIVING_PLANES driving_plane )
    {
       Point2D  p = new Point2D( 0.0, 0.0 );
       
       ConvertTo2D( driving_plane, p );
       
       return p;      
    }
    
    public void Add( Vector3D v)
    {
       x += v.x;
       y += v.y;
       z += v.z;
    }
    
    public void Sub( Vector3D v)
    {
       x -= v.x;
       y -= v.y;
       z -= v.z;
    }

    public static void Sub( Point3D res, Point3D p1, Point3D p2)
    {       
       res.x = p1.x - p2.x;
       res.y = p1.y - p2.y;
       res.z = p1.z - p2.z;
    }
    
    public void Show( String  s )
    {
       System.out.println( s + " x=" + x + " y=" + y + " z=" + z);
    }
    
    public double DotProduct( Vector3D v )
    {
       return x*v.x + y*v.y + z*v.z;
    }
    
    public void CrossProduct( Vector3D out, Vector3D v )
    {
       out.x = y*v.z - z*v.y;
       out.y = z*v.x - x*v.z;
       out.z = x*v.y - y*v.x;
    }

}

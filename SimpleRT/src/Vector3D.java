import java.util.Scanner;
import java.util.Random;

public class Vector3D {
    public  double  x, y, z;
    
    private static Random rand = new Random();
    
    public Vector3D()
    {
       x = 1.0;
       y = z = 0.0;
    }
    public Vector3D( double x, double y, double z )
    {
       this.x = x;
       this.y = y;
       this.z = z;
    }
    
    public Vector3D( Point3D v2, Point3D v1 )
    {
       this.x = v2.x - v1.x;
       this.y = v2.y - v1.y;
       this.z = v2.z - v1.z;
    }
    
    public static Vector3D Sub( Vector3D v2, Vector3D v1 )
    {
       Vector3D   result = new Vector3D();
       result.x = v2.x - v1.x;
       result.y = v2.y - v1.y;
       result.z = v2.z - v1.z;
       
       return result;
    }    
    
    public static Vector3D Sub( Point3D v2, Point3D v1 )
    {
       Vector3D   result = new Vector3D();
       result.x = v2.x - v1.x;
       result.y = v2.y - v1.y;
       result.z = v2.z - v1.z;
       
       return result;
    }    
        
    public void Normalize()
    {
       double len;
       len = Math.sqrt( x*x + y*y + z*z );
       if ( len < RTUtils.EPS )
          throw new RTException( "Cannot normalize a vector " );
       x /= len;
       y /= len;
       z /= len;
    }
    
    public double DotProduct( Vector3D v )
    {
       return x*v.x + y*v.y + z*v.z;
    }
    
    public double DotProduct( Point3D v )
    {
       return x*v.x + y*v.y + z*v.z;
    }
    
    public Vector3D CrossProduct( Vector3D v )
    {
       Vector3D out = new Vector3D();
       out.x = y*v.z - z*v.y;
       out.y = z*v.x - x*v.z;
       out.z = x*v.y - y*v.x;
       return out;       
    }
    
    public Vector3D CrossProduct( Vector3D out, Vector3D v )
    {
       out.x = y*v.z - z*v.y;
       out.y = z*v.x - x*v.z;
       out.z = x*v.y - y*v.x;
       return out;       
    }
    
    public double GetLength()
    {
       return Math.sqrt( x*x + y*y + z*z );
    }
    
    public void ScalarMult( double t )
    {
       x *= t;
       y *= t;
       z *= t;
    }
    
    public void ReadFromFile( Scanner s )
    {
        x = s.nextDouble();
        y = s.nextDouble();
        z = s.nextDouble();
    }    
    
    public void CopyTo( Vector3D v)
    {
       v.x = x;
       v.y = y;
       v.z = z;
    }

    public void FromPoints( Point3D v2, Point3D v1 )
    {
       this.x = v2.x - v1.x;
       this.y = v2.y - v1.y;
       this.z = v2.z - v1.z;
    }
    
   public void Reverse() 
   {
      x = -x;
      y = -y;
      z = -z;
   }

   public void Interpolate( Vector3D vA, Vector3D vB, double a ) 
   {
      x = a*vA.x + (1-a)*vB.x;
      y = a*vA.y + (1-a)*vB.y;
      z = a*vA.z + (1-a)*vB.z;
      Normalize();
   }   
   
   public double Length()
   {
      return Math.sqrt( x*x + y*y + z*z );
   }
   
   public double LengthSqr()
   {
      return x*x + y*y + z*z;
   }
   
   public void Random( double min, double max )
   {
      x = rand.nextDouble( min, max );
      y = rand.nextDouble( min, max );
      z = rand.nextDouble( min, max );
   }
}

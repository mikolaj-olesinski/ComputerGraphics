
public class Plane3D {
   // Plane equation: NP + d = 0
    public Vector3D  N;
    public double    d;  
    
    public void CreateFromVertices( Point3D v1, Point3D v2, Point3D v3 )
    {
        Vector3D w1, w2;
        
        w1 = new Vector3D( v2, v1 );
        w2 = new Vector3D( v3, v1 );
        
        N = w1.CrossProduct( w2 );
        N.Normalize();
        
        d = - N.DotProduct( v1 );        
    }
    
}

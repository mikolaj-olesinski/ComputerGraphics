
public abstract class Shape3D {
   public int mat_index;    
   public int part_index;
   public MatAttr  material;
   public RGBColor native_color;   
   public BoundingVolume  bv;
   public Point3D  center;
   public abstract boolean IsIntersected( RTRay ray );    
   public abstract void GetNormal( Point3D p, Vector3D N );  
   
   public void UpdateAttenuation( RGBColorFloat attenuation )
   {
      attenuation.r *= material.ktCr;
      attenuation.g *= material.ktCg;
      attenuation.b *= material.ktCb;      
   }
   
   public double GetCenterCoord( int axis )
   {
      switch (axis )  {
         case 0 : return center.x;
         case 1 : return center.y;
         case 2 : return center.z;
      }      
      return 0.0;
   }
}

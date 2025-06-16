import java.util.Scanner;

public class RGBColorFloat {
   public float r, g, b;
   public RGBColorFloat(float i, float j, float k) 
   {     
      r = i; g = j; b = k;
   }
   public RGBColorFloat() {
      r = g = b = 0.0f;
   }
   
   public void ReadFromFile( Scanner s )
   {
      r = s.nextFloat();
      g = s.nextFloat();
      b = s.nextFloat();
   }
   public void CopyTo(RGBColorFloat c) 
   {
      c.r = r;
      c.g = g;
      c.b = b;      
   }
}

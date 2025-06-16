
public class RGBColor {
   public int   R, G, B;
   public int   as_int;
   
   RGBColor()
   {
      this.R = 0;
      this.G = 0;
      this.B = 0;
   }
   
   RGBColor( int R, int G, int B )
   {
      this.R = R;
      this.G = G;
      this.B = B;
   }

   public void CopyTo( RGBColor c )
   {
      c.R = R;
      c.G = G;
      c.B = B;
   }   

  
   
}

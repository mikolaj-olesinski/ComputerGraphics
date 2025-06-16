
public class Line2D {
   
    // Line equation: ax + by + c = 0;
   public double  a, b, c;
   private double c_right;
      
   // Determines line parameters so as it contains points p1 and p2.
   // Point p3 is on the positive side of the line
   Line2D( Point2D p1, Point2D p2, Point2D p3)
   {
      double  vx = p2.x - p1.x;
      double  vy = p2.y - p1.y;
      
      double len = vx*vx + vy*vy;
      
      if ( len < RTUtils.EPS )
         throw new RTException( "Cannot create 2D line - points too close; p1: " +
                                 p1.x + " " + p1.y + "    p2: " + p2.x + " " + p2.y );
      len = Math.sqrt( len );
      
      a = vy;
      b = -vx;
      c = -( a*p1.x + b*p1.y );
            
      // Normalize the line equation
      a /= len;
      b /= len;
      c /= len; 
      
      // Make the line so as the point p3 is on the positive side of it
      // If after applying the line equation to the point p3 the left
      // side of the line equation is negative then multiply all line 
      // coefficients by -1
      if ( a*p3.x + b*p3.y + c < 0 )
      {
         a = -a;
         b = -b;
         c = -c;
      }
      c_right = -c - RTUtils.EPS;
      
      // For the sake of acceleration
      //c = -c + RTUtils.EPS;;
   }
   
   public double ApplyToPoint( Point2D p )
   {
      return a*p.x + b*p.y + c;
   } 
   public boolean TestPoint( Point2D p )
   {
      return a*p.x + b*p.y + c < -RTUtils.EPS ;
   }
   
   // Tests if the point pmid is located to the left of the line determined by
   // pl (lowest point) and pu (highest point); pmin y coord must be between pu.y and pl.y
   public boolean ToLeftSide( Point2D pu, Point2D pl, Point2D pmid )
   {
      double a = ( pmid.y - pl.y ) / ( pu.y - pl.y );
      double x_mid = a * pu.x + (1-a) * pl.x ;
      return pmid.x < x_mid;      
   } 

}

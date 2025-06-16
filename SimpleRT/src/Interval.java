
public class Interval
{
   public double v_min;
   public double v_max;
   
   Interval()
   {
      this.v_min = 0.0;
      this.v_max = 0.0;      
   }
   Interval( double v_min, double v_max )
   {
      this.v_min = v_min;
      this.v_max = v_max;
   }
   
   Interval( Interval other )
   {
      this.v_min = other.v_min;
      this.v_max = other.v_max;
   }
   
   public boolean intersects( Interval other )
   {
      return ( this.v_min > other.v_max ) || (other.v_min > this.v_max );
   }
   
   public Interval intersection( Interval other )
   {
      if (!intersects( other ))
         return null;
      else
         return new Interval( Math.max(this.v_min,  other.v_min),
                              Math.min(this.v_max, other.v_max ) );
   }
   
   public void MergeInplace( Interval other )
   {
      v_min = Math.min( v_min, other.v_min );
      v_max = Math.max( v_max, other.v_max );
   }
}

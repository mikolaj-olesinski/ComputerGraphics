import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList; 

public class BVHNode
{
   public BoundingVolume bv;
   public BVHNode  next_l, next_h;
   public Shape3D[]  shapes;
   public int trg_cnt;
   
   private int axis = 0;
   
   // Following constants established experimentally
   public  static int    MIN_NODE_OCCUPATION = 3;  // 5
   public  static int    BINS_CNT = 100;   
   private static double INTERSECT_T = 1.0;
   private static double TRAVERSAL_T = 0.6;        // 0.25
   
   public BVHNode()
   {
      next_l  = null;
      next_h  = null;
      shapes  = null;
      trg_cnt = 0;
      bv      = new BoundingVolume();      
   }   
  
   private static Interval bb_interval = new Interval();
   
   public static BVHNode CreateTree( Shape3D members[], int first, int last )
   {
      // System.out.println( "In create Tree " + first + " " + last + " " + members.length );
      if ( members.length == 0)
         return null;      
      
      BVHNode node = new BVHNode();
      total_nodes++;
      node.bv = MakeBB( members, first, last );
      node.trg_cnt = last - first + 1;
      
      if (node.trg_cnt <= MIN_NODE_OCCUPATION )
      {
         int len = last-first+1;
         node.shapes = new Shape3D[len];
         total_trgs += len;
         for ( int i = 0; i < len; i++ )
            node.shapes[i] = members[i+first];         
         leave_nodes++;
         return node;
      }
      
      // Split is required
      
      // Find the longest axis of bv
      Interval[] spans = FindSpans( members, first, last );
      int  axis = 0;
      double max_span = 0;
      for ( int i = 0; i < 3; i++ )
      {
         double span = spans[i].v_max - spans[i].v_min;
         if ( span > max_span )
         {
            max_span = span;
            axis = i;
         }
      }
      
      Shape3D sorted[] = SortShapesByAxis( members, first, last, axis );
      int sorted_len = sorted.length;
      int half = sorted_len / 2;
      
      // System.out.println( "   Building left " + (half - 0 + 1) ); 
      node.next_l = CreateTree( sorted, 0, half );
      // System.out.println( "   Building right " + ((sorted_len - 1) - half+1 + 1)); 
      node.next_h = CreateTree( sorted, half+1, sorted_len-1 );     
            
      return node;
   }
   public static Interval[] FindSpans( Shape3D members[], int first, int last )
   {
      if ( last < first )
         return null;      

      if ((first == 0) && (last == 0))
         last = members.length - 1;
      
      Interval[] spans = new Interval[3];
      spans[0] = new Interval();
      spans[1] = new Interval();
      spans[2] = new Interval();
      
      spans[0].v_min = members[first].center.x;
      spans[0].v_max = members[first].center.x;
      spans[1].v_min = members[first].center.y;
      spans[1].v_max = members[first].center.y;
      spans[2].v_min = members[first].center.z;
      spans[2].v_max = members[first].center.z;
      
      for( int i = first+1; i <= last; i++ )
      {
         spans[0].v_min = Math.min( spans[0].v_min, members[i].center.x );
         spans[0].v_max = Math.max( spans[0].v_max, members[i].center.x );
         spans[1].v_min = Math.min( spans[1].v_min, members[i].center.y );
         spans[1].v_max = Math.max( spans[1].v_max, members[i].center.y );
         spans[2].v_min = Math.min( spans[2].v_min, members[i].center.z );
         spans[2].v_max = Math.max( spans[2].v_max, members[i].center.z );         
      }
      
      return spans;
   }
   
   public static BoundingVolume MakeBB( Shape3D members[], int first, int last )
   {  
      if ( last < first )
         return null;
    
      if ((first == 0) && (last == 0))
         last = members.length - 1;
      
      BoundingVolume bv = new BoundingVolume( members[first].bv );
      
      for( int i = first+1; i <= last; i++ )
         bv.MergeInplace(members[i].bv );
      
      bv.AddMargin();
      
      return bv;
   }
   
   private static Shape3D[] SortShapesByAxis( Shape3D members[], int first, int last, int axis )
   {
      if ( last < first )
         return null;      

      if ((first == 0) && (last == 0))
         last = members.length - 1;
      
      Shape3D[] sorted = new Shape3D[ last - first + 1 ];
      
      System.arraycopy(members, first, sorted, 0, last - first + 1);
      
      switch (axis) {
         case 0:
            Arrays.sort(sorted, new Comparator<Shape3D>() {
               @Override
               public int compare(Shape3D p1, Shape3D p2) {
                   return Double.compare(p1.center.x, p2.center.x);
               }
            });      
            break;
         case 1:
            Arrays.sort(sorted, new Comparator<Shape3D>() {
               @Override
               public int compare(Shape3D p1, Shape3D p2) {
                   return Double.compare(p1.center.y, p2.center.y);
               }
            });      
            break;
         case 2:
            Arrays.sort(sorted, new Comparator<Shape3D>() {
               @Override
               public int compare(Shape3D p1, Shape3D p2) {
                   return Double.compare(p1.center.z, p2.center.z);
               }
            });      
            break;
      }       
      return sorted;
   }
   
   private static double ComputeCost( BoundingVolume bv1, BoundingVolume bv2, BoundingVolume bv12, int bv1_cnt, int bv2_cnt )
   {
      return 2*TRAVERSAL_T +  (INTERSECT_T /bv12.SVH() )* ( bv1_cnt *bv1.SVH() +  bv2_cnt *bv2.SVH() );      
   }
   
   public static BVHNode CreateTreeSAH( Shape3D members[], int first, int last )
   {
      // System.out.println( "In create Tree " + first + " " + last + " " + members.length );
      if ( members.length == 0)
         return null;      

      BVHNode node = new BVHNode();
      total_nodes++;
      node.bv = MakeBB( members, first, last );
      node.trg_cnt = last - first + 1;      

      if (node.trg_cnt <= MIN_NODE_OCCUPATION )
      {
         int len = last-first+1;
         node.shapes = new Shape3D[len];
         total_trgs += len;
         for ( int i = 0; i < len; i++ )
            node.shapes[i] = members[i+first];         
         leave_nodes++;
         return node;
      }      
      
      // Split is required
      
      double[]       bin_bounds   = new double[BINS_CNT+1];      
      BoundingVolume tmp_bv       = new BoundingVolume();      
      int            best_axis    = -1;
      int            best_l_index = -1;
      double         best_cost    = RTUtils.INFINITY;      
      Shape3D[][]    sorted       = new Shape3D[3][];
      int            shapes_cnt   = -1;
      
      // Try x axes
      for ( int axis = 0; axis < 3; axis++ )
      {
         int l_shp_index = 0; 

         sorted[axis] = SortShapesByAxis(members, first, last, axis);
         shapes_cnt = sorted[axis].length;
         
         double size = node.bv.GetSize( axis );
         size = sorted[axis][shapes_cnt-1].GetCenterCoord( axis ) - 
                sorted[axis][0].GetCenterCoord( axis );
         
         double left_bound = node.bv.GetMin( axis );    
         left_bound = sorted[axis][0].GetCenterCoord( axis );
         double increment = size / BINS_CNT;         

         for ( int bin_index = 0; bin_index <= BINS_CNT; bin_index ++)
            bin_bounds[bin_index] = left_bound + bin_index * increment;
         bin_bounds[ BINS_CNT] += 0.01;
         bin_bounds[ 0 ] -= 0.01;
         
         double h_bound = bin_bounds[1];
         int previous_l_shp_index = 0;
         
         tmp_bv.PrapareForMerge();
         
         for ( int bin_index = 0; bin_index < BINS_CNT; bin_index ++)
         {            
            h_bound = bin_bounds[ bin_index + 1];           
            
            while (( l_shp_index < shapes_cnt ) && (sorted[axis][l_shp_index].GetCenterCoord(axis) <= h_bound) )
            {
               tmp_bv.MergeInplace( sorted[axis][l_shp_index].bv );
               l_shp_index++;
            }
            
            if ( l_shp_index >= shapes_cnt )
               // This is the last bin - all shapes assigned to left subset 
               // further processing does not make sense
               break;
            
            if ( l_shp_index == previous_l_shp_index )
               // no change in L/H sets - continue
               continue;
            
            previous_l_shp_index = l_shp_index;
            
            BoundingVolume right_bv = MakeBB( sorted[axis], l_shp_index, sorted[axis].length - 1);
            
            double cost = ComputeCost( tmp_bv, right_bv, node.bv, l_shp_index, shapes_cnt-l_shp_index );
            if ( cost < best_cost )
            {
               best_cost = cost;
               best_axis = axis;
               best_l_index = l_shp_index - 1;
            }
         }         
      }      
     
      if ( best_axis == -1)
      {
         // We were not able to find split - find the longest axis of bv
         
         // Find the longest axis of bv
         Interval[] spans = FindSpans( members, first, last );
         double max_span = 0;
         for ( int i = 0; i < 3; i++ )
         {
            double span = spans[i].v_max - spans[i].v_min;
            if ( span > max_span )
            {
               max_span = span;
               best_axis = i;
            }
         }
         
         best_l_index =  shapes_cnt / 2;
      }
      
      if ( best_cost > INTERSECT_T * node.trg_cnt )
         {
            int len = last-first+1;
            node.shapes = new Shape3D[len];
            total_trgs += len;
            for ( int i = 0; i < len; i++ )
               node.shapes[i] = members[i+first];         
            leave_nodes++;
            return node;
         }              
      
      // System.out.println( "   Building left " + (best_l_index + 1) ); 
      node.next_l = CreateTreeSAH( sorted[best_axis], 0, best_l_index );
      // System.out.println( "   Building right " + ((shapes_cnt - 1) - best_l_index+1 + 1)); 
      node.next_h = CreateTreeSAH( sorted[best_axis], best_l_index+1, shapes_cnt-1 );     
            
      return node;
   }
   
   public static int total_trgs = 0;
   public static int total_nodes = 0;
   public static int leave_nodes = 0;
   
   public void DisplayTree(int level)
   {
      if ( shapes != null )
         total_trgs += shapes.length;
      
      String indent = "";
      String msg = "";
      for ( int i = 0; i < level; i++ )
         indent += "   ";
      indent = indent + level + " Trg# " + trg_cnt + " Span x:" + 
            bv.intv_x.v_min + " - " + bv.intv_x.v_max + "   " + 
            bv.intv_y.v_min + " - " + bv.intv_y.v_max + "   " + 
            bv.intv_z.v_min + " - " + bv.intv_z.v_max + "   ";
      System.out.println( indent );
      if ( next_l != null )
         next_l.DisplayTree( level + 1);
      if ( next_h != null )
         next_h.DisplayTree( level + 1);  
   }
   
   public Shape3D FindClosestIntersection( RTRay ray, Shape3D excluded, Point3D int_point )
   {
      if ( !bv.IsIntersected( ray  ))
         return null;
      
      if ( shapes != null)
      {
         // This is leave node
         int intersected_shp_ind = -1;
         int shp_count = shapes.length;
         
         for ( int i = 0; i < shp_count; i++ )
         {
            // Do not test excluded face
            if ( shapes[i] == excluded )
               continue;
            if ( shapes[i].IsIntersected(ray) )
            {
               int_point.x = Triangle.int_point.x;
               int_point.y = Triangle.int_point.y;            
               int_point.z = Triangle.int_point.z;
               
               intersected_shp_ind = i;
            }
         }
         if (intersected_shp_ind == -1)
            return null;
         else
            return shapes[intersected_shp_ind];
      }
      
      Shape3D l_shape = null;
      Shape3D h_shape = null;
      
      if (next_l != null)
         l_shape = next_l.FindClosestIntersection( ray, excluded, int_point );
      if (next_h != null)
         h_shape = next_h.FindClosestIntersection( ray, excluded, int_point );
      
      if ( h_shape != null )
         return h_shape;
      else
         return l_shape;       
   }   
   
   public boolean FindLightAttenuation( RTRay ray, RGBColorFloat attenuation, Shape3D excluded )
   {
      if ( !bv.IsIntersected( ray ))
         return true;

      if ( shapes != null)
      {      
         int shp_count = shapes.length;
         attenuation.r = attenuation.g = attenuation.b = (float)1.0;
      
         for ( int i = 0; i < shp_count; i++ )
         {
            // Do not test excluded face
            if ( shapes[i] == excluded )
               continue;
   
            if ( shapes[i].IsIntersected(ray) )
            {
               shapes[i].UpdateAttenuation( attenuation );
               if ( ( attenuation.r < RTUtils.EPS ) && ( attenuation.g < RTUtils.EPS ) &&
                    ( attenuation.b < RTUtils.EPS ) )
                  return false;
               // This is to avoid rejection of more distant transparent objects
               ray.t_int = RTUtils.INFINITY;
            }
         }
   
         return true;
      }
 
      boolean l_transfer = true;
      boolean h_transfer = true;
      
      if (next_l != null)
         l_transfer = next_l.FindLightAttenuation( ray, attenuation, excluded );
      if ( ! l_transfer )
         return false;
      
      if (next_h != null)
         h_transfer = next_h.FindLightAttenuation( ray, attenuation, excluded );
      
      return h_transfer;
     
   }    
}

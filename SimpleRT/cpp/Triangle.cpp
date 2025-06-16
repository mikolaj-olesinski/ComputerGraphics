// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Triangle.h"
#include "BoundingVolume.h"
#include "_BoundingVolume.h"
#include "RGBColor.h"

using Scanner = java::util::Scanner;

void Triangle::ReadFromFile(std::shared_ptr<Scanner> s, std::vector<std::shared_ptr<Point3D>> &vertices)
{
   i1 = s->nextInt();
   i2 = s->nextInt();
   i3 = s->nextInt();

   // Evaluate triangle plane parameters
   plane = std::make_shared<Plane3D>();
   plane->CreateFromVertices(vertices[i1], vertices[i2], vertices[i3]);

   // Select Driving plane
   double a = std::abs(plane->N->x);
   double b = std::abs(plane->N->y);
   double c = std::abs(plane->N->z);
   double xbbb_min = std::min(vertices[i1]->x, std::min(vertices[i2]->x, vertices[i3]->x)) - RTUtils::EPS;
   double xbbb_max = std::max(vertices[i1]->x, std::max(vertices[i2]->x, vertices[i3]->x)) + RTUtils::EPS;
   double ybbb_min = std::min(vertices[i1]->y, std::min(vertices[i2]->y, vertices[i3]->y)) - RTUtils::EPS;
   double ybbb_max = std::max(vertices[i1]->y, std::max(vertices[i2]->y, vertices[i3]->y)) + RTUtils::EPS;
   double zbbb_min = std::min(vertices[i1]->z, std::min(vertices[i2]->z, vertices[i3]->z)) - RTUtils::EPS;
   double zbbb_max = std::max(vertices[i1]->z, std::max(vertices[i2]->z, vertices[i3]->z)) + RTUtils::EPS;

   center = std::make_shared<Point3D>();
   center->x = (xbbb_max + xbbb_min) / 2.0;
   center->y = (ybbb_max + ybbb_min) / 2.0;
   center->z = (zbbb_max + zbbb_min) / 2.0;

   if (a >= std::max(b, c))
   {
	  driving_plane = RTUtils::DRIVING_PLANES::EXCLUDE_X;
	  xbb_min = ybbb_min;
	  xbb_max = ybbb_max;
	  ybb_min = zbbb_min;
	  ybb_max = zbbb_max;
   }
   else
   {
   if (b >= std::max(a, c))
   {
	  driving_plane = RTUtils::DRIVING_PLANES::EXCLUDE_Y;
	  xbb_min = xbbb_min;
	  xbb_max = xbbb_max;
	  ybb_min = zbbb_min;
	  ybb_max = zbbb_max;
   }
   else
   {
	  driving_plane = RTUtils::DRIVING_PLANES::EXCLUDE_Z;
	  xbb_min = xbbb_min;
	  xbb_max = xbbb_max;
	  ybb_min = ybbb_min;
	  ybb_max = ybbb_max;
   }
   }

   bv = std::make_shared<BoundingVolume>(xbbb_min, xbbb_max, ybbb_min, ybbb_max, zbbb_min, zbbb_max);

   std::shared_ptr<Point2D> p1 = vertices[i1]->ConvertTo2D(driving_plane);
   std::shared_ptr<Point2D> p2 = vertices[i2]->ConvertTo2D(driving_plane);
   std::shared_ptr<Point2D> p3 = vertices[i3]->ConvertTo2D(driving_plane);
   x1 = p1->x;
   y1 = p1->y;
   x2 = p2->x;
   y2 = p2->y;
   x3 = p3->x;
   y3 = p3->y;

   l12 = std::make_shared<Line2D>(p1, p2, p3);
   l13 = std::make_shared<Line2D>(p1, p3, p2);
   l23 = std::make_shared<Line2D>(p2, p3, p1);

   // Assign random colors to triangles - just for testing purposes
   native_color = std::make_shared<RGBColor>();
   native_color->R = static_cast<int>(255 * Math::random());
   native_color->G = static_cast<int>(255 * Math::random());
   native_color->B = static_cast<int>(255 * Math::random());
   native_color->as_int = RTUtils::byte2RGB(native_color->R, native_color->G, native_color->B); //native_color.R << 16 + native_color.G <<8 + native_color.B;

   // edge1 = Vector3D.Sub( vertices[i2], vertices[i1] );
   // edge2 = Vector3D.Sub( vertices[i3], vertices[i1] );
   // vertex_0 = vertices[i1];       
}

bool Triangle::IsIntersected(std::shared_ptr<RTRay> ray)
{
   double t;

   // Count intersection tests
   total_ints++;

   // Find intersection with triangle plane
   double Nu = plane->N->DotProduct(ray->u);

   // This test can be skipped because division by 0 leads to t = infinity
   // according to IEEE 754 recommendation
   // if ( Math.abs( Nu) < RTUtils.EPS )
   //   return false;       

   t = -(plane->N->DotProduct(ray->P) + plane->d) / Nu;

   // No intersection if the plane is intersected on the opposite side of the ray
   // or it is behind the other intersected facet        
   if ((t > ray->t_max) || (t < RTUtils::EPS) || (t > ray->t_int))
   {
	  return false;
   }

   int_point->x = ray->P->x + t * ray->u->x;
   int_point->y = ray->P->y + t * ray->u->y;
   int_point->z = ray->P->z + t * ray->u->z;

   // Project into 2D
   int_point->ConvertTo2D(driving_plane, int_point_2D);

   if ((int_point_2D->x < xbb_min) || (int_point_2D->x > xbb_max))
   {
	  return false;
   }
   if ((int_point_2D->y < ybb_min) || (int_point_2D->y > ybb_max))
   {
	  return false;
   }

   // Test location with respect to triangle edges
   if (l12->TestPoint(int_point_2D))
   {
	  return false;
   }

   if (l13->TestPoint(int_point_2D))
   {
	  return false;
   }

   if (l23->TestPoint(int_point_2D))
   {
	  return false;
   }

   ray->t_int = t;
   ray->int_point = int_point;

   return true;
}

void Triangle::GetNativeNormal(std::shared_ptr<Vector3D> N)
{
   plane->N->CopyTo(N);
   return;
}

void Triangle::GetNormal(std::shared_ptr<Point3D> p, std::shared_ptr<Vector3D> N)
{
   if (!use_interpolated_normals)
   {
	  plane->N->CopyTo(N);
	  return;
   }

   // Find baycentric coordinates
   p->ConvertTo2D(driving_plane, int_point_2D);

   double x = int_point_2D->x;
   double y = int_point_2D->y;
   double l1 = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
   double l2 = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
   double l3 = 1.0 - l1 - l2;


   N->x = l1 * p1_normal->x + l2 * p2_normal->x + l3 * p3_normal->x;
   N->y = l1 * p1_normal->y + l2 * p2_normal->y + l3 * p3_normal->y;
   N->z = l1 * p1_normal->z + l2 * p2_normal->z + l3 * p3_normal->z;
   N->Normalize();
}

void Triangle::PrepareNormalInterpolationData(std::vector<std::shared_ptr<Point3D>> &vertices)
{
   double a;

   std::shared_ptr<Point2D> p1 = vertices[i1]->ConvertTo2D(driving_plane);
   std::shared_ptr<Point2D> p2 = vertices[i2]->ConvertTo2D(driving_plane);
   std::shared_ptr<Point2D> p3 = vertices[i3]->ConvertTo2D(driving_plane);

   use_interpolated_normals = true;

   // Order by y on driving plane
   if ((p1->y >= p2->y) && (p1->y >= p3->y))
   {
	  // p1 is highest
	  if (p2->y > p3->y)
	  {
		 // CASE 1: p1 is highest; p2 is mid point
		 y_mid = static_cast<float>(p2->y);
		 v1_x = p1->x;
		 v2_x = p2->x;
		 v3_x = p3->x;
		 a = (p2->y - p3->y) / (p1->y - p3->y);
		 v4_x = a * p1->x + (1 - a) * p3->x;
		 v1 = p1_normal;
		 v2 = p2_normal;
		 v3 = p3_normal;
		 v4 = std::make_shared<Vector3D>();
		 v4->Interpolate(v1, v3, a);
	  }
	  else
	  {
		 // p3 is mid point
		 // CASE 2: p1 is highest; p3 is mid point
		 y_mid = static_cast<float>(p3->y);
		 v1_x = p1->x;
		 v2_x = p3->x;
		 v3_x = p2->x;
		 a = (p3->y - p2->y) / (p1->y - p2->y);
		 v4_x = a * p1->x + (1 - a) * p2->x;
		 v1 = p1_normal;
		 v2 = p3_normal;
		 v3 = p2_normal;
		 v4 = std::make_shared<Vector3D>();
		 v4->Interpolate(v1, v3, a);
	  }
   }
   else
   {
	  if ((p2->y >= p1->y) && (p2->y >= p3->y))
	  {
		 // p2 is highest
		 if (p1->y > p3->y)
		 {
			// CASE 3: p2 is highest; p1 is mid point
			y_mid = static_cast<float>(p1->y);
			v1_x = p2->x;
			v2_x = p1->x;
			v3_x = p3->x;
			a = (p1->y - p3->y) / (p2->y - p3->y);
			v4_x = a * p2->x + (1 - a) * p3->x;
			v1 = p2_normal;
			v2 = p1_normal;
			v3 = p3_normal;
			v4 = std::make_shared<Vector3D>();
			v4->Interpolate(v2, v3, a);
		 }
		 else
		 {
			// p3 is mid point
			// CASE 4: p2 is highest; p3 is mid point
			y_mid = static_cast<float>(p3->y);
			v1_x = p2->x;
			v2_x = p3->x;
			v3_x = p1->x;
			a = (p3->y - p1->y) / (p2->y - p1->y);
			v4_x = a * p2->x + (1 - a) * p1->x;
			v1 = p2_normal;
			v2 = p3_normal;
			v3 = p1_normal;
			v4 = std::make_shared<Vector3D>();
			v4->Interpolate(v2, v1, a);
		 }
	  }
	  else
	  {
		 // p3 is highest
		 if (p2->y > p1->y)
		 {
			// CASE 5: p3 is highest; p2 is mid point
			y_mid = static_cast<float>(p2->y);
			v1_x = p3->x;
			v2_x = p2->x;
			v3_x = p1->x;
			a = (p2->y - p1->y) / (p3->y - p1->y);
			v4_x = a * p3->x + (1 - a) * p1->x;
			v1 = p3_normal;
			v2 = p2_normal;
			v3 = p1_normal;
			v4 = std::make_shared<Vector3D>();
			v4->Interpolate(v3, v1, a);

		 }
		 else
		 {
			// CASE 5: p3 is highest; p1 is mid point
			y_mid = static_cast<float>(p1->y);
			v1_x = p3->x;
			v2_x = p1->x;
			v3_x = p2->x;
			a = (p1->y - p1->y) / (p3->y - p2->y);
			v4_x = a * p3->x + (1 - a) * p1->x;
			v1 = p3_normal;
			v2 = p1_normal;
			v3 = p2_normal;
			v4 = std::make_shared<Vector3D>();
			v4->Interpolate(v3, v2, a);
		 }
	  }
   }
}

void Triangle::AddAvgNormal(std::shared_ptr<Vector3D> normal, int ind)
{
   switch (ind)
   {
	  case 1 :
		 p1_normal = normal;
		 break;
	  case 2 :
		 p2_normal = normal;
		 break;
	  case 3 :
		 p3_normal = normal;
		 break;
   }
}

void Triangle::CorrectAvgNormals()
{
   // Use p1_normal as beasline
   if (p1_normal->DotProduct(p2_normal) < 0)
   {
	  p2_normal->Reverse();
   }
   if (p1_normal->DotProduct(p3_normal) < 0)
   {
	  p3_normal->Reverse();
   }
}

std::shared_ptr<Point3D> Triangle::DisplaceIntPoint(std::shared_ptr<Point3D> int_point, std::shared_ptr<Point3D> observer, std::vector<std::shared_ptr<Point3D>> &vertices)
{
   if (!use_interpolated_normals)
   {
	  return int_point;
   }
   std::shared_ptr<Vector3D> vect_to_observer = std::make_shared<Vector3D>();

   // Find barycentric coordinates

   int_point->ConvertTo2D(driving_plane, int_point_2D);

   double x = int_point_2D->x;
   double y = int_point_2D->y;
   double l1 = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
   double l2 = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
   double l3 = 1.0 - l1 - l2;

   vect_to_observer->x = observer->x - int_point->x;
   vect_to_observer->y = observer->y - int_point->y;
   vect_to_observer->z = observer->z - int_point->z;

   // set to 1.0 iff observer of the same side than plane normal
   double reverse = 1.0;

   // Orient interpolated normals towards observer
   if (plane->N->DotProduct(vect_to_observer) > 0)
   {
	  // Observer on this side pointed out by plane normal
	  reverse = 1.0;
	  // Orient vertex normals towards N
	  if (p1_normal->DotProduct(plane->N) < 0)
	  {
		 p1_normal->Reverse();
	  }
	  if (p2_normal->DotProduct(plane->N) < 0)
	  {
		 p2_normal->Reverse();
	  }
	  if (p3_normal->DotProduct(plane->N) < 0)
	  {
		 p3_normal->Reverse();
	  }
   }
   else
   {
	  // Observer on the opposite side tnan pointed out by plane normal
	  reverse = -1.0;
	  // Orient vertex normals opposite to N
	  if (p1_normal->DotProduct(plane->N) > 0)
	  {
		 p1_normal->Reverse();
	  }
	  if (p2_normal->DotProduct(plane->N) > 0)
	  {
		 p2_normal->Reverse();
	  }
	  if (p3_normal->DotProduct(plane->N) > 0)
	  {
		 p3_normal->Reverse();
	  }
   }

   // Vector from the vertex to intersection point
   std::shared_ptr<Vector3D> p_vx = std::make_shared<Vector3D>();

   //       /\
   //       /
   //      /
   //     / / 
   //  V *----------------------------- P
   //       \


   // Process p_vx = p_int - > V1 
   p_vx->x = vertices[i1]->x - int_point->x;
   p_vx->y = vertices[i1]->y - int_point->y;
   p_vx->z = vertices[i1]->z - int_point->z;

   // bcause pv_x is not normalized - then the product below takes into ac count the distance from
   // the vertex to the intersection point
   double dhdd_1 = p_vx->DotProduct(p1_normal);

   // make it dependent on the corresponding barycentric coordinate
   double dh_1 = dhdd_1 * (1 - l1);

   p_vx->x = vertices[i2]->x - int_point->x;
   p_vx->y = vertices[i2]->y - int_point->y;
   p_vx->z = vertices[i2]->z - int_point->z;
   double dhdd_2 = p_vx->DotProduct(p2_normal);
   double dh_2 = dhdd_2 * (1 - l2);

   p_vx->x = vertices[i3]->x - int_point->x;
   p_vx->y = vertices[i3]->y - int_point->y;
   p_vx->z = vertices[i3]->z - int_point->z;
   double dhdd_3 = p_vx->DotProduct(p3_normal);
   double dh_3 = dhdd_3 * (1 - l3);

   // The strength constant set experimentally
   // Skip concave manifolds
   if (dh_1 + dh_2 + dh_3 > 0)
   {
	  std::shared_ptr<Point3D> displ_point = std::make_shared<Point3D>();
	  displ_point->x = int_point->x + 0.15 * (dh_1 + dh_2 + dh_3) * plane->N->x * reverse;
	  displ_point->y = int_point->y + 0.15 * (dh_1 + dh_2 + dh_3) * plane->N->y * reverse;
	  displ_point->z = int_point->z + 0.15 * (dh_1 + dh_2 + dh_3) * plane->N->z * reverse;
	  return displ_point;
   }

   return int_point;
}

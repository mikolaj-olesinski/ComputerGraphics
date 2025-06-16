// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Point3D.h"

using Scanner = java::util::Scanner;

Point3D::Point3D()
{
   x = y = z = 0.0;
}

Point3D::Point3D(std::shared_ptr<Point3D> p)
{
   x = p->x;
   y = p->y;
   z = p->z;
}

void Point3D::CopyTo(std::shared_ptr<Point3D> p)
{
   p->x = x;
   p->y = y;
   p->z = z;
}

Point3D::Point3D(double x, double y, double z)
{
   this->x = x;
   this->y = y;
   this->z = z;
}

void Point3D::ReadFromFile(std::shared_ptr<Scanner> s)
{
	x = s->nextDouble();
	y = s->nextDouble();
	z = s->nextDouble();
}

void Point3D::ConvertTo2D(RTUtils::DRIVING_PLANES driving_plane, std::shared_ptr<Point2D> p)
{

   if (driving_plane == RTUtils::DRIVING_PLANES::EXCLUDE_X)
   {
	  p->x = y;
	  p->y = z;
   }
   else
   {
	  if (driving_plane == RTUtils::DRIVING_PLANES::EXCLUDE_Y)
	  {
		 p->x = x;
		 p->y = z;
	  }
	  else
	  {
		 p->x = x;
		 p->y = y;
	  }
   }
}

std::shared_ptr<Point2D> Point3D::ConvertTo2D(RTUtils::DRIVING_PLANES driving_plane)
{
   std::shared_ptr<Point2D> p = std::make_shared<Point2D>(0.0, 0.0);

   ConvertTo2D(driving_plane, p);

   return p;
}

void Point3D::Add(std::shared_ptr<Vector3D> v)
{
   x += v->x;
   y += v->y;
   z += v->z;
}

void Point3D::Sub(std::shared_ptr<Vector3D> v)
{
   x -= v->x;
   y -= v->y;
   z -= v->z;
}

void Point3D::Sub(std::shared_ptr<Point3D> res, std::shared_ptr<Point3D> p1, std::shared_ptr<Point3D> p2)
{
   res->x = p1->x - p2->x;
   res->y = p1->y - p2->y;
   res->z = p1->z - p2->z;
}

void Point3D::Show(const std::wstring &s)
{
   std::wcout << s << L" x=" << x << L" y=" << y << L" z=" << z << std::endl;
}

double Point3D::DotProduct(std::shared_ptr<Vector3D> v)
{
   return x * v->x + y * v->y + z * v->z;
}

void Point3D::CrossProduct(std::shared_ptr<Vector3D> out, std::shared_ptr<Vector3D> v)
{
   out->x = y * v->z - z * v->y;
   out->y = z * v->x - x * v->z;
   out->z = x * v->y - y * v->x;
}

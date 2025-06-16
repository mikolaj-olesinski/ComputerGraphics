// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Vector3D.h"
#include "RTUtils.h"
#include "RTException.h"

using Scanner = java::util::Scanner;
using Random = java::util::Random;

Vector3D::Vector3D()
{
   x = 1.0;
   y = z = 0.0;
}

Vector3D::Vector3D(double x, double y, double z)
{
   this->x = x;
   this->y = y;
   this->z = z;
}

Vector3D::Vector3D(std::shared_ptr<Point3D> v2, std::shared_ptr<Point3D> v1)
{
   this->x = v2->x - v1->x;
   this->y = v2->y - v1->y;
   this->z = v2->z - v1->z;
}

std::shared_ptr<Vector3D> Vector3D::Sub(std::shared_ptr<Vector3D> v2, std::shared_ptr<Vector3D> v1)
{
   std::shared_ptr<Vector3D> result = std::make_shared<Vector3D>();
   result->x = v2->x - v1->x;
   result->y = v2->y - v1->y;
   result->z = v2->z - v1->z;

   return result;
}

std::shared_ptr<Vector3D> Vector3D::Sub(std::shared_ptr<Point3D> v2, std::shared_ptr<Point3D> v1)
{
   std::shared_ptr<Vector3D> result = std::make_shared<Vector3D>();
   result->x = v2->x - v1->x;
   result->y = v2->y - v1->y;
   result->z = v2->z - v1->z;

   return result;
}

void Vector3D::Normalize()
{
   double len;
   len = std::sqrt(x * x + y * y + z * z);
   if (len < RTUtils::EPS)
   {
	  throw RTException(L"Cannot normalize a vector ");
   }
   x /= len;
   y /= len;
   z /= len;
}

double Vector3D::DotProduct(std::shared_ptr<Vector3D> v)
{
   return x * v->x + y * v->y + z * v->z;
}

double Vector3D::DotProduct(std::shared_ptr<Point3D> v)
{
   return x * v->x + y * v->y + z * v->z;
}

std::shared_ptr<Vector3D> Vector3D::CrossProduct(std::shared_ptr<Vector3D> v)
{
   std::shared_ptr<Vector3D> out = std::make_shared<Vector3D>();
   out->x = y * v->z - z * v->y;
   out->y = z * v->x - x * v->z;
   out->z = x * v->y - y * v->x;
   return out;
}

std::shared_ptr<Vector3D> Vector3D::CrossProduct(std::shared_ptr<Vector3D> out, std::shared_ptr<Vector3D> v)
{
   out->x = y * v->z - z * v->y;
   out->y = z * v->x - x * v->z;
   out->z = x * v->y - y * v->x;
   return out;
}

double Vector3D::GetLength()
{
   return std::sqrt(x * x + y * y + z * z);
}

void Vector3D::ScalarMult(double t)
{
   x *= t;
   y *= t;
   z *= t;
}

void Vector3D::ReadFromFile(std::shared_ptr<Scanner> s)
{
	x = s->nextDouble();
	y = s->nextDouble();
	z = s->nextDouble();
}

void Vector3D::CopyTo(std::shared_ptr<Vector3D> v)
{
   v->x = x;
   v->y = y;
   v->z = z;
}

void Vector3D::FromPoints(std::shared_ptr<Point3D> v2, std::shared_ptr<Point3D> v1)
{
   this->x = v2->x - v1->x;
   this->y = v2->y - v1->y;
   this->z = v2->z - v1->z;
}

void Vector3D::Reverse()
{
   x = -x;
   y = -y;
   z = -z;
}

void Vector3D::Interpolate(std::shared_ptr<Vector3D> vA, std::shared_ptr<Vector3D> vB, double a)
{
   x = a * vA->x + (1 - a) * vB->x;
   y = a * vA->y + (1 - a) * vB->y;
   z = a * vA->z + (1 - a) * vB->z;
   Normalize();
}

double Vector3D::Length()
{
   return std::sqrt(x * x + y * y + z * z);
}

double Vector3D::LengthSqr()
{
   return x * x + y * y + z * z;
}

void Vector3D::Random(double min, double max)
{
   x = rand->nextDouble(min, max);
   y = rand->nextDouble(min, max);
   z = rand->nextDouble(min, max);
}

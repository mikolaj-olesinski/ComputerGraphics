// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Line2D.h"
#include "RTUtils.h"
#include "RTException.h"

Line2D::Line2D(std::shared_ptr<Point2D> p1, std::shared_ptr<Point2D> p2, std::shared_ptr<Point2D> p3)
{
   double vx = p2->x - p1->x;
   double vy = p2->y - p1->y;

   double len = vx * vx + vy * vy;

   if (len < RTUtils::EPS)
   {
	  throw RTException(L"Cannot create 2D line - points too close; p1: " + std::to_wstring(p1->x) + L" " + std::to_wstring(p1->y) + L"    p2: " + std::to_wstring(p2->x) + L" " + std::to_wstring(p2->y));
   }
   len = std::sqrt(len);

   a = vy;
   b = -vx;
   c = -(a * p1->x + b * p1->y);

   // Normalize the line equation
   a /= len;
   b /= len;
   c /= len;

   // Make the line so as the point p3 is on the positive side of it
   // If after applying the line equation to the point p3 the left
   // side of the line equation is negative then multiply all line 
   // coefficients by -1
   if (a * p3->x + b * p3->y + c < 0)
   {
	  a = -a;
	  b = -b;
	  c = -c;
   }
   c_right = -c - RTUtils::EPS;

   // For the sake of acceleration
   //c = -c + RTUtils.EPS;
}

double Line2D::ApplyToPoint(std::shared_ptr<Point2D> p)
{
   return a * p->x + b * p->y + c;
}

bool Line2D::TestPoint(std::shared_ptr<Point2D> p)
{
   return a * p->x + b * p->y + c < -RTUtils::EPS;
}

bool Line2D::ToLeftSide(std::shared_ptr<Point2D> pu, std::shared_ptr<Point2D> pl, std::shared_ptr<Point2D> pmid)
{
   double a = (pmid->y - pl->y) / (pu->y - pl->y);
   double x_mid = a * pu->x + (1 - a) * pl->x;
   return pmid->x < x_mid;
}

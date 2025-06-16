#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Point2D.h"
#include <cmath>
#include <memory>

class Line2D : public std::enable_shared_from_this<Line2D>
{

	// Line equation: ax + by + c = 0;
   public:
   double a = 0.0, b = 0.0, c = 0.0;
   private:
   double c_right = 0.0;

   // Determines line parameters so as it contains points p1 and p2.
   // Point p3 is on the positive side of the line
   public:
   Line2D(std::shared_ptr<Point2D> p1, std::shared_ptr<Point2D> p2, std::shared_ptr<Point2D> p3);

   virtual double ApplyToPoint(std::shared_ptr<Point2D> p);
   virtual bool TestPoint(std::shared_ptr<Point2D> p);

   // Tests if the point pmid is located to the left of the line determined by
   // pl (lowest point) and pu (highest point); pmin y coord must be between pu.y and pl.y
   virtual bool ToLeftSide(std::shared_ptr<Point2D> pu, std::shared_ptr<Point2D> pl, std::shared_ptr<Point2D> pmid);

};

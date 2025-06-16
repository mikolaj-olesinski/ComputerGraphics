#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include <memory>

class Point2D : public std::enable_shared_from_this<Point2D>
{
   public:
   double x = 0.0;
   double y = 0.0;

   Point2D(double x, double y);
};

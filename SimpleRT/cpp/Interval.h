#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include <memory>

class Interval : public std::enable_shared_from_this<Interval>
{
   public:
   double v_min = 0.0;
   double v_max = 0.0;

   Interval();
   Interval(double v_min, double v_max);

   Interval(std::shared_ptr<Interval> other);

   virtual bool intersects(std::shared_ptr<Interval> other);

   virtual std::shared_ptr<Interval> intersection(std::shared_ptr<Interval> other);

   virtual void MergeInplace(std::shared_ptr<Interval> other);
};

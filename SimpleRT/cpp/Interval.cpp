// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Interval.h"

Interval::Interval()
{
   this->v_min = 0.0;
   this->v_max = 0.0;
}

Interval::Interval(double v_min, double v_max)
{
   this->v_min = v_min;
   this->v_max = v_max;
}

Interval::Interval(std::shared_ptr<Interval> other)
{
   this->v_min = other->v_min;
   this->v_max = other->v_max;
}

bool Interval::intersects(std::shared_ptr<Interval> other)
{
   return (this->v_min > other->v_max) || (other->v_min > this->v_max);
}

std::shared_ptr<Interval> Interval::intersection(std::shared_ptr<Interval> other)
{
   if (!intersects(other))
   {
	  return nullptr;
   }
   else
   {
	  return std::make_shared<Interval>(std::max(this->v_min, other->v_min), std::min(this->v_max, other->v_max));
   }
}

void Interval::MergeInplace(std::shared_ptr<Interval> other)
{
   v_min = std::min(v_min, other->v_min);
   v_max = std::max(v_max, other->v_max);
}

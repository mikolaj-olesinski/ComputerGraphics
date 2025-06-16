#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Interval.h"
#include "_Interval.h"
#include "RTRay.h"
#include <vector>
#include <memory>

class BoundingVolume : public std::enable_shared_from_this<BoundingVolume>
{
   public:
   static inline double EPSILON = 1.0e-6;

   static inline long long total_ints = 0;

   std::shared_ptr<Interval> intv_x;
   std::shared_ptr<Interval> intv_y;
   std::shared_ptr<Interval> intv_z;
   private:
   std::vector<std::shared_ptr<Interval>> intv;

   public:
   BoundingVolume();

   virtual double GetSize(int axis);

   virtual double GetMin(int axis);

   virtual double GetMax(int axis);

   BoundingVolume(std::shared_ptr<BoundingVolume> other);

   static void MakeFromTriangles();
   BoundingVolume(double xbbb_min, double xbbb_max, double ybbb_min, double ybbb_max, double zbbb_min, double zbbb_max);

   virtual void PrapareForMerge();

   virtual void MergeInplace(std::shared_ptr<BoundingVolume> bv);

   virtual void AddMargin();

   virtual double SVH();

   virtual bool IsIntersected(std::shared_ptr<RTRay> ray, std::shared_ptr<Interval> ray_t);

   virtual bool IsIntersectedUnrolled(std::shared_ptr<RTRay> ray, std::shared_ptr<Interval> ray_t);
};

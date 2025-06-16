#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "MatAttr.h"
#include "RGBColor.h"
#include "BoundingVolume.h"
#include "_BoundingVolume.h"
#include "Point3D.h"
#include "RTRay.h"
#include "Vector3D.h"
#include "RGBColorFloat.h"
#include <memory>

class Shape3D : public std::enable_shared_from_this<Shape3D>
{
   public:
   int mat_index = 0;
   int part_index = 0;
   std::shared_ptr<MatAttr> material;
   std::shared_ptr<RGBColor> native_color;
   std::shared_ptr<BoundingVolume> bv;
   std::shared_ptr<Point3D> center;
   virtual bool IsIntersected(std::shared_ptr<RTRay> ray) = 0;
   virtual void GetNormal(std::shared_ptr<Point3D> p, std::shared_ptr<Vector3D> N) = 0;

   virtual void UpdateAttenuation(std::shared_ptr<RGBColorFloat> attenuation);

   virtual double GetCenterCoord(int axis);
};

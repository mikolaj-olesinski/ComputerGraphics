#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Point3D.h"
#include "Vector3D.h"
#include <vector>
#include <cmath>
#include <memory>

class RTRay : public std::enable_shared_from_this<RTRay>
{
   public:
   std::shared_ptr<Point3D> P; // ray begin
   std::shared_ptr<Vector3D> u; // ray direction
   double t_min = 0.0;
   double t_max = 0.0;

   double t_int = 0.0;
   bool is_inside = false;
   bool is_diffused = false;
   std::shared_ptr<Point3D> int_point;
   std::vector<double> ad_inv;
   std::vector<double> P_array;

   RTRay();

   virtual void Update();

   virtual void RayInit(std::shared_ptr<Point3D> P, std::shared_ptr<Vector3D> u);

   virtual void InitDistances();

   virtual void ReflectedDir(std::shared_ptr<Vector3D> N, std::shared_ptr<Vector3D> refl);

   static void LambertianDir(std::shared_ptr<Vector3D> N, std::shared_ptr<Vector3D> diffused);

   virtual bool RefractedDir(std::shared_ptr<Vector3D> N, std::shared_ptr<Vector3D> refr, double eta);
};

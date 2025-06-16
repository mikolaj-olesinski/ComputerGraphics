#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Point3D.h"
#include "Shape3D.h"
#include "Light.h"
#include "MatAttr.h"
#include "RTObserver.h"
#include "RGBColorFloat.h"
#include "BVHNode.h"
#include "RGBColor.h"
#include "RTRay.h"
#include "Vector3D.h"
#include <string>
#include <vector>
#include <iostream>
#include <cmath>
#include <memory>

using Color = java::awt::Color;
using BufferedImage = java::awt::image::BufferedImage;
using ArrayList = java::util::ArrayList;


class World : public std::enable_shared_from_this<World>
{
   // Scene description
   public:
   std::vector<std::shared_ptr<Point3D>> vertices;
   std::vector<std::vector<int>> adj_triangles;
   std::vector<std::shared_ptr<Shape3D>> shapes;
   std::vector<std::shared_ptr<Light>> lights;
   std::vector<int> parts;
   std::vector<std::shared_ptr<MatAttr>> materials;
   std::vector<std::shared_ptr<RTObserver>> cams;
   std::shared_ptr<RTObserver> camera;
   double ambient_light = 0.0;
   std::shared_ptr<Color> bckg_color;
   std::shared_ptr<RGBColorFloat> bckg_color_rgb = std::make_shared<RGBColorFloat>();
   int supersamples = 0;
   // Temporary data
   private:
   std::shared_ptr<Point3D> int_point = std::make_shared<Point3D>();
   std::shared_ptr<RGBColorFloat> attenuation = std::make_shared<RGBColorFloat>();
   std::vector<std::shared_ptr<RGBColorFloat>> ray_colors;

   static inline int MIN_SUPERSAMPLES = 10;
   static inline double COLOR_SMALL_FRACT = 0.2;

   // Statistics
   long long prim_cnt = 0;
   long long refr_cnt = 0;
   long long refl_cnt = 0;
   long long diffused_cnt = 0;
   long long int_refl_cnt = 0;
   std::shared_ptr<BVHNode> bvh = nullptr;
   public:
   bool use_BVH = true;
   bool use_stratified_supersampling = false;

   virtual double LoadFromFile(const std::wstring &fname, std::shared_ptr<RGBColor> b_color, double amb_light);

   private:
   bool ContinueSupersampling(std::vector<std::shared_ptr<RGBColorFloat>> &samples_color, int samples_cnt);

   public:
   virtual std::shared_ptr<BufferedImage> RayTrace();

   private:
   std::shared_ptr<Shape3D> FindClosestIntersection(std::shared_ptr<RTRay> ray, std::shared_ptr<Shape3D> excluded);

   std::shared_ptr<Shape3D> FindClosestIntersectionBasic(std::shared_ptr<RTRay> ray, std::shared_ptr<Shape3D> excluded);


   bool FindLightAttenuation(std::shared_ptr<RTRay> ray, std::shared_ptr<RGBColorFloat> attenuation, std::shared_ptr<Shape3D> excluded);

   bool FindLightAttenuationBasic(std::shared_ptr<RTRay> ray, std::shared_ptr<RGBColorFloat> attenuation, std::shared_ptr<Shape3D> excluded);

   std::shared_ptr<Shape3D> RayColor(std::shared_ptr<RTRay> ray, int level, std::shared_ptr<Shape3D> origin_face);

   /**
    * @param trg_index
    */
   public:
   virtual void AddAvgNormals(int trg_index);

   virtual std::shared_ptr<Vector3D> FindAveragedNormal(std::shared_ptr<Vector3D> nnormal, int vrt_index);

   virtual void SetSupersampling(int supersampling);
};

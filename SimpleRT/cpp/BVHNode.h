#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "BoundingVolume.h"
#include "_BoundingVolume.h"
#include "Shape3D.h"
#include "Interval.h"
#include "_Interval.h"
#include "Point3D.h"
#include "RTRay.h"
#include "RGBColorFloat.h"
#include <string>
#include <vector>
#include <algorithm>
#include <iostream>
#include <memory>

class BVHNode : public std::enable_shared_from_this<BVHNode>
{
   public:
   std::shared_ptr<BoundingVolume> bv;
   std::shared_ptr<BVHNode> next_l, next_h;
   std::vector<std::shared_ptr<Shape3D>> shapes;
   int trg_cnt = 0;

   private:
   int axis = 0;

   // Following constants established experimentally
   public:
   static inline int MIN_NODE_OCCUPATION = 3; // 5
   static inline int BINS_CNT = 100;
   private:
   static inline double INTERSECT_T = 1.0;
   static inline double TRAVERSAL_T = 0.6; // 0.25

   public:
   BVHNode();

   private:
   static inline std::shared_ptr<Interval> bb_interval = std::make_shared<Interval>();

   public:
   static std::shared_ptr<BVHNode> CreateTree(std::vector<std::shared_ptr<Shape3D>> &members, int first, int last);
   static std::vector<std::shared_ptr<Interval>> FindSpans(std::vector<std::shared_ptr<Shape3D>> &members, int first, int last);

   static std::shared_ptr<BoundingVolume> MakeBB(std::vector<std::shared_ptr<Shape3D>> &members, int first, int last);

   private:
   static std::vector<std::shared_ptr<Shape3D>> SortShapesByAxis(std::vector<std::shared_ptr<Shape3D>> &members, int first, int last, int axis);

   private:
   class ComparatorAnonymousInnerClass : public std::enable_shared_from_this<ComparatorAnonymousInnerClass>, public Comparator<std::shared_ptr<Shape3D>>
   {
   public:
	   int compare(std::shared_ptr<Shape3D> p1, std::shared_ptr<Shape3D> p2);
   };

   private:
   class ComparatorAnonymousInnerClass2 : public std::enable_shared_from_this<ComparatorAnonymousInnerClass2>, public Comparator<std::shared_ptr<Shape3D>>
   {
   public:
	   int compare(std::shared_ptr<Shape3D> p1, std::shared_ptr<Shape3D> p2);
   };

   private:
   class ComparatorAnonymousInnerClass3 : public std::enable_shared_from_this<ComparatorAnonymousInnerClass3>, public Comparator<std::shared_ptr<Shape3D>>
   {
   public:
	   int compare(std::shared_ptr<Shape3D> p1, std::shared_ptr<Shape3D> p2);
   };

   private:
   static double ComputeCost(std::shared_ptr<BoundingVolume> bv1, std::shared_ptr<BoundingVolume> bv2, std::shared_ptr<BoundingVolume> bv12, int bv1_cnt, int bv2_cnt);

   public:
   static std::shared_ptr<BVHNode> CreateTreeSAH(std::vector<std::shared_ptr<Shape3D>> &members, int first, int last);

   static inline int total_trgs = 0;
   static inline int total_nodes = 0;
   static inline int leave_nodes = 0;

   virtual void DisplayTree(int level);

   virtual std::shared_ptr<Shape3D> FindClosestIntersection(std::shared_ptr<RTRay> ray, std::shared_ptr<Shape3D> excluded, std::shared_ptr<Point3D> int_point);

   virtual bool FindLightAttenuation(std::shared_ptr<RTRay> ray, std::shared_ptr<RGBColorFloat> attenuation, std::shared_ptr<Shape3D> excluded);
};

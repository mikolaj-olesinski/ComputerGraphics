// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "BVHNode.h"
#include "RTUtils.h"
#include "Triangle.h"

using Arrays = java::util::Arrays;
using Comparator = java::util::Comparator;
using ArrayList = java::util::ArrayList;

BVHNode::BVHNode()
{
   next_l = nullptr;
   next_h = nullptr;
   shapes = std::vector<std::shared_ptr<Shape3D>>();
   trg_cnt = 0;
   bv = std::make_shared<BoundingVolume>();
}

std::shared_ptr<BVHNode> BVHNode::CreateTree(std::vector<std::shared_ptr<Shape3D>> &members, int first, int last)
{
   // System.out.println( "In create Tree " + first + " " + last + " " + members.length );
   if (members.empty())
   {
	  return nullptr;
   }

   std::shared_ptr<BVHNode> node = std::make_shared<BVHNode>();
   total_nodes++;
   node->bv = MakeBB(members, first, last);
   node->trg_cnt = last - first + 1;

   if (node->trg_cnt <= MIN_NODE_OCCUPATION)
   {
	  int len = last - first + 1;
	  node->shapes = std::vector<std::shared_ptr<Shape3D>>(len);
	  total_trgs += len;
	  for (int i = 0; i < len; i++)
	  {
		 node->shapes[i] = members[i + first];
	  }
	  leave_nodes++;
	  return node;
   }

   // Split is required

   // Find the longest axis of bv
   std::vector<std::shared_ptr<Interval>> spans = FindSpans(members, first, last);
   int axis = 0;
   double max_span = 0;
   for (int i = 0; i < 3; i++)
   {
	  double span = spans[i]->v_max - spans[i]->v_min;
	  if (span > max_span)
	  {
		 max_span = span;
		 axis = i;
	  }
   }

   std::vector<std::shared_ptr<Shape3D>> sorted = SortShapesByAxis(members, first, last, axis);
   int sorted_len = sorted.size();
   int half = sorted_len / 2;

   // System.out.println( "   Building left " + (half - 0 + 1) ); 
   node->next_l = CreateTree(sorted, 0, half);
   // System.out.println( "   Building right " + ((sorted_len - 1) - half+1 + 1)); 
   node->next_h = CreateTree(sorted, half + 1, sorted_len - 1);

   return node;
}

std::vector<std::shared_ptr<Interval>> BVHNode::FindSpans(std::vector<std::shared_ptr<Shape3D>> &members, int first, int last)
{
   if (last < first)
   {
	  return std::vector<std::shared_ptr<Interval>>();
   }

   if ((first == 0) && (last == 0))
   {
	  last = members.size() - 1;
   }

   std::vector<std::shared_ptr<Interval>> spans(3);
   spans[0] = std::make_shared<Interval>();
   spans[1] = std::make_shared<Interval>();
   spans[2] = std::make_shared<Interval>();

   spans[0]->v_min = members[first]->center.x;
   spans[0]->v_max = members[first]->center.x;
   spans[1]->v_min = members[first]->center.y;
   spans[1]->v_max = members[first]->center.y;
   spans[2]->v_min = members[first]->center.z;
   spans[2]->v_max = members[first]->center.z;

   for (int i = first + 1; i <= last; i++)
   {
	  spans[0]->v_min = std::min(spans[0]->v_min, members[i]->center.x);
	  spans[0]->v_max = std::max(spans[0]->v_max, members[i]->center.x);
	  spans[1]->v_min = std::min(spans[1]->v_min, members[i]->center.y);
	  spans[1]->v_max = std::max(spans[1]->v_max, members[i]->center.y);
	  spans[2]->v_min = std::min(spans[2]->v_min, members[i]->center.z);
	  spans[2]->v_max = std::max(spans[2]->v_max, members[i]->center.z);
   }

   return spans;
}

std::shared_ptr<BoundingVolume> BVHNode::MakeBB(std::vector<std::shared_ptr<Shape3D>> &members, int first, int last)
{
   if (last < first)
   {
	  return nullptr;
   }

   if ((first == 0) && (last == 0))
   {
	  last = members.size() - 1;
   }

   std::shared_ptr<BoundingVolume> bv = std::make_shared<BoundingVolume>(members[first]->bv);

   for (int i = first + 1; i <= last; i++)
   {
	  bv->MergeInplace(members[i]->bv);
   }

   bv->AddMargin();

   return bv;
}

std::vector<std::shared_ptr<Shape3D>> BVHNode::SortShapesByAxis(std::vector<std::shared_ptr<Shape3D>> &members, int first, int last, int axis)
{
   if (last < first)
   {
	  return std::vector<std::shared_ptr<Shape3D>>();
   }

   if ((first == 0) && (last == 0))
   {
	  last = members.size() - 1;
   }

   std::vector<std::shared_ptr<Shape3D>> sorted(last - first + 1);

   std::copy_n(members.begin() + first, last - first + 1, sorted.begin());

   switch (axis)
   {
	  case 0:
		 Arrays::sort(sorted, std::make_shared<ComparatorAnonymousInnerClass>());
		 break;
	  case 1:
		 Arrays::sort(sorted, std::make_shared<ComparatorAnonymousInnerClass2>());
		 break;
	  case 2:
		 Arrays::sort(sorted, std::make_shared<ComparatorAnonymousInnerClass3>());
		 break;
   }
   return sorted;
}

int BVHNode::ComparatorAnonymousInnerClass::compare(std::shared_ptr<Shape3D> p1, std::shared_ptr<Shape3D> p2)
{
	return Double::compare(p1->center->x, p2->center->x);
}

int BVHNode::ComparatorAnonymousInnerClass2::compare(std::shared_ptr<Shape3D> p1, std::shared_ptr<Shape3D> p2)
{
	return Double::compare(p1->center->y, p2->center->y);
}

int BVHNode::ComparatorAnonymousInnerClass3::compare(std::shared_ptr<Shape3D> p1, std::shared_ptr<Shape3D> p2)
{
	return Double::compare(p1->center->z, p2->center->z);
}

double BVHNode::ComputeCost(std::shared_ptr<BoundingVolume> bv1, std::shared_ptr<BoundingVolume> bv2, std::shared_ptr<BoundingVolume> bv12, int bv1_cnt, int bv2_cnt)
{
   return 2 * TRAVERSAL_T + (INTERSECT_T / bv12->SVH()) * (bv1_cnt * bv1->SVH() + bv2_cnt * bv2->SVH());
}

std::shared_ptr<BVHNode> BVHNode::CreateTreeSAH(std::vector<std::shared_ptr<Shape3D>> &members, int first, int last)
{
   // System.out.println( "In create Tree " + first + " " + last + " " + members.length );
   if (members.empty())
   {
	  return nullptr;
   }

   std::shared_ptr<BVHNode> node = std::make_shared<BVHNode>();
   total_nodes++;
   node->bv = MakeBB(members, first, last);
   node->trg_cnt = last - first + 1;

   if (node->trg_cnt <= MIN_NODE_OCCUPATION)
   {
	  int len = last - first + 1;
	  node->shapes = std::vector<std::shared_ptr<Shape3D>>(len);
	  total_trgs += len;
	  for (int i = 0; i < len; i++)
	  {
		 node->shapes[i] = members[i + first];
	  }
	  leave_nodes++;
	  return node;
   }

   // Split is required

   std::vector<double> bin_bounds(BINS_CNT + 1);
   std::shared_ptr<BoundingVolume> tmp_bv = std::make_shared<BoundingVolume>();
   int best_axis = -1;
   int best_l_index = -1;
   double best_cost = RTUtils::INFINITY;
   std::vector<std::vector<std::shared_ptr<Shape3D>>> sorted(3);
   int shapes_cnt = -1;

   // Try x axes
   for (int axis = 0; axis < 3; axis++)
   {
	  int l_shp_index = 0;

	  sorted[axis] = SortShapesByAxis(members, first, last, axis);
	  shapes_cnt = sorted[axis].length;

	  double size = node->bv->GetSize(axis);
	  size = sorted[axis][shapes_cnt - 1]->GetCenterCoord(axis) - sorted[axis][0]->GetCenterCoord(axis);

	  double left_bound = node->bv->GetMin(axis);
	  left_bound = sorted[axis][0]->GetCenterCoord(axis);
	  double increment = size / BINS_CNT;

	  for (int bin_index = 0; bin_index <= BINS_CNT; bin_index++)
	  {
		 bin_bounds[bin_index] = left_bound + bin_index * increment;
	  }
	  bin_bounds[BINS_CNT] += 0.01;
	  bin_bounds[0] -= 0.01;

	  double h_bound = bin_bounds[1];
	  int previous_l_shp_index = 0;

	  tmp_bv->PrapareForMerge();

	  for (int bin_index = 0; bin_index < BINS_CNT; bin_index++)
	  {
		 h_bound = bin_bounds[bin_index + 1];

		 while ((l_shp_index < shapes_cnt) && (sorted[axis][l_shp_index]->GetCenterCoord(axis) <= h_bound))
		 {
			tmp_bv->MergeInplace(sorted[axis][l_shp_index]->bv);
			l_shp_index++;
		 }

		 if (l_shp_index >= shapes_cnt)
		 {
			// This is the last bin - all shapes assigned to left subset 
			// further processing does not make sense
			break;
		 }

		 if (l_shp_index == previous_l_shp_index)
		 {
			// no change in L/H sets - continue
			continue;
		 }

		 previous_l_shp_index = l_shp_index;

		 std::shared_ptr<BoundingVolume> right_bv = MakeBB(sorted[axis], l_shp_index, sorted[axis].length - 1);

		 double cost = ComputeCost(tmp_bv, right_bv, node->bv, l_shp_index, shapes_cnt - l_shp_index);
		 if (cost < best_cost)
		 {
			best_cost = cost;
			best_axis = axis;
			best_l_index = l_shp_index - 1;
		 }
	  }
   }

   if (best_axis == -1)
   {
	  // We were not able to find split - find the longest axis of bv

	  // Find the longest axis of bv
	  std::vector<std::shared_ptr<Interval>> spans = FindSpans(members, first, last);
	  double max_span = 0;
	  for (int i = 0; i < 3; i++)
	  {
		 double span = spans[i]->v_max - spans[i]->v_min;
		 if (span > max_span)
		 {
			max_span = span;
			best_axis = i;
		 }
	  }

	  best_l_index = shapes_cnt / 2;
   }

   if (best_cost > INTERSECT_T * node->trg_cnt)
   {
		 int len = last - first + 1;
		 node->shapes = std::vector<std::shared_ptr<Shape3D>>(len);
		 total_trgs += len;
		 for (int i = 0; i < len; i++)
		 {
			node->shapes[i] = members[i + first];
		 }
		 leave_nodes++;
		 return node;
   }

   // System.out.println( "   Building left " + (best_l_index + 1) ); 
   node->next_l = CreateTreeSAH(sorted[best_axis], 0, best_l_index);
   // System.out.println( "   Building right " + ((shapes_cnt - 1) - best_l_index+1 + 1)); 
   node->next_h = CreateTreeSAH(sorted[best_axis], best_l_index + 1, shapes_cnt - 1);

   return node;
}

void BVHNode::DisplayTree(int level)
{
   if (!shapes.empty())
   {
	  total_trgs += shapes.size();
   }

   std::wstring indent = L"";
   std::wstring msg = L"";
   for (int i = 0; i < level; i++)
   {
	  indent += L"   ";
   }
   indent = indent + std::to_wstring(level) + L" Trg# " + std::to_wstring(trg_cnt) + L" Span x:" + std::to_wstring(bv->intv_x->v_min) + L" - " + std::to_wstring(bv->intv_x->v_max) + L"   " + std::to_wstring(bv->intv_y->v_min) + L" - " + std::to_wstring(bv->intv_y->v_max) + L"   " + std::to_wstring(bv->intv_z->v_min) + L" - " + std::to_wstring(bv->intv_z->v_max) + L"   ";
   std::wcout << indent << std::endl;
   if (next_l != nullptr)
   {
	  next_l->DisplayTree(level + 1);
   }
   if (next_h != nullptr)
   {
	  next_h->DisplayTree(level + 1);
   }
}

std::shared_ptr<Shape3D> BVHNode::FindClosestIntersection(std::shared_ptr<RTRay> ray, std::shared_ptr<Shape3D> excluded, std::shared_ptr<Point3D> int_point)
{
   if (!bv->IsIntersected(ray, bb_interval))
   {
	  return nullptr;
   }

   if (!shapes.empty())
   {
	  // This is leave node
	  int intersected_shp_ind = -1;
	  int shp_count = shapes.size();

	  for (int i = 0; i < shp_count; i++)
	  {
		 // Do not test excluded face
		 if (shapes[i] == excluded)
		 {
			continue;
		 }
		 if (shapes[i]->IsIntersected(ray))
		 {
			int_point->x = Triangle::int_point->x;
			int_point->y = Triangle::int_point->y;
			int_point->z = Triangle::int_point->z;

			intersected_shp_ind = i;
		 }
	  }
	  if (intersected_shp_ind == -1)
	  {
		 return nullptr;
	  }
	  else
	  {
		 return shapes[intersected_shp_ind];
	  }
   }

   std::shared_ptr<Shape3D> l_shape = nullptr;
   std::shared_ptr<Shape3D> h_shape = nullptr;

   if (next_l != nullptr)
   {
	  l_shape = next_l->FindClosestIntersection(ray, excluded, int_point);
   }
   if (next_h != nullptr)
   {
	  h_shape = next_h->FindClosestIntersection(ray, excluded, int_point);
   }

   if (h_shape != nullptr)
   {
	  return h_shape;
   }
   else
   {
	  return l_shape;
   }
}

bool BVHNode::FindLightAttenuation(std::shared_ptr<RTRay> ray, std::shared_ptr<RGBColorFloat> attenuation, std::shared_ptr<Shape3D> excluded)
{
   if (!bv->IsIntersected(ray, bb_interval))
   {
	  return true;
   }

   if (!shapes.empty())
   {
	  int shp_count = shapes.size();
	  attenuation->r = attenuation->g = attenuation->b = static_cast<float>(1.0);

	  for (int i = 0; i < shp_count; i++)
	  {
		 // Do not test excluded face
		 if (shapes[i] == excluded)
		 {
			continue;
		 }

		 if (shapes[i]->IsIntersected(ray))
		 {
			shapes[i]->UpdateAttenuation(attenuation);
			if ((attenuation->r < RTUtils::EPS) && (attenuation->g < RTUtils::EPS) && (attenuation->b < RTUtils::EPS))
			{
			   return false;
			}
			// This is to avoid rejection of more distant transparent objects
			ray->t_int = RTUtils::INFINITY;
		 }
	  }

	  return true;
   }

   bool l_transfer = true;
   bool h_transfer = true;

   if (next_l != nullptr)
   {
	  l_transfer = next_l->FindLightAttenuation(ray, attenuation, excluded);
   }
   if (!l_transfer)
   {
	  return false;
   }

   if (next_h != nullptr)
   {
	  h_transfer = next_h->FindLightAttenuation(ray, attenuation, excluded);
   }

   return h_transfer;

}

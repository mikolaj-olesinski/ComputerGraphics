// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "_BoundingVolume.h"

void BoundingVolume::MakeFromTriangles()
{
	std::shared_ptr<BoundingVolume> bv = std::make_shared<BoundingVolume>();

}

BoundingVolume::BoundingVolume(double xbbb_min, double xbbb_max, double ybbb_min, double ybbb_max, double zbbb_min, double zbbb_max)
{
   intv_x = std::make_shared<Interval>(xbbb_min, xbbb_max);
   intv_y = std::make_shared<Interval>(ybbb_min, ybbb_max);
   intv_z = std::make_shared<Interval>(zbbb_min, zbbb_max);
}

void BoundingVolume::MergeInplace(std::shared_ptr<BoundingVolume> bv)
{
   intv_x->MergeInplace(bv->intv_x);
   intv_x->MergeInplace(bv->intv_x);
   intv_x->MergeInplace(bv->intv_x);
}

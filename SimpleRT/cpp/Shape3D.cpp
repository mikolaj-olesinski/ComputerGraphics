// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Shape3D.h"

void Shape3D::UpdateAttenuation(std::shared_ptr<RGBColorFloat> attenuation)
{
   attenuation->r *= material->ktCr;
   attenuation->g *= material->ktCg;
   attenuation->b *= material->ktCb;
}

double Shape3D::GetCenterCoord(int axis)
{
   switch (axis)
   {
	  case 0 :
		  return center->x;
	  case 1 :
		  return center->y;
	  case 2 :
		  return center->z;
   }
   return 0.0;
}

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "RGBColor.h"

RGBColor::RGBColor()
{
   this->R = 0;
   this->G = 0;
   this->B = 0;
}

RGBColor::RGBColor(int R, int G, int B)
{
   this->R = R;
   this->G = G;
   this->B = B;
}

void RGBColor::CopyTo(std::shared_ptr<RGBColor> c)
{
   c->R = R;
   c->G = G;
   c->B = B;
}

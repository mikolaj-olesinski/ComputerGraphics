// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "RGBColorFloat.h"

using Scanner = java::util::Scanner;

RGBColorFloat::RGBColorFloat(float i, float j, float k)
{
   r = i;
   g = j;
   b = k;
}

RGBColorFloat::RGBColorFloat()
{
   r = g = b = 0.0f;
}

void RGBColorFloat::ReadFromFile(std::shared_ptr<Scanner> s)
{
   r = s->nextFloat();
   g = s->nextFloat();
   b = s->nextFloat();
}

void RGBColorFloat::CopyTo(std::shared_ptr<RGBColorFloat> c)
{
   c->r = r;
   c->g = g;
   c->b = b;
}

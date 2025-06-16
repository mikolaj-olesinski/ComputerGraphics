#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include <memory>

using Scanner = java::util::Scanner;

class RGBColorFloat : public std::enable_shared_from_this<RGBColorFloat>
{
   public:
   float r = 0.0F, g = 0.0F, b = 0.0F;
   RGBColorFloat(float i, float j, float k);
   RGBColorFloat();

   virtual void ReadFromFile(std::shared_ptr<Scanner> s);
   virtual void CopyTo(std::shared_ptr<RGBColorFloat> c);
};

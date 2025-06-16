#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include <memory>

class RGBColor : public std::enable_shared_from_this<RGBColor>
{
   public:
   int R = 0, G = 0, B = 0;
   int as_int = 0;

   RGBColor();

   RGBColor(int R, int G, int B);

   virtual void CopyTo(std::shared_ptr<RGBColor> c);



};

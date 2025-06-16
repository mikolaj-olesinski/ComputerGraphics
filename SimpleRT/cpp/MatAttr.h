#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include <string>
#include <memory>

using Scanner = java::util::Scanner;


class MatAttr : public std::enable_shared_from_this<MatAttr>
{
   public:
   std::wstring name;

   double R = 0.0, G = 0.0, B = 0.0;
   double kdCr = 0.0, kdCg = 0.0, kdCb = 0.0;
   double ksCr = 0.0, ksCg = 0.0, ksCb = 0.0;
   double ktCr = 0.0, ktCg = 0.0, ktCb = 0.0;
   double kaCr = 0.0, kaCg = 0.0, kaCb = 0.0;

   double kdIr = 0.0, kdIg = 0.0, kdIb = 0.0;
   double ksIr = 0.0, ksIg = 0.0, ksIb = 0.0;
   double ktIr = 0.0, ktIg = 0.0, ktIb = 0.0;
   double kaIr = 0.0, kaIg = 0.0, kaIb = 0.0;

   bool is_transparent = false;
   bool is_specular_refl = false;
   bool is_diffused = false;

   int g = 0;
   double eta = 0.0;

   virtual void ReadFromFile(std::shared_ptr<Scanner> s);
};

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "MatAttr.h"

using Scanner = java::util::Scanner;

void MatAttr::ReadFromFile(std::shared_ptr<Scanner> s)
{
   s->next();
   name = s->next();

   s->next();
   R = s->nextDouble();
   G = s->nextDouble();
   B = s->nextDouble();

   s->next();
   kdCr = s->nextDouble();
   s->next();
   kdCg = s->nextDouble();
   s->next();
   kdCb = s->nextDouble();

   s->next();
   ksCr = s->nextDouble();
   s->next();
   ksCg = s->nextDouble();
   s->next();
   ksCb = s->nextDouble();

   s->next();
   ktCr = s->nextDouble();
   s->next();
   ktCg = s->nextDouble();
   s->next();
   ktCb = s->nextDouble();

   s->next();
   kaCr = s->nextDouble();
   s->next();
   kaCg = s->nextDouble();
   s->next();
   kaCb = s->nextDouble();

   s->next();
   g = s->nextInt();
   s->next();
   eta = s->nextDouble();

   kdIr = kdCr * R;
   kdIg = kdCg * G;
   kdIb = kdCb * B;

   // ksIr = ksCr*R; ksIg = ksCg*G; ksIb = ksCb*B;
   ksIr = ksCr;
   ksIg = ksCg;
   ksIb = ksCb;
   is_specular_refl = ((ksIr > 0) || (ksIg > 0) || (ksIb > 0));

   kaIr = kaCr * R;
   kaIg = kaCg * G;
   kaIb = kaCb * B;
   ktIr = ktCr * R;
   ktIg = ktCg * G;
   ktIb = ktCb * B;
   is_transparent = ((ktIr > 0) || (ktIg > 0) || (ktIb > 0));
   is_diffused = ((kdIr > 0) || (kdIg > 0) || (kdIb > 0));

}

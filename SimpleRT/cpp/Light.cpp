// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Light.h"

using Scanner = java::util::Scanner;

void Light::ReadFromFile(std::shared_ptr<Scanner> s)
{
   // Skip cam_name keyword
   s->next();
   name = s->next();

   // Enabled switch
   s->next();
   enabled = (s->nextInt() == 1);

   // Light type
   s->next();
   std::wstring stg = s->next();

   if (stg == L"point")
   {
	  type = LightType::POINT_LT;
   }
   else
   {
	  type = LightType::CONE_LT;
   }

   s->next();
   rgb = std::make_shared<RGBColorFloat>();
   rgb->ReadFromFile(s);

   s->next();
   E = s->nextDouble();

   s->next();
   position = std::make_shared<Point3D>();
   position->ReadFromFile(s);

   s->next();
   dir = std::make_shared<Vector3D>();
   dir->ReadFromFile(s);

   s->next();
   inner_angle = s->nextDouble();
   s->next();
   outer_angle = s->nextDouble();

   // Read gonio diagram
   s->next();
   int gonio_cnt = s->nextInt();

// JAVA TO C++ CONVERTER NOTE: The following call to the 'RectangularVectors' helper class reproduces the rectangular array initialization that is automatic in Java:
// ORIGINAL LINE: gonio = new double[gonio_cnt][2];
   gonio = RectangularVectors::rectangularDoubleVector(gonio_cnt, 2);
   for (int i = 0; i < gonio_cnt; i++)
   {
	  gonio[i][0] = s->nextDouble();
	  gonio[i][1] = s->nextDouble();
   }
}

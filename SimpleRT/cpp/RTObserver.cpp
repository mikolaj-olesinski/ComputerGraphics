// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "RTObserver.h"

using Scanner = java::util::Scanner;

void RTObserver::ReadFromFile(std::shared_ptr<Scanner> s)
{
   // Skip cam_name keyword
   s->next();
   name = s->next();

   // Get resolution
   s->next();
   x_res = s->nextInt();
   y_res = s->nextInt();

   // Get eye position
   s->next();
   O = std::make_shared<Point3D>();
   O->ReadFromFile(s);
   s->next();
   T = std::make_shared<Point3D>();
   T->ReadFromFile(s);

   // Get field of view
   s->next();
   fov = s->nextDouble();

   s->next();
   alpha = s->nextDouble();

   RasterData();
}

void RTObserver::RasterData()
{
   // Vertical and horizontal half vectors
   std::shared_ptr<Vector3D> vert_v;
   std::shared_ptr<Vector3D> hor_v;

   // Find aspect ratio: w/h
   w_h = x_res / static_cast<double>(y_res);
   x_invert = 1.0 / x_res;
   y_invert = 1.0 / y_res;

   // Find view direction
   std::shared_ptr<Vector3D> u = std::make_shared<Vector3D>(T, O); // u = T - O;; 0 ----> T
   double len = u->GetLength();

   // Define normalized verticaL vector
   std::shared_ptr<Vector3D> vn = std::make_shared<Vector3D>(0.0, 1.0, 0.0);

   // h - normalized horizontal vector
   hor_v = u->CrossProduct(vn);
   hor_v->Normalize();

   // Find the vertical vector perpendicular to u nad h
   vert_v = u->CrossProduct(hor_v);
   vert_v->Normalize();

   // Inverse vertical normalized if necessary
   if (vert_v->DotProduct(vn) < 0)
   {
	  vert_v->ScalarMult(-1.0);
   }

   // Find half vectors of window edges
   double hl = len * std::tan(std::numbers::pi * fov / 360.0);
   //  w_h = hl/vl
   double vl = hl / w_h;

   P_UL = std::make_shared<Point3D>(T);
   hor_v->ScalarMult(hl);
   vert_v->ScalarMult(vl);

   P_UL->Sub(hor_v);
   P_UL->Add(vert_v);

   P_LL = std::make_shared<Point3D>(T);
   P_LL->Sub(hor_v);
   P_LL->Sub(vert_v);

   P_UR = std::make_shared<Point3D>(T);
   P_UR->Add(hor_v);
   P_UR->Add(vert_v);

   h_edge = std::make_shared<Vector3D>(P_UR, P_UL);
   v_edge = std::make_shared<Vector3D>(P_LL, P_UL);

   P_UL->Show(L"P_UL: ");
   P_LL->Show(L"P_LL: ");
   P_UR->Show(L"P_UR: ");
}

void RTObserver::SetRayParam(double x, double y, std::shared_ptr<RTRay> ray)
{
   ray->P->x = O->x;
   ray->P->y = O->y;
   ray->P->z = O->z;

   double x_fract = x * x_invert;
   double y_fract = y * y_invert;

   double x_pix = P_UL->x + x_fract * h_edge->x + y_fract * v_edge->x;
   double y_pix = P_UL->y + x_fract * h_edge->y + y_fract * v_edge->y;
   double z_pix = P_UL->z + x_fract * h_edge->z + y_fract * v_edge->z;

   ray->u->x = x_pix - O->x;
   ray->u->y = y_pix - O->y;
   ray->u->z = z_pix - O->z;

   ray->u->Normalize();

   ray->InitDistances();
}

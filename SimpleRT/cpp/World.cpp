// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "World.h"
#include "RTUtils.h"
#include "Triangle.h"
#include "RTException.h"
#include "BoundingVolume.h"
#include "_BoundingVolume.h"

using Color = java::awt::Color;
using BufferedImage = java::awt::image::BufferedImage;
using BufferedReader = java::io::BufferedReader;
using FileReader = java::io::FileReader;
using ArrayList = java::util::ArrayList;
using Locale = java::util::Locale;
using Scanner = java::util::Scanner;
using IOException = java::io::IOException;

double World::LoadFromFile(const std::wstring &fname, std::shared_ptr<RGBColor> b_color, double amb_light)
{
   std::wcout << L"Loading scene data from file: " << fname << std::endl;

   bckg_color_rgb->r = static_cast<float>(b_color->R / 255.0);
   bckg_color_rgb->g = static_cast<float>(b_color->G / 255.0);
   bckg_color_rgb->b = static_cast<float>(b_color->B / 255.0);

   ambient_light = amb_light;

   ray_colors = std::vector<std::shared_ptr<RGBColorFloat>>(RTUtils::MAX_RAY_TREE_DEPTH);
   for (int i = 0; i < RTUtils::MAX_RAY_TREE_DEPTH; i++)
   {
	  ray_colors[i] = std::make_shared<RGBColorFloat>(0.0f, 0.0f, 0.0f);
   }

   std::wstring tmp_fname = fname + L".tmp";

   RTUtils::RemoveComments(fname, tmp_fname);

   std::shared_ptr<Scanner> s = nullptr;
   try
   {
	   s = std::make_shared<Scanner>(std::make_shared<BufferedReader>(std::make_shared<FileReader>(tmp_fname)));
	   s->useLocale(Locale::US);

	   // Read vertices
	   // skip keyword
	   s->next();
	   int vert_count = s->nextInt();
	   vertices = std::vector<std::shared_ptr<Point3D>>(vert_count);
	   for (int i = 0; i < vert_count; i++)
	   {
		  vertices[i] = std::make_shared<Point3D>();
		  vertices[i]->x = s->nextDouble();
		  vertices[i]->y = s->nextDouble();
		  vertices[i]->z = s->nextDouble();
	   }

	   // Read triangles
	   // skip keyword
	   s->next();
	   int trg_count = s->nextInt();

	   // Create data for computing averaged normals
	   adj_triangles = std::vector<std::vector>(trg_count);
	   for (int i = 0; i < vert_count; i++)
	   {
		  adj_triangles[i] = std::vector<int>();
	   }

	   shapes = std::vector<std::shared_ptr<Shape3D>>(trg_count);
	   for (int i = 0; i < trg_count; i++)
	   {
		  std::shared_ptr<Triangle> trg = std::make_shared<Triangle>();
		  trg->ReadFromFile(s, vertices);
		  adj_triangles[trg->i1].push_back(i);
		  adj_triangles[trg->i2].push_back(i);
		  adj_triangles[trg->i3].push_back(i);
		  shapes[i] = trg;
	   }

	   // Create parts array
	   // skip keyword
	   s->next();
	   int part_count = s->nextInt();
	   parts = std::vector<int>(part_count);
	   for (int i = 0; i < part_count; i++)
	   {
		  parts[i] = -1;
	   }

	   // Read part indices of triangles          
	   for (int i = 0; i < trg_count; i++)
	   {
		   shapes[i]->part_index = s->nextInt();
	   }

	   // Read materials
	   // skip keyword
	   s->next();
	   int mat_count = s->nextInt();
	   materials = std::vector<std::shared_ptr<MatAttr>>(mat_count);
	   for (int i = 0; i < mat_count; i++)
	   {
		  std::shared_ptr<MatAttr> mat = std::make_shared<MatAttr>();
		  mat->ReadFromFile(s);
		  materials[i] = mat;
	   }

	   // Connect materials and parts
	   for (int i = 0; i < part_count; i++)
	   {
		  s->nextInt();
		  std::wstring mat_name = s->next();

		  // Find material index
		  int j;
		  for (j = 0; j < mat_count; j++)
		  {
			 if (materials[j]->name == mat_name)
			 {
				break;
			 }
		  }
		  if (j >= mat_count)
		  {
			 throw RTException(L"Cannot find material for a part");
		  }

		  parts[i] = j;
	   }

	   // Update shapes with material references
	   int shape_cnt = shapes.size();
	   for (int i = 0; i < shape_cnt; i++)
	   {
		  shapes[i]->material = materials[parts[shapes[i]->part_index]];
	   }

	   // Load lights
	   s->next();
	   int lights_count = s->nextInt();
	   lights = std::vector<std::shared_ptr<Light>>(lights_count);
	   for (int i = 0; i < lights_count; i++)
	   {
		  std::shared_ptr<Light> l = std::make_shared<Light>();
		  l->ReadFromFile(s);
		  lights[i] = l;
	   }


	   // Load camera data
	   s->next();
	   int cam_count = s->nextInt();
	   cams = std::vector<std::shared_ptr<RTObserver>>(cam_count);
	   s->next();
	   int active_cam_index = s->nextInt();

	   for (int i = 0; i < cam_count; i++)
	   {
		  std::shared_ptr<RTObserver> observer = std::make_shared<RTObserver>();
		  observer->ReadFromFile(s);
		  cams[i] = observer;
		  if (i == active_cam_index)
		  {
			 camera = observer;
		  }
	   }

	   bckg_color = RTUtils::DEF_BCKG_COLOR;

	   // Build averaged normal for triangles
	   for (int i = 0; i < trg_count; i++)
	   {
		  AddAvgNormals(i);
	   }

	   std::wcout << L"Scene data loaded." << std::endl;
   }
// JAVA TO C++ CONVERTER TASK: There is no C++ equivalent to the exception 'finally' clause:
   finally
   {
	   s->close();
   }

   // Get start time
   long long s_time = System::currentTimeMillis();
   bvh = BVHNode::CreateTreeSAH(shapes, 0, shapes.size() - 1);
   // bvh.DisplayTree(0);
   long long e_time = System::currentTimeMillis();
   double ltime = (e_time - s_time) * 0.001;

   std::wcout << L"BVH building time: " << ltime << L" sec." << std::endl;
   std::wcout << L"BVH statistics:" << std::endl;
   std::wcout << L"   TRGS:   " << BVHNode::total_trgs << std::endl;
   std::wcout << L"   NODES:  " << BVHNode::total_nodes << std::endl;
   std::wcout << L"   LEAVES: " << BVHNode::leave_nodes << std::endl;

   return ltime;
}

bool World::ContinueSupersampling(std::vector<std::shared_ptr<RGBColorFloat>> &samples_color, int samples_cnt)
{
   double r_avg, r_max, r_min;
   double g_avg, g_max, g_min;
   double b_avg, b_max, b_min;

   r_avg = r_max = r_min = samples_color[0]->r;
   g_avg = g_max = g_min = samples_color[0]->g;
   b_avg = b_max = b_min = samples_color[0]->b;

   for (int j = 1; j < samples_cnt; j++)
   {
	  r_avg += samples_color[j]->r;
	  if (samples_color[j]->r > r_max)
	  {
		 r_max = samples_color[j]->r;
	  }
	  if (samples_color[j]->r < r_min)
	  {
		 r_min = samples_color[j]->r;
	  }

	  g_avg += samples_color[j]->g;
	  if (samples_color[j]->g > g_max)
	  {
		 g_max = samples_color[j]->g;
	  }
	  if (samples_color[j]->g < g_min)
	  {
		 g_min = samples_color[j]->g;
	  }

	  b_avg += samples_color[j]->b;
	  if (samples_color[j]->b > b_max)
	  {
		 b_max = samples_color[j]->b;
	  }
	  if (samples_color[j]->b < b_min)
	  {
		 b_min = samples_color[j]->b;
	  }
   }

   r_avg /= samples_cnt;
   g_avg /= samples_cnt;
   b_avg /= samples_cnt;

   if ((r_max - r_min) > COLOR_SMALL_FRACT * r_avg)
   {
	   return true;
   }
   if ((g_max - g_min) > COLOR_SMALL_FRACT * g_avg)
   {
	   return true;
   }
   if ((b_max - b_min) > COLOR_SMALL_FRACT * b_avg)
   {
	   return true;
   }

   return false;
}

std::shared_ptr<BufferedImage> World::RayTrace()
{
   std::shared_ptr<BufferedImage> img;
   std::shared_ptr<RTRay> primary_ray;
   int x_res, y_res;
   int bckg_color_int = bckg_color->getRGB();
   int shape_color_int = (std::make_shared<Color>(0,0,0))->getRGB();
   int supersamples_sqr = supersamples * supersamples;
   double x_ray;
   double y_ray;
   double recip_ss = 1.0 / (supersamples + 1);

   // Request the resolution of the active camera
   x_res = camera->x_res;
   y_res = camera->y_res;

   prim_cnt = refr_cnt = refl_cnt = int_refl_cnt = 0;

   std::wcout << L"Ray tracing initialized ..." << std::endl;

   // Get start time
   long long s_time = System::currentTimeMillis();

   // Create the output image
   img = std::make_shared<BufferedImage>(x_res, y_res, BufferedImage::TYPE_INT_RGB);
   primary_ray = std::make_shared<RTRay>();
   std::shared_ptr<RGBColorFloat> ray_color = std::make_shared<RGBColorFloat>();

   std::vector<std::shared_ptr<RGBColorFloat>> samples_color(supersamples + 1);
   for (int i = 0; i <= supersamples; i++)
   {
	  samples_color[i] = std::make_shared<RGBColorFloat>();
   }

   for (int y = 0; y < y_res; y++)
   {
	  for (int x = 0; x < x_res; x++)
	  {
		 int samples_cnt = 0;

		 // img.setRGB( x, y, bckg_color_int );
		 prim_cnt++;
		 camera->SetRayParam(x, y, primary_ray);
		 primary_ray->Update();

		 std::shared_ptr<Shape3D> int_shape = RayColor(primary_ray, 0, nullptr);

		 ray_color->r = samples_color[samples_cnt]->r = ray_colors[0]->r;
		 ray_color->g = samples_color[samples_cnt]->g = ray_colors[0]->g;
		 ray_color->b = samples_color[samples_cnt]->b = ray_colors[0]->b;
		 samples_cnt++;

		 if (supersamples > 0)
		 {
			for (int y_d = 0; y_d < supersamples; y_d++)
			{
			   for (int x_d = 0; x_d < supersamples; x_d++)
			   {
				  prim_cnt++;
				  if (use_stratified_supersampling)
				  {
					 x_ray = x + (Math::random() + x_d) * recip_ss;
					 y_ray = y + (Math::random() + y_d) * recip_ss;
				  }
				  else
				  {
					 x_ray = x + Math::random() - 0.5;
					 y_ray = y + Math::random() - 0.5;
				  }
				  camera->SetRayParam(x_ray, y_ray, primary_ray);
				  primary_ray->Update();
				  int_shape = RayColor(primary_ray, 0, nullptr);

				  ray_color->r += ray_colors[0]->r;
				  ray_color->g += ray_colors[0]->g;
				  ray_color->b += ray_colors[0]->b;

				  samples_cnt++;
			   }
			}
		 }

		 double gamma = 0.5;
		 ray_colors[0]->r = static_cast<float>(std::pow(ray_color->r / samples_cnt, gamma));
		 ray_colors[0]->g = static_cast<float>(std::pow(ray_color->g / samples_cnt, gamma));
		 ray_colors[0]->b = static_cast<float>(std::pow(ray_color->b / samples_cnt, gamma));
		 ray_colors[0]->r *= 255;
		 ray_colors[0]->g *= 255;
		 ray_colors[0]->b *= 255;
		 int pixel_color = RTUtils::byte2RGB(static_cast<int>(ray_colors[0]->r), static_cast<int>(ray_colors[0]->g), static_cast<int>(ray_colors[0]->b));
		 img->setRGB(x, y, pixel_color);


		 /*
		 int int_shape_ind = FindClosestIntersection( primary_ray );
		 if ( int_shape_ind >= 0 )
		    img.setRGB( x, y, shapes[int_shape_ind].native_color.as_int );
		 else
		    img.setRGB( x, y, bckg_color_int );
		 */            

	  }
	  if (y % 10 == 0)
	  {
		 std::wcout << L"\rRow " << y << L" completed";
	  }
   }

   std::wcout << L"" << std::endl;

   // Get start time
   long long e_time = System::currentTimeMillis();
   double rttime = (e_time - s_time) * 0.001;

   std::wcout << L"Rendering time: " << rttime << L" sec." << std::endl;
   std::wcout << L"Triangles count: " << shapes.size() << std::endl;

   std::wcout << L"Prmary rays: " << prim_cnt << std::endl;
   std::wcout << L"Reflected rays: " << refl_cnt << std::endl;
   std::wcout << L"Refracted rays: " << refr_cnt << std::endl;
   std::wcout << L"Internal refl:  " << int_refl_cnt << std::endl;
   std::wcout << L"Diffused rays:  " << diffused_cnt << std::endl;

   std::wcout << L"Intersection test count: " << Triangle::total_ints * 1.0e-6 << L" mlns" << std::endl;
   std::wcout << L"M intersections/sec:  " << (Triangle::total_ints * 1.0e-6) / rttime << std::endl;
   std::wcout << L"BV test count:        " << BoundingVolume::total_ints * 1.0e-6 << L" mlns" << std::endl;
   std::wcout << L"M BV tests/sec:       " << (BoundingVolume::total_ints * 1.0e-6) / rttime << std::endl;
   std::wcout << L"Avg samples per pixel " << (prim_cnt / (x_res * y_res)) << std::endl;
   return img;
}

std::shared_ptr<Shape3D> World::FindClosestIntersection(std::shared_ptr<RTRay> ray, std::shared_ptr<Shape3D> excluded)
{
   if (use_BVH)
   {
	  return bvh->FindClosestIntersection(ray, excluded, int_point);
   }
   else
   {
	  return FindClosestIntersectionBasic(ray, excluded);
   }
}

std::shared_ptr<Shape3D> World::FindClosestIntersectionBasic(std::shared_ptr<RTRay> ray, std::shared_ptr<Shape3D> excluded)
{
   int intersected_shp_ind = -1;
   int shp_count = shapes.size();

   for (int i = 0; i < shp_count; i++)
   {
	  // Do not test excluded face
	  if (shapes[i] == excluded)
	  {
		 continue;
	  }
	  if (shapes[i]->IsIntersected(ray))
	  {
		 int_point->x = Triangle::int_point->x;
		 int_point->y = Triangle::int_point->y;
		 int_point->z = Triangle::int_point->z;

		 intersected_shp_ind = i;
	  }
   }

   if (intersected_shp_ind == -1)
   {
	  return nullptr;
   }
   else
   {
	  return shapes[intersected_shp_ind];
   }
}

bool World::FindLightAttenuation(std::shared_ptr<RTRay> ray, std::shared_ptr<RGBColorFloat> attenuation, std::shared_ptr<Shape3D> excluded)
{
   if (use_BVH)
   {
	  return bvh->FindLightAttenuation(ray, attenuation, excluded);
   }
   else
   {
	  return FindLightAttenuationBasic(ray, attenuation, excluded);
   }
}

bool World::FindLightAttenuationBasic(std::shared_ptr<RTRay> ray, std::shared_ptr<RGBColorFloat> attenuation, std::shared_ptr<Shape3D> excluded)
{
   int shp_count = shapes.size();
   attenuation->r = attenuation->g = attenuation->b = static_cast<float>(1.0);
   ray->t_min = RTUtils::EPS;

   for (int i = 0; i < shp_count; i++)
   {
	  // Do not test excluded face
	  if (shapes[i] == excluded)
	  {
		 continue;
	  }

	  if (shapes[i]->IsIntersected(ray))
	  {
		 int mat_index = parts[shapes[i]->part_index];
		 attenuation->r *= materials[mat_index]->ktCr;
		 attenuation->g *= materials[mat_index]->ktCg;
		 attenuation->b *= materials[mat_index]->ktCb;
		 if ((attenuation->r < RTUtils::EPS) && (attenuation->g < RTUtils::EPS) && (attenuation->b < RTUtils::EPS))
		 {
			return false;
		 }
		 ray->t_int = RTUtils::INFINITY;
	  }
   }

   return true;
}

std::shared_ptr<Shape3D> World::RayColor(std::shared_ptr<RTRay> ray, int level, std::shared_ptr<Shape3D> origin_face)
{
   double rf, gf, bf;
   double dist_to_light;
   std::shared_ptr<Shape3D> int_object = FindClosestIntersection(ray, origin_face);

   if (int_object == nullptr)
   {
	  bckg_color_rgb->CopyTo(ray_colors[level]);
	  return nullptr;
   }

   std::shared_ptr<Vector3D> n = std::make_shared<Vector3D>();
   std::shared_ptr<RTRay> shadow_ray = std::make_shared<RTRay>();
   std::shared_ptr<Vector3D> glossy_dir = std::make_shared<Vector3D>();

   std::shared_ptr<Point3D> int_point = std::make_shared<Point3D>();

   int_point->x = this->int_point->x;
   int_point->y = this->int_point->y;
   int_point->z = this->int_point->z;

   rf = gf = bf = 0.0;

   int mat_index = parts[int_object->part_index];

   // Get normal oriented towards observer - u is from the observer to the 
   // observed fragments      
   int_object->GetNormal(int_point, n);
   if (n->DotProduct(ray->u) > 0)
   {
	  n->Reverse();
   }

   std::shared_ptr<Point3D> shadow_ray_origin;

   shadow_ray_origin = int_point;
   if (std::dynamic_pointer_cast<Triangle>(int_object) != nullptr)
   {
	  std::shared_ptr<Triangle> trg = std::static_pointer_cast<Triangle>(int_object);
	  if (trg->use_interpolated_normals)
	  {
		 shadow_ray_origin = trg->DisplaceIntPoint(int_point, ray->P, vertices);
	  }
   }

   // Object hit - compute diffused light
   for (int l = 0; l < lights.size(); l++)
   {
	  // Test shadowing
	  shadow_ray->P->x = shadow_ray_origin->x;
	  shadow_ray->P->y = shadow_ray_origin->y;
	  shadow_ray->P->z = shadow_ray_origin->z;

	  //int_point.CopyTo( shadow_ray.P );
	  shadow_ray->u->FromPoints(lights[l]->position, int_point);

	  // if the light on the opposite side than observer - skip the light, 
	  // the only exception is the transparent-difuse surface which can be illuminated from
	  // both sides
	  if ((n->DotProduct(shadow_ray->u) <= 0.0) && !materials[mat_index]->is_transparent)
	  {
		 continue;
	  }

	  shadow_ray->t_max = dist_to_light = shadow_ray->u->GetLength();
	  shadow_ray->t_min = RTUtils::EPS;
	  shadow_ray->t_int = RTUtils::INFINITY;
	  shadow_ray->u->Normalize();
	  shadow_ray->Update();

	  attenuation->r = attenuation->g = attenuation->b = 1.0f;
	  if (FindLightAttenuation(shadow_ray, attenuation, int_object))
	  {
		 double dist_clipped = std::max(0.3, dist_to_light);

		 // Light arrives to the observed point
		 double dist_att = lights[l]->E * std::abs(shadow_ray->u->DotProduct(n)) / dist_clipped;
		 rf += dist_att * attenuation->r * lights[l]->rgb.r * materials[mat_index]->kdIr;
		 gf += dist_att * attenuation->g * lights[l]->rgb.g * materials[mat_index]->kdIg;
		 bf += dist_att * attenuation->b * lights[l]->rgb.b * materials[mat_index]->kdIb;

		 if (materials[mat_index]->is_specular_refl)
		 {
			ray->ReflectedDir(n, glossy_dir);
			double g_factor = shadow_ray->u->DotProduct(glossy_dir);
			if (g_factor < 0)
			{
			   g_factor = 0.0;
			}

			dist_att = lights[l]->E * std::pow(g_factor, materials[mat_index]->g) / dist_clipped;

			rf += dist_att * attenuation->r * lights[l]->rgb.r * materials[mat_index]->ksIr;
			gf += dist_att * attenuation->g * lights[l]->rgb.g * materials[mat_index]->ksIg;
			bf += dist_att * attenuation->b * lights[l]->rgb.b * materials[mat_index]->ksIb;
		 }
	  }
   }


   // Sample diffused light

   if ((materials[mat_index]->is_diffused) && (level < RTUtils::MAX_RAY_TREE_DEPTH - 1) && (!ray->is_diffused))
   {
	  std::shared_ptr<RTRay> diffused = std::make_shared<RTRay>();

	  // Create reflected ray
	  int_point->CopyTo(diffused->P);
	  RTRay::LambertianDir(n, diffused->u);
	  diffused->is_inside = ray->is_inside;
	  diffused->is_diffused = true;
	  diffused->Update();
	  diffused_cnt++;

	  RayColor(diffused, level + 1, int_object);

	  rf += static_cast<float>(ray_colors[level + 1]->r * materials[mat_index]->kdIr);
	  gf += static_cast<float>(ray_colors[level + 1]->g * materials[mat_index]->kdIg);
	  bf += static_cast<float>(ray_colors[level + 1]->b * materials[mat_index]->kdIb);
   }

   // Add ambient light
   rf += ambient_light * materials[mat_index]->kaIr;
   gf += ambient_light * materials[mat_index]->kaIg;
   bf += ambient_light * materials[mat_index]->kaIb;


   if ((materials[mat_index]->is_specular_refl) && (level < RTUtils::MAX_RAY_TREE_DEPTH - 1) && !ray->is_diffused && !ray->is_inside)
   {
	  std::shared_ptr<RTRay> reflected = std::make_shared<RTRay>();

	  // Create reflected ray
	  int_point->CopyTo(reflected->P);
	  ray->ReflectedDir(n, reflected->u);
	  reflected->is_inside = ray->is_inside;
	  reflected->Update();

	  RayColor(reflected, level + 1, int_object);
	  refl_cnt++;

	  // Use Schlick approximation of Fresnel law
	  double R;
	  double m = std::pow(1.0 - std::abs(ray->u->DotProduct(n)), 5.0);

	  R = materials[mat_index]->ksIr + (1.0 - materials[mat_index]->ksIr) * m;
	  if (R > 4 * materials[mat_index]->ksIr)
	  {
		 R = 4 * materials[mat_index]->ksIr;
	  }
	  rf += static_cast<float>(ray_colors[level + 1]->r * R);

	  R = materials[mat_index]->ksIg + (1.0 - materials[mat_index]->ksIg) * m;
	  if (R > 4 * materials[mat_index]->ksIg)
	  {
		 R = 4 * materials[mat_index]->ksIg;
	  }
	  gf += static_cast<float>(ray_colors[level + 1]->g * R);

	  R = materials[mat_index]->ksIb + (1.0 - materials[mat_index]->ksIb) * m;
	  if (R > 4 * materials[mat_index]->ksIb)
	  {
		 R = 4 * materials[mat_index]->ksIb;
	  }
	  bf += static_cast<float>(ray_colors[level + 1]->b * R);

	  /*
	  rf += (float)(ray_colors[level+1].r * materials[mat_index].ksIr);  
	  gf += (float)(ray_colors[level+1].g * materials[mat_index].ksIg);
	  bf += (float)(ray_colors[level+1].b * materials[mat_index].ksIb);
	  */

   }

   if ((materials[mat_index]->is_transparent) && (level < RTUtils::MAX_RAY_TREE_DEPTH - 1) && !ray->is_diffused)
   {
	  double eta;

	  std::shared_ptr<RTRay> refracted = std::make_shared<RTRay>();

	  // Create refracted ray
	  int_point->CopyTo(refracted->P);
	  if (ray->is_inside)
	  {
		 eta = materials[mat_index]->eta;
	  }
	  else
	  {
		 eta = 1.0 / materials[mat_index]->eta;
	  }

	  if (ray->RefractedDir(n, refracted->u, eta))
	  {
		 // Proceed only in case there is no total internal reflection
		 refracted->is_inside = !ray->is_inside;
		 refracted->t_min = RTUtils::EPS;
		 refr_cnt++;
		 refracted->Update();
		 RayColor(refracted, level + 1, int_object);

		 // Use transmitance attenuation
		 rf += static_cast<float>(ray_colors[level + 1]->r * materials[mat_index]->ktIr);
		 gf += static_cast<float>(ray_colors[level + 1]->g * materials[mat_index]->ktIg);
		 bf += static_cast<float>(ray_colors[level + 1]->b * materials[mat_index]->ktIb);
	  }
	  else
	  {
		 // Total internal reflection
		 int_point->CopyTo(refracted->P);
		 ray->ReflectedDir(n, refracted->u);
		 refracted->is_inside = ray->is_inside;
		 refracted->t_min = RTUtils::EPS;
		 int_refl_cnt++;
		 refracted->Update();
		 RayColor(refracted, level + 1, int_object);

		 // Compute the loss of energy in the media boundary using Frenel's principle
		 double absorbtion = (eta - 1.0) / (eta + 1.0);
		 absorbtion *= absorbtion;
		 double transmitance = 1.0 - absorbtion;

		 // Use Schlick approximation of Fresnel law
		 double R;
		 double m = std::pow(1.0 - std::abs(ray->u->DotProduct(n)), 5.0);

		 R = transmitance + (1.0 - transmitance) * m;
		 if (R > 1.0)
		 {
			R = 1.0;
		 }

		 // R = materials[mat_index].ksIr + (1.0 -  materials[mat_index].ksIr)*m;
		 rf += static_cast<float>(ray_colors[level + 1]->r * R);

		 // R = materials[mat_index].ksIg + (1.0 -  materials[mat_index].ksIg)*m;
		 gf += static_cast<float>(ray_colors[level + 1]->g * R);

		 // R = materials[mat_index].ksIb + (1.0 -  materials[mat_index].ksIb)*m;
		 bf += static_cast<float>(ray_colors[level + 1]->b * R);
	  }
   }

   // Pass finally computed color
   ray_colors[level]->r = static_cast<float>(rf); //1.0f;
   ray_colors[level]->g = static_cast<float>(gf); //0.0f;
   ray_colors[level]->b = static_cast<float>(bf); //0.0f;

   return int_object;
}

void World::AddAvgNormals(int trg_index)
{
   std::shared_ptr<Vector3D> normal, nnormal;

   // Do it only for triangles
   if (!(std::dynamic_pointer_cast<Triangle>(shapes[trg_index]) != nullptr))
   {
	  return;
   }

   std::shared_ptr<Triangle> trg = std::static_pointer_cast<Triangle>(shapes[trg_index]);
   nnormal = std::make_shared<Vector3D>();

   // Get triangle geometric normal
   trg->GetNativeNormal(nnormal);

   // Find average normals for this triangle for each its vertex
   normal = FindAveragedNormal(nnormal, trg->i1);
   trg->AddAvgNormal(normal, 1);
   normal = FindAveragedNormal(nnormal, trg->i2);
   trg->AddAvgNormal(normal, 2);
   normal = FindAveragedNormal(nnormal, trg->i3);
   trg->AddAvgNormal(normal, 3);

   // Orient vertex normals uniformly
   trg->CorrectAvgNormals();

   // Do other things related to vertex interpolation
   trg->PrepareNormalInterpolationData(vertices);
}

std::shared_ptr<Vector3D> World::FindAveragedNormal(std::shared_ptr<Vector3D> nnormal, int vrt_index)
{
   double x = 0;
   double y = 0;
   double z = 0;

   for (int i = 0; i < adj_triangles[vrt_index].size(); i++)
   {
	  std::shared_ptr<Shape3D> shp = shapes[adj_triangles[vrt_index][i]];
	  if (!(std::dynamic_pointer_cast<Triangle>(shp) != nullptr))
	  {
		 continue;
	  }
	  std::shared_ptr<Triangle> trg = std::static_pointer_cast<Triangle>(shp);

	  std::shared_ptr<Vector3D> other_normal = std::make_shared<Vector3D>();
	  trg->GetNativeNormal(other_normal);

	  // Test if this normal close enough to the native one
	  double cos_a = nnormal->DotProduct(other_normal);
	  if (std::abs(cos_a) < RTUtils::COS_25)
	  {
		 continue;
	  }

	  if (cos_a > 0.0)
	  {
		 x += other_normal->x;
		 y += other_normal->y;
		 z += other_normal->z;
	  }
	  else
	  {
		 x -= other_normal->x;
		 y -= other_normal->y;
		 z -= other_normal->z;
	  }
   }

   std::shared_ptr<Vector3D> avg_norm = std::make_shared<Vector3D>();
   avg_norm->x = x;
   avg_norm->y = y;
   avg_norm->z = z;
   avg_norm->Normalize();

   return avg_norm;
}

void World::SetSupersampling(int supersampling)
{
   supersamples = supersampling;
}

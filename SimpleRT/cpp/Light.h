#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "RGBColorFloat.h"
#include "Point3D.h"
#include "Vector3D.h"
#include <string>
#include <vector>
#include <stdexcept>
#include <utility>
#include <memory>
#include "tangible_rectangular_vectors.h"

using Scanner = java::util::Scanner;


class Light : public std::enable_shared_from_this<Light>
{
   public:
   enum class LightType
   {
	   POINT_LT,
	   CONE_LT

   };

   class LightTypeHelper
   {
   private:
	   static std::vector<std::pair<LightType, std::wstring>> pairs()
	   {
		   return
		   {
			   {LightType::POINT_LT, L"POINT_LT"},
			   {LightType::CONE_LT, L"CONE_LT"}
		   };
	   }

   public:
	   static std::vector<LightType> values()
	   {
		   std::vector<LightType> temp;
		   for (auto pair : pairs())
		   {
			   temp.push_back(pair.first);
		   }
		   return temp;
	   }

	   static std::wstring enumName(LightType value)
	   {
		   for (auto pair : pairs())
		   {
			   if (pair.first == value)
				   return pair.second;
		   }

		   throw std::runtime_error("Enum not found.");
	   }

	   static int ordinal(LightType value)
	   {
		   std::vector<std::pair<LightType, std::wstring>> temp = pairs();
		   for (std::size_t i = 0; i < temp.size(); i++)
		   {
			   if (temp[i].first == value)
				   return i;
		   }

		   throw std::runtime_error("Enum not found.");
	   }

	   static LightType enumFromString(std::wstring value)
	   {
		   for (auto pair : pairs())
		   {
			   if (pair.second == value)
				   return pair.first;
		   }

		   throw std::runtime_error("Enum not found.");
	   }
   };

   public:
   std::wstring name;
   bool enabled = false;
   LightType type = static_cast<LightType>(0);
   std::shared_ptr<RGBColorFloat> rgb;
   double E = 0.0;
   std::shared_ptr<Point3D> position;
   std::shared_ptr<Vector3D> dir;
   double inner_angle = 0.0;
   double outer_angle = 0.0;
   std::vector<std::vector<double>> gonio;

   virtual void ReadFromFile(std::shared_ptr<Scanner> s);
};

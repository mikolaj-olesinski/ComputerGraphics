#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include <string>
#include <vector>
#include <stdexcept>
#include <utility>
#include <memory>

using Color = java::awt::Color;
using IOException = java::io::IOException;


class RTUtils : public std::enable_shared_from_this<RTUtils>
{

   public:
   enum class DRIVING_PLANES
   {
	   EXCLUDE_X,
	   EXCLUDE_Y,
	   EXCLUDE_Z

   };

   class DRIVING_PLANESHelper
   {
   private:
	   static std::vector<std::pair<DRIVING_PLANES, std::wstring>> pairs()
	   {
		   return
		   {
			   {DRIVING_PLANES::EXCLUDE_X, L"EXCLUDE_X"},
			   {DRIVING_PLANES::EXCLUDE_Y, L"EXCLUDE_Y"},
			   {DRIVING_PLANES::EXCLUDE_Z, L"EXCLUDE_Z"}
		   };
	   }

   public:
	   static std::vector<DRIVING_PLANES> values()
	   {
		   std::vector<DRIVING_PLANES> temp;
		   for (auto pair : pairs())
		   {
			   temp.push_back(pair.first);
		   }
		   return temp;
	   }

	   static std::wstring enumName(DRIVING_PLANES value)
	   {
		   for (auto pair : pairs())
		   {
			   if (pair.first == value)
				   return pair.second;
		   }

		   throw std::runtime_error("Enum not found.");
	   }

	   static int ordinal(DRIVING_PLANES value)
	   {
		   std::vector<std::pair<DRIVING_PLANES, std::wstring>> temp = pairs();
		   for (std::size_t i = 0; i < temp.size(); i++)
		   {
			   if (temp[i].first == value)
				   return i;
		   }

		   throw std::runtime_error("Enum not found.");
	   }

	   static DRIVING_PLANES enumFromString(std::wstring value)
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
   static constexpr double EPS = 1.0E-10;
   static constexpr double INFINITY = 1.0E+30;
   static inline const std::shared_ptr<Color> DEF_BCKG_COLOR = std::make_shared<Color>(0, 0, 160);
   static constexpr int MAX_RAY_TREE_DEPTH = 20;

   static constexpr int DEF_BCOL_RED = 0;
   static constexpr int DEF_BCOL_GREEN = 0;
   static constexpr int DEF_BCOL_BLUE = 150;
   static constexpr double COS_25 = 0.906;

   static void RemoveComments(const std::wstring &fname, const std::wstring &tmp_fname);

   static int byte2RGB(int red, int green, int blue);

};

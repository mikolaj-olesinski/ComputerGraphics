#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Light.h"
#include <memory>

class PointLight : public Light
{


protected:
	std::shared_ptr<PointLight> shared_from_this()
	{
		return std::static_pointer_cast<PointLight>(Light::shared_from_this());
	}
};

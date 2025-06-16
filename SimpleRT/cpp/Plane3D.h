#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Vector3D.h"
#include "Point3D.h"
#include <memory>

class Plane3D : public std::enable_shared_from_this<Plane3D>
{
   // Plane equation: NP + d = 0
public:
	std::shared_ptr<Vector3D> N;
	double d = 0.0;

	virtual void CreateFromVertices(std::shared_ptr<Point3D> v1, std::shared_ptr<Point3D> v2, std::shared_ptr<Point3D> v3);

};

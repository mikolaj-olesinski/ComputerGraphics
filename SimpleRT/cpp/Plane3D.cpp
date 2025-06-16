// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Plane3D.h"

void Plane3D::CreateFromVertices(std::shared_ptr<Point3D> v1, std::shared_ptr<Point3D> v2, std::shared_ptr<Point3D> v3)
{
	std::shared_ptr<Vector3D> w1, w2;

	w1 = std::make_shared<Vector3D>(v2, v1);
	w2 = std::make_shared<Vector3D>(v3, v1);

	N = w1->CrossProduct(w2);
	N->Normalize();

	d = - N->DotProduct(v1);
}

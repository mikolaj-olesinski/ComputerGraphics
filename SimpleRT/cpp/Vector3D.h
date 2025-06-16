#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Point3D.h"
#include <cmath>
#include <memory>

using Scanner = java::util::Scanner;
using Random = java::util::Random;

class Vector3D : public std::enable_shared_from_this<Vector3D>
{
public:
	double x = 0.0, y = 0.0, z = 0.0;

private:
	static inline std::shared_ptr<Random> rand = std::make_shared<Random>();

public:
	Vector3D();
	Vector3D(double x, double y, double z);

	Vector3D(std::shared_ptr<Point3D> v2, std::shared_ptr<Point3D> v1);

	static std::shared_ptr<Vector3D> Sub(std::shared_ptr<Vector3D> v2, std::shared_ptr<Vector3D> v1);

	static std::shared_ptr<Vector3D> Sub(std::shared_ptr<Point3D> v2, std::shared_ptr<Point3D> v1);

	virtual void Normalize();

	virtual double DotProduct(std::shared_ptr<Vector3D> v);

	virtual double DotProduct(std::shared_ptr<Point3D> v);

	virtual std::shared_ptr<Vector3D> CrossProduct(std::shared_ptr<Vector3D> v);

	virtual std::shared_ptr<Vector3D> CrossProduct(std::shared_ptr<Vector3D> out, std::shared_ptr<Vector3D> v);

	virtual double GetLength();

	virtual void ScalarMult(double t);

	virtual void ReadFromFile(std::shared_ptr<Scanner> s);

	virtual void CopyTo(std::shared_ptr<Vector3D> v);

	virtual void FromPoints(std::shared_ptr<Point3D> v2, std::shared_ptr<Point3D> v1);

   virtual void Reverse();

   virtual void Interpolate(std::shared_ptr<Vector3D> vA, std::shared_ptr<Vector3D> vB, double a);

   virtual double Length();

   virtual double LengthSqr();

   virtual void Random(double min, double max);
};

#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Point2D.h"
#include "RTUtils.h"
#include "Vector3D.h"
#include <string>
#include <iostream>
#include <memory>

using Scanner = java::util::Scanner;


class Point3D : public std::enable_shared_from_this<Point3D>
{
public:
	double x = 0.0, y = 0.0, z = 0.0;

	Point3D();

	Point3D(std::shared_ptr<Point3D> p);

	virtual void CopyTo(std::shared_ptr<Point3D> p);

	Point3D(double x, double y, double z);

	virtual void ReadFromFile(std::shared_ptr<Scanner> s);

	virtual void ConvertTo2D(RTUtils::DRIVING_PLANES driving_plane, std::shared_ptr<Point2D> p);

	virtual std::shared_ptr<Point2D> ConvertTo2D(RTUtils::DRIVING_PLANES driving_plane);

	virtual void Add(std::shared_ptr<Vector3D> v);

	virtual void Sub(std::shared_ptr<Vector3D> v);

	static void Sub(std::shared_ptr<Point3D> res, std::shared_ptr<Point3D> p1, std::shared_ptr<Point3D> p2);

	virtual void Show(const std::wstring &s);

	virtual double DotProduct(std::shared_ptr<Vector3D> v);

	virtual void CrossProduct(std::shared_ptr<Vector3D> out, std::shared_ptr<Vector3D> v);

};

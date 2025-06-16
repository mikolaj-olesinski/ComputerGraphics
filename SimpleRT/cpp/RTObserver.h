#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Point3D.h"
#include "Vector3D.h"
#include "RTRay.h"
#include <string>
#include <numbers>
#include <cmath>
#include <memory>

using Scanner = java::util::Scanner;

class RTObserver : public std::enable_shared_from_this<RTObserver>
{
 public:
	 std::wstring name;

	 std::shared_ptr<Point3D> O; // Eye position in scene coordinates
	 std::shared_ptr<Point3D> T; // Target point
	 std::shared_ptr<Vector3D> u; // Looking directions

	 double fov = 0.0; // Field of view angle in degrees
	 double alpha = 0.0; // Rotation angle with recpect to vertical Y axis
	 double w_h = 0.0; // Aspect ratio: w/h

	 int x_res = 0, y_res = 0; // image resolution
	 double x_invert = 0.0; // 1 / x_res
	 double y_invert = 0.0; // 1 / y_res

	 std::shared_ptr<Point3D> P_UL; // upper left screen parallelogram vertex
	 std::shared_ptr<Point3D> P_UR; // upper right screen parallelogram vertex
	 std::shared_ptr<Point3D> P_LL; // lower left screen parallelogram vertex

	 // Screen window edges in 3D scene coords
	 //
	 //                    h_edge
	 //      P_UL -------------------------->  P_UR
	 //       |
	 //       |   v_edge
	 //      \|/
	 //      P_LL
	 //
	 std::shared_ptr<Vector3D> h_edge;
	 std::shared_ptr<Vector3D> v_edge;

	 virtual void ReadFromFile(std::shared_ptr<Scanner> s);

 private:
	 void RasterData();

 public:
	 virtual void SetRayParam(double x, double y, std::shared_ptr<RTRay> ray);
};

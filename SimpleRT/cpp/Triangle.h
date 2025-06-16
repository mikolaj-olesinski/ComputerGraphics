#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "Shape3D.h"
#include "Plane3D.h"
#include "RTUtils.h"
#include "Line2D.h"
#include "Point3D.h"
#include "Point2D.h"
#include "Vector3D.h"
#include "RTRay.h"
#include <vector>
#include <cmath>
#include <memory>

using Scanner = java::util::Scanner;

class Triangle : public Shape3D
{
public:
	int i1 = 0, i2 = 0, i3 = 0;
	std::shared_ptr<Plane3D> plane;
	RTUtils::DRIVING_PLANES driving_plane = static_cast<RTUtils::DRIVING_PLANES>(0); // excluded coordinate: x-1, y-2, z-3
	std::shared_ptr<Line2D> l12;
	std::shared_ptr<Line2D> l13;
	std::shared_ptr<Line2D> l23;
	static inline std::shared_ptr<Point3D> int_point = std::make_shared<Point3D>();
private:
	static inline std::shared_ptr<Point2D> int_point_2D = std::make_shared<Point2D>(0.0, 0.0);
	double xbb_min = 0.0, xbb_max = 0.0, ybb_min = 0.0, ybb_max = 0.0;

	// Veretx positions on driving plane
	double x1 = 0.0, y1 = 0.0, x2 = 0.0, y2 = 0.0, x3 = 0.0, y3 = 0.0;

	// Normal vectors at vertices
	//      
	//        y /|\
	//           |
	//           |                     v1
	//           |
	//           | y_mid         v2           v4
	//           |                                
	//           |                                   v3
	//           |

	// Original averaged normals at vertices
public:
	std::shared_ptr<Vector3D> p1_normal, p2_normal, p3_normal;
	// Normals at ordered vertices   
	bool use_interpolated_normals = false;
	std::shared_ptr<Vector3D> v1, v2, v3, v4;
	double v1_x = 0.0, v2_x = 0.0, v3_x = 0.0, v4_x = 0.0;
	float y_mid = 0.0F;


	static inline long long total_ints = 0;

	/*
	private Vector3D  edge1;
	private Vector3D  edge2;   
	private  static Point3D s_point = new Point3D();
	private  static Vector3D  q_vector = new Vector3D();    
	private  static Vector3D  h_vector = new Vector3D();       
	public   Point3D  vertex_0;
	*/

	virtual void ReadFromFile(std::shared_ptr<Scanner> s, std::vector<std::shared_ptr<Point3D>> &vertices);

	/*
	public boolean IsIntersected( RTRay ray )
	{    
	   ray.u.CrossProduct( h_vector, edge2 );
	   
	   double   a = edge1.DotProduct( h_vector );
	   
	   if (a > -0.0000001f && a < 0.0000001f) 
	      return false; // ray parallel to 
	   
	   double f = 1 / a;
	   
	   Point3D.Sub(s_point, ray.P, vertex_0 );
	   
	   double u = f * s_point.DotProduct( h_vector );
	   if (u < 0 || u > 1) 
	      return false;
	   
	   s_point.CrossProduct( q_vector, edge1 );
	   double v = f * ray.u.DotProduct( q_vector );
	   if (v < 0 || u + v > 1) 
	      return false;
	   double t = -f * edge2.DotProduct( q_vector );
	   if (t < 0.0000001f)
	      return false;
	   ray.t_int = t;

	   int_point.x = ray.P.x + t * ray.u.x;
	   int_point.y = ray.P.y + t * ray.u.y;
	   int_point.z = ray.P.z + t * ray.u.z;     
	   ray.int_point = int_point;
	   
	   return true;
	}    
	*/

	bool IsIntersected(std::shared_ptr<RTRay> ray) override;

	virtual void GetNativeNormal(std::shared_ptr<Vector3D> N);

	void GetNormal(std::shared_ptr<Point3D> p, std::shared_ptr<Vector3D> N) override;

	virtual void PrepareNormalInterpolationData(std::vector<std::shared_ptr<Point3D>> &vertices);

	virtual void AddAvgNormal(std::shared_ptr<Vector3D> normal, int ind);

	virtual void CorrectAvgNormals();


	virtual std::shared_ptr<Point3D> DisplaceIntPoint(std::shared_ptr<Point3D> int_point, std::shared_ptr<Point3D> observer, std::vector<std::shared_ptr<Point3D>> &vertices);

protected:
	std::shared_ptr<Triangle> shared_from_this()
	{
		return std::static_pointer_cast<Triangle>(Shape3D::shared_from_this());
	}
};

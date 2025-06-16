// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "SimpleRT.h"
#include "World.h"
#include "RGBColor.h"
#include "RTUtils.h"
#include "RTException.h"

using BufferedImage = java::awt::image::BufferedImage;
using File = java::io::File;
using IOException = java::io::IOException;
using ImageIO = javax::imageio::ImageIO;

void SimpleRT::main(std::vector<std::wstring> &args)
{
	 std::shared_ptr<World> world;

	 if (args.size() < 2)
	 {
		std::wcout << L"At least one parameter required (input scene file) " << std::endl;
		return;
	 }

	 std::wcout << L"Running in: " << System::getProperty(L"java.vm.name") << L" " << System::getProperty(L"java.version") << std::endl;
	 world = std::make_shared<World>();

	 try
	 {
		std::shared_ptr<BufferedImage> image;

		// Plane normal testing
		// Point3D  p1 = new Point3D( 0.0, 1.0, 0.0 );
		// Point3D  p2 = new Point3D( 1.0, 0.0, 0.0 );           
		// Point3D  p3 = new Point3D( 0.0, 0.0, 1.0 );
		//
		// Plane3D plane = new Plane3D();
		// plane.CreateFromVertives( p1, p2, p3 );
		//
		// System.out.print( "a: " + plane.N.x + " " );
		// System.out.print( "b: " + plane.N.y + " " );
		// System.out.print( "c: " + plane.N.z + " " );
		// System.out.println( "d: " + plane.d );

		double amb = 0.0;
		int supersampling = 0;
		std::shared_ptr<RGBColor> b_color;
		if (args.size() >= 4)
		{
		   b_color = std::make_shared<RGBColor>(std::stoi(args[1]), std::stoi(args[2]), std::stoi(args[3]));
		}
		else
		{
		   b_color = std::make_shared<RGBColor>(RTUtils::DEF_BCOL_RED, RTUtils::DEF_BCOL_GREEN, RTUtils::DEF_BCOL_BLUE);
		}
		if (args.size() >= 5)
		{
		   amb = std::stod(args[4]);
		}
		std::wcout << L"Ambient light: " << amb << std::endl;

		if (args.size() >= 6)
		{
		   supersampling = std::stoi(args[5]);
		}

		world->LoadFromFile(args[0], b_color, amb);
		world->SetSupersampling(supersampling);

		image = world->RayTrace();

		// Write image to a file 
		std::wstring image_fname;

		int idx = (int)args[0].rfind(L'.');
		if (idx > 0)
		{
		   image_fname = args[0].substr(0, idx) + L".bmp";
		}
		else
		{
		   image_fname = args[0] + L".bmp";
		}

		std::wcout << L"Saving image to the file: " << image_fname << std::endl;
		try
		{
			ImageIO::write(image, L"bmp", std::make_shared<File>(image_fname));
			std::wcout << L"Rendered image created successfully" << std::endl;
		}
		catch (const IOException &e)
		{
			throw RTException(L"Rendered image cannot be stored in BMP file");
		};
	 }
	 catch (const IOException &ex)
	 {
		std::wcout << L"Cannot load scene data: " << args[0] << std::endl;
	 }
	 catch (const RTException &ex)
	 {
		std::wcout << ex->msg << std::endl;
	 }
}

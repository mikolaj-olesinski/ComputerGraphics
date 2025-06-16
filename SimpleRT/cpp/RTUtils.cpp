// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "RTUtils.h"

using Color = java::awt::Color;
using BufferedReader = java::io::BufferedReader;
using FileReader = java::io::FileReader;
using FileWriter = java::io::FileWriter;
using PrintWriter = java::io::PrintWriter;
using Locale = java::util::Locale;
using Scanner = java::util::Scanner;
using IOException = java::io::IOException;

void RTUtils::RemoveComments(const std::wstring &fname, const std::wstring &tmp_fname)
{
   std::shared_ptr<BufferedReader> inputStream1 = nullptr;
   std::shared_ptr<PrintWriter> outputStream1 = nullptr;

   try
   {
	   inputStream1 = std::make_shared<BufferedReader>(std::make_shared<FileReader>(fname));
	   outputStream1 = std::make_shared<PrintWriter>(std::make_shared<FileWriter>(tmp_fname));

	   std::wstring l;
	   while ((l = inputStream1->readLine()) != L"")
	   {
		  int ind = (int)l.find(L"//");
		  if (ind == 0)
		  {
			 continue;
		  }

		  if (ind > 0)
		  {
			  l = l.substr(0, ind - 1);
		  }
		   outputStream1->println(l);
	   }
   }
// JAVA TO C++ CONVERTER TASK: There is no C++ equivalent to the exception 'finally' clause:
   finally
   {
	   if (inputStream1 != nullptr)
	   {
		   inputStream1->close();
	   }

	   if (outputStream1 != nullptr)
	   {
		   outputStream1->close();
	   }
   }
}

int RTUtils::byte2RGB(int red, int green, int blue)
{
   // Color components must be in range 0 - 255
   if (red > 255)
   {
	  red = 255;
   }
   if (green > 255)
   {
	  green = 255;
   }
   if (blue > 255)
   {
	  blue = 255;
   }
   red = 0xff & red;
   green = 0xff & green;
   blue = 0xff & blue;
   return (red << 16) + (green << 8) + blue;
}

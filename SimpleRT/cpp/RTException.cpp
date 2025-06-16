// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include "RTException.h"

RTException::RTException(const std::wstring &msg) : RuntimeException()
{
   this->msg = msg;
}

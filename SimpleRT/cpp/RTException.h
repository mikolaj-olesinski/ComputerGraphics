#pragma once

// ====================================================================================================
// Produced by the Free Edition of Java to C++ Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/java-to-cplus-converter.html
// ====================================================================================================

#include <string>
#include <memory>

class RTException : public RuntimeException, public std::enable_shared_from_this<RTException>
{
   public:
   std::wstring msg;

   RTException(const std::wstring &msg);
};

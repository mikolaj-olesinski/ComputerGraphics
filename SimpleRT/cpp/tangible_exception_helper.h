#pragma once

#include <string>
#include <exception>
#include <codecvt>
#include <locale>

class IOException : public std::exception
{
private:
    std::string msg;

public:
    IOException(const std::string &message = "") : msg(message)
    {
    }

    IOException(const std::wstring &message)
    {
        msg = std::wstring_convert<std::codecvt_utf8<wchar_t>, wchar_t>().to_bytes(message);
    }

    const char * what() const noexcept
    {
        return msg.c_str();
    }
};

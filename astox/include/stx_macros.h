#ifndef STX_MACROS_H_INCLUDED
#define STX_MACROS_H_INCLUDED


#if defined(_MSVC_TRADITIONAL) && _MSVC_TRADITIONAL
// Logic using the traditional preprocessor
#else
// Logic using cross-platform compatible preprocessor
#endif

#ifdef _MSC_VER
    #define STX_COMPILER_MSC
    #define _CRT_SECURE_NO_WARNINGS
#elif defined(__CYGWIN__) || defined(__CYGWIN32__)
    #define STX_COMPILER_CYGWIN
#elif defined(__MINGW32_MAJOR_VERSION) || defined(__MINGW32_MINOR_VERSION) || defined(__MINGW64_VERSION_MAJOR) || defined(__MINGW64_VERSION_MINOR) || defined(__MINGW32__)
    #define STX_COMPILER_MINGW
#elif defined(__GNUC__) || defined(__GNUC_MINOR__) || defined(__GNUC_PATCHLEVEL__)
    #define STX_COMPILER_GNU
#endif

#endif // STX_MACROS_H_INCLUDED
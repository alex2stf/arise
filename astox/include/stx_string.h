#ifndef STX_STRING_H_INCLUDED
#define STX_STRING_H_INCLUDED

#include "stx_macros.h"

#include <stdio.h>
 #include <string.h>
//#include <strsafe.h>

#ifdef STX_COMPILER_MSC
	#include <windows.h>
	#include <tchar.h>
	wchar_t* stx_wchar_from_char(char * in);
	char * stx_char_from_wchar(wchar_t * wc);
#endif
	int stx_char_ends_with(char* wc, char c);




#endif

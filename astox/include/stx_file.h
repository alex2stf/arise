#ifndef STX_FILE_UTIL_H_INCLUDED
#define STX_FILE_UTIL_H_INCLUDED

#include "stx_string.h"

#ifdef STX_COMPILER_MSC
    #include <windows.h>
    #define STX_FILE_SEPARATOR '\\'
    #define STX_LIST_MODE_WIN
#else
    #include <dirent.h>
    #define STX_FILE_SEPARATOR '\\'
#endif

#include <sys/stat.h>

typedef struct stx_file_attr {
    int is_dir;
    char * name;
    char * absolute_path;
} stx_file_attr;

int stx_file_is_regular(const char* path);
int stx_file_is_directory(const char* path);
void stx_list_files(const char* dir, void (*callback)(stx_file_attr));
char* stx_path_listable_format(const char* in);
char* stx_path_concat(const char * c1, const char *c2);



#endif // STX_FILE_UTIL_H_INCLUDED

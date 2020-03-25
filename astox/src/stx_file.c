#include "stx_file.h"

#include <stdlib.h>

int stx_file_is_regular(const char* path)
{
    /*
    struct stat path_stat;
    stat(path, &path_stat);
    return S_ISREG(path_stat.st_mode);
    */
}

int stx_file_is_directory(const char* path) {
    /*
    struct stat statbuf;
    if (stat(path, &statbuf) != 0) {
        return 0;
    }
    return S_ISDIR(statbuf.st_mode);
    */
}



char* stx_path_listable_format(const char* in) {
    #ifdef STX_COMPILER_GNU
        return in;
    #endif // STX_COMPILER_GNU



    size_t len = strlen(in);
    if (len < 2) {
        return in;
    }
    if (in[len - 1] == '*' && in[len - 2] == STX_FILE_SEPARATOR) {
        return in;
    }


    if (in[len - 1] == STX_FILE_SEPARATOR) {
        char* res = malloc(len + 2);
        if (res) {
            int i = 0;
            for (i = 0; i < len; i++) {
                res[i] = in[i];
            }
            res[len] = '*';
            res[len + 1] = '\0';
            return res;
        }

    }
    char* res = malloc(len + 3);
    if (res) {
        int i = 0;
        for (i = 0; i < len; i++) {
            res[i] = in[i];
        }
        res[len] = STX_FILE_SEPARATOR;
        res[len + 1] = '*';
        res[len + 2] = '\0';
        return res;
    }

    return in;

};


static inline int stx_path_limit(char * c){
    size_t len = strlen(c);
    if(len > 2){
        if(c[len - 1] == '*'){
            len-=1;
        }
        while(c[len - 1] == STX_FILE_SEPARATOR){
            len--;
        }
    }

    return len;
}

char* stx_path_concat(const char * c1, const char *c2){
    size_t first_limit = stx_path_limit(c1);
    size_t second_limit = stx_path_limit(c2);
    size_t total = first_limit + second_limit + 2;
    int index = 0;
    char * res = malloc(total);
    if (res) {
        int i = 0;
        for (i = 0; i < first_limit; i++) {
            res[index] = c1[i];
            index++;
        }

        res[index] = STX_FILE_SEPARATOR;
        index++;

        int j = 0;
        for (j = 0; j < second_limit; j++) {
            res[index] = c2[j];
            index++;
        }
        res[total - 1] = '\0';
    }
    return res;
};




#ifdef STX_LIST_MODE_WIN
static inline void stx_win_list_files(char * dir, void (*callback)(stx_file_attr), int recrsv)
{
    WIN32_FIND_DATA ffd;

    wchar_t* wcdir= stx_wchar_from_char(dir);
    // error if wn == size_t(-1)

    //assert(dir == NULL); // successful conversion

    HANDLE hFind = FindFirstFile(wcdir, &ffd);
    DWORD dwError = 0;
    if (INVALID_HANDLE_VALUE == hFind)
    {
       // printf("INVALID_HANDLE_VALUE FOR: %s\n", dir);
        _tprintf(TEXT(" INVALID HANDLE FOR [%s]"), wcdir);
        return;
    }

    do
    {

        stx_file_attr attr;
        attr.name = stx_char_from_wchar(ffd.cFileName);
        attr.is_dir = (ffd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY);
        attr.absolute_path = stx_path_concat(dir, attr.name);

        //_tprintf(TEXT("  %s   %ld bytes\n"), ffd.cFileName, ffd.nFileSizeHigh);
        callback(attr);


    } while (FindNextFile(hFind, &ffd) != 0);

    dwError = GetLastError();
    if (dwError != ERROR_NO_MORE_FILES)
    {
        printf("%s", "end");
    }

    FindClose(hFind);
    free(wcdir);

}
#else
static inline void stx_dirent_list_files(const char * dir, void (*callback)(stx_file_attr)) {
     struct dirent *de;


    DIR *dr = opendir(dir);

    if (dr == NULL)  {
        printf("Could not open current directory %s\n", dir );
        return;
    }

    while ((de = readdir(dr)) != NULL) {



        stx_file_attr attr;
        attr.name = de->d_name;
        attr.absolute_path = stx_path_concat(dir, de->d_name);
        struct stat statbuf;
        if (stat(attr.absolute_path, &statbuf) == 0) {
            attr.is_dir =  S_ISDIR(statbuf.st_mode);
        }


      callback(attr);
        //copiaza char pointer

    }

    closedir(dr);

}
#endif

void stx_list_files(const char* dir, void (*callback)(stx_file_attr)) {

char* fixed = stx_path_listable_format(dir);
#ifdef STX_LIST_MODE_WIN
    stx_win_list_files(fixed, callback, 0);
#else
    stx_dirent_list_files(dir, callback);
#endif


};

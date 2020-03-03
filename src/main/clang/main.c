#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <sys/stat.h>
#include "include/arise_file_util.h"
#include <string.h>

int is_directory(const char *path) {
   struct stat statbuf;
   if (stat(path, &statbuf) != 0)
       return 0;
   return S_ISDIR(statbuf.st_mode);
}


int is_regular_file(const char *path)
{
    struct stat path_stat;
    stat(path, &path_stat);
    return S_ISREG(path_stat.st_mode);
}


void arise_read_dir(const char * dirname, void (*callback)(file_attr) ) {
    struct dirent *de;


    DIR *dr = opendir(dirname);

    if (dr == NULL)  {
        printf("Could not open current directory" );
        return;
    }





    while ((de = readdir(dr)) != NULL) {
        file_attr attr;
        attr.name = de->d_name;
        attr.is_directory = is_directory(de->d_name);

        //copiaza char pointer
        attr.absolute_path = (char *) malloc(strlen(dirname) + strlen(de->d_name) + 1);
        strcpy(attr.absolute_path,  dr->dd_name);
        strcat(attr.absolute_path, "\\");
        strcat(attr.absolute_path, de->d_name);


        if(attr.name[0] != '.'){
            callback(attr);
        }


    }

    closedir(dr);


}



void on_file_readed(file_attr attr){
     // printf("%s\n", attr.name);


      if(is_directory(attr.absolute_path) && attr.name[0] != '.'){
         arise_read_dir(attr.absolute_path, &on_file_readed);
      }
      else {
        printf("%s\n", attr.absolute_path);
      }
}
int main(int argc, char *argv[])
{



    arise_read_dir("C:", &on_file_readed);
    printf("Hello world .... !\n");
    return 0;
}

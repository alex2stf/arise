#include <stdio.h>
#include <stdlib.h>


#include <stx_file.h>
#include <string.h>





/*
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
        printf("%s\n", attr.name);
        //copiaza char pointer
        attr.absolute_path = (char *) malloc(strlen(dr->dd_name) + strlen(de->d_name) + 2);
        strcpy(attr.absolute_path,  dr->dd_name);
        strcat(attr.absolute_path, "\\");
        strcat(attr.absolute_path, de->d_name);


        if(attr.name[0] != '.'){
            callback(attr);
        }


    }

    closedir(dr);


}

*/

void on_file_readed(stx_file_attr attr){
      printf(">> %s %d %s\n", attr.absolute_path, strlen(attr.name), (attr.is_dir ? "<DIR>\n" : "<file>\n") );

      if (attr.is_dir && attr.name[0] != '.' && (strlen(attr.name) > 1) ) {
         // size_t len = strlen(attr.absolute_path) + 3;
        //  char* ndir = stx_path_listable_format(attr.absolute_path);

          //strcpy



          //printf("NEXT %s\n", ndir);


          stx_list_files(attr.absolute_path, &on_file_readed);
      }




}
int main(int argc, char *argv[])
{




  //  stx_list_files("C:\\Applications\\ideaIU-2017.1.6.win", &on_file_readed);
    stx_list_files("C:\\Applications\\ideaIU-2017.1.6.win", &on_file_readed);
    printf("Hello world2 .... !\n");
    while (1);
    return 0;
}

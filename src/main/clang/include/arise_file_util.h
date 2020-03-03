#ifndef ARISE_FILE_UTIL_H_INCLUDED
#define ARISE_FILE_UTIL_H_INCLUDED

typedef int boolean;
enum { false, true };


typedef struct file_attr {
    boolean is_directory;
    char * name;
    char * absolute_path;
} file_attr;

void arise_read_dir(const char * dirname, void (*callback)(file_attr) );
void arise_read_file();

#endif // ARISE_FILE_UTIL_H_INCLUDED

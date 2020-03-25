#include <stx_string.h>

#ifdef STX_COMPILER_MSC

wchar_t* stx_wchar_from_char(char* in)
{
    mbstate_t state;
    memset(&state, 0, sizeof state);
    size_t wn = mbsrtowcs(NULL, &in, 0, &state) + 1;
    wchar_t* buf = (wchar_t*) malloc(sizeof(wchar_t) * wn);
    wn = mbsrtowcs(&buf[0], &in, wn, &state);
    return buf;
};

char* stx_char_from_wchar(wchar_t* wc)
{
    int len = wcslen(wc);

    // _tprintf(TEXT(" ALLOC %s   %ld bytes\n"),wc, len);

    char* res = malloc(len);
    sprintf(res, "%ws", wc);
    return res;
};





#endif


int stx_char_ends_with(char* wc, char c) {
    size_t len = strlen(wc);
    if (len == 0) {
        return 0;
    }
    if (len == 1) {
        return c == wc[0];
    }
    return c == wc[strlen(wc) - 1];
};

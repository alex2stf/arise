package com.arise.cargo.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Dependencies {
    public static final Dependency VLC_2_1_0 = new Dependency()
            .setName("vlc_2_1_0").setWindowsSource("https://get.videolan.org/vlc/2.1.0/win32/vlc-2.1.0-win32.zip");


    public static final Dependency MEDIA_INFO_CLI_WIN32 = new Dependency()
            .setName("media_info_cli_32")
            .setWindowsSource("https://mediaarea.net/download/binary/mediainfo/0.7.60/MediaInfo_CLI_0.7.60_Windows_i386.zip");

    public static final Dependency MNT_CLI_WIN_32 = new Dependency()
            .setName("mnt_cli_32")
            .setWindowsSource("https://netix.dl.sourceforge.net/project/moviethumbnail/movie%20thumbnailer%20win32%20binary/mtn-200808a-win32/mtn-200808a-win32.zip");

    public static final Dependency NWJS_0_12_0 =
            new Dependency().setName("nwjs_0_12_0").setWindowsSource("http://dl.nwjs.io/v0.12.0/nwjs-v0.12.0-win-ia32.zip");

    public static final Dependency MINGW_PORTABLE = new Dependency().setName("mingw-portable")
            .setWindowsSource("https://github.com/jonasstrandstedt/MinGW/archive/master.zip");






}

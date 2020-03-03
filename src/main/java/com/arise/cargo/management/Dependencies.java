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

    public static final Dependency NWJS_0_12_0 =
            new Dependency().setName("nwjs_0_12_0").setWindowsSource("http://dl.nwjs.io/v0.12.0/nwjs-v0.12.0-win-ia32.zip");

    public static final Dependency MINGW_PORTABLE = new Dependency().setName("mingw-portable")
            .setWindowsSource("https://github.com/jonasstrandstedt/MinGW/archive/master.zip");






}

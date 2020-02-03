package com.arise.cargo.management;

public class Dependencies {
    public static final Dependency VLC_2_1_0 = new Dependency()
            .setName("vlc_2_1_0").setWindowsSource("https://get.videolan.org/vlc/2.1.0/win32/vlc-2.1.0-win32.zip");

    public static final Dependency NWJS_0_12_0 =
            new Dependency().setName("nwjs_0_12_0").setWindowsSource("http://dl.nwjs.io/v0.12.0/nwjs-v0.12.0-win-ia32.zip");


    public static final Dependency FFMPEG_32_BINARIES =
            new Dependency().setName("ffmpeg_32_bin").setWindowsSource("https://github.com/vot/ffbinaries-prebuilt/releases/download/v3.2/ffmpeg-3.2-win-32.zip");

    //https://github.com/vot/ffbinaries-prebuilt/releases/download/v3.2/ffmpeg-3.2-win-32.zip
}

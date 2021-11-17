package com.arise.weland.impl;

import com.arise.cargo.management.Dependency;
import com.arise.cargo.management.DependencyManager;
import com.arise.cargo.management.Unarchiver;

import java.io.File;
import java.io.IOException;

public class PCDependencies {
    static {
        try {
            DependencyManager.solve(new Dependency().setWindowsSource("https://www.ffmpeg.org/releases/ffmpeg-3.3.9.tar.gz")
                    .setName("ffmpeg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class GZipUnarchiver implements Unarchiver {

        @Override
        public boolean extract(File source, File destination) {



            return false;
        }
    }


}

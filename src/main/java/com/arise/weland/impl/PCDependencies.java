package com.arise.weland.impl;

import com.arise.cargo.management.Dependency;
import com.arise.cargo.management.DependencyManager;
import com.arise.cargo.management.Unarchiver;
import com.arise.core.tools.StreamUtil;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

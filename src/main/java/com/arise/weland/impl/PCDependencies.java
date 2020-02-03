package com.arise.weland.impl;

import com.arise.cargo.management.Dependency;
import com.arise.cargo.management.DependencyManager;
import com.arise.cargo.management.Unarchiver;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.models.Condition;
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
        DependencyManager.unarchivers.add(new Condition<File, Unarchiver>() {
            @Override
            public Unarchiver getPayload() {
                return new GZipUnarchiver();
            }

            @Override
            public boolean isAcceptable(File data) {
                return data.getName().endsWith("tar.gz");
            }
        });
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

            try {
                GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(new FileInputStream(source));
                TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn);
                TarArchiveEntry entry;

                while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                    /** If the entry is a directory, create the directory. **/
                    if (entry.isDirectory()) {
                        File f = new File(destination, entry.getName());
                        boolean created = f.mkdirs();
                        if (!created) {
                            System.out.printf("Unable to create directory '%s', during extraction of archive contents.\n",
                                    f.getAbsolutePath());
                        }
                    } else {
                        int count;
                        byte data[] = new byte[204];
                        FileOutputStream fos = new FileOutputStream(new File(destination, entry.getName()), false);
                        try (BufferedOutputStream dest = new BufferedOutputStream(fos, 204)) {
                            while ((count = tarIn.read(data, 0, 204)) != -1) {
                                dest.write(data, 0, count);
                            }
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            return false;
        }
    }

   static class SevenZipUnarchiver implements Unarchiver {

        @Override
        public boolean extract(File source, File destination) {
            SevenZFile sevenZFile = null;
            try {
                sevenZFile = new SevenZFile(new SeekableInMemoryByteChannel(StreamUtil.fullyReadFileToBytes(source)));
                SevenZArchiveEntry entry = sevenZFile.getNextEntry();
                while(entry!=null){
                    System.out.println(entry.getName());
                    if (entry.isDirectory()){
                        File dir = new File(destination, entry.getName());
                        dir.mkdirs();
                        continue;
                    }
                    File file = new File(destination, entry.getName());
                    file.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(file);
                    byte[] content = new byte[(int) entry.getSize()];
                    sevenZFile.read(content, 0, content.length);
                    out.write(content);
                    out.close();
                    entry = sevenZFile.getNextEntry();
                }
                sevenZFile.close();
            } catch (Exception e) {
                e.printStackTrace();
                destination.delete();
                return false;
            }

            return true;
        }
    }
}

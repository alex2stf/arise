package com.arise.weland.impl.unarchivers;

import com.arise.core.models.Unarchiver;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class GZip extends Unarchiver {
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
            return false;
        }
        return true;
    }
}

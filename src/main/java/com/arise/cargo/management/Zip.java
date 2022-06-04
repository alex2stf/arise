package com.arise.cargo.management;

import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.arise.core.tools.Util.close;

public class Zip implements Unarchiver {


    @Override
    public boolean extract(File source, File destination) {

        // create output directory if it doesn't exist
        if(!destination.exists()) {
            destination.mkdirs();
        }
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(source);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){

                String fileName = ze.getName();
                File newFile = new File(destination + File.separator + fileName);
                if (ze.isDirectory() && newFile.exists()){
                    newFile.mkdir();
                    ze = zis.getNextEntry();
                    System.out.println("mkdir " + newFile.getAbsolutePath());
                    continue;
                }

                File parentDir = new File(newFile.getParent());
                if (!parentDir.exists()){
                    parentDir.mkdirs();
                }

                if (!newFile.exists()){
                    System.out.println("unzip "+ newFile.getAbsolutePath());
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    close(fos);
                }

                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            close(zis);
            close(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FileUtil.hasFiles(destination);
    }


}

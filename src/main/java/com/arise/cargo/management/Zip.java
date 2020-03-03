package com.arise.cargo.management;

import com.arise.core.tools.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
                if (ze.isDirectory()){
                    newFile.mkdir();
                    ze = zis.getNextEntry();
                    System.out.println("mkdir " + newFile.getAbsolutePath());
                    continue;
                }
                System.out.println("unzip "+ newFile.getAbsolutePath());
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FileUtil.hasFiles(destination);
    }


}

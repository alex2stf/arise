package com.arise.core.models;

import com.arise.core.tools.StreamUtil;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.arise.core.tools.FileUtil.hasFiles;
import static com.arise.core.tools.Util.close;

public class Archiver {
    public static Archiver forName(String n) {
        if (n.equalsIgnoreCase("7zip")){
            return SEVEN_ZIP;
        }
        return ZIP;
    }


    private static final Archiver SEVEN_ZIP = new Archiver() {
        @Override
        public boolean extract(File source, File destination) {
           /*
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
                return false;
            }

            */

            return hasFiles(destination);
        }
    };

    public static final Archiver ZIP = new Archiver() {
        @Override
        public boolean extract(File src, File dest) {
            // create output directory if it doesn't exist
            if(!dest.exists()) {
                dest.mkdirs();
            }
            FileInputStream fis;
            //buffer for read and write data to file
            byte[] buffer = new byte[1024];
            try {
                fis = new FileInputStream(src);
                ZipInputStream zis = new ZipInputStream(fis);
                ZipEntry ze = zis.getNextEntry();
                while(ze != null){

                    String fileName = ze.getName();
                    File newFile = new File(dest + File.separator + fileName);
                    if (ze.isDirectory() ){
                        if (!newFile.exists()) {
                            newFile.mkdir();
                            System.out.println("mkdir " + newFile.getAbsolutePath());
                        }
                        ze = zis.getNextEntry();
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
            return hasFiles(dest);
        }
    };

    public boolean extract(File source, File destination){
        return false;
    }



}

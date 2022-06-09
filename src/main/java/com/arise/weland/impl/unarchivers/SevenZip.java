package com.arise.weland.impl.unarchivers;

import com.arise.core.models.Unarchiver;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StreamUtil;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import java.io.File;
import java.io.FileOutputStream;

public class SevenZip implements Unarchiver {
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
//            destination.delete();
            return false;
        }

        return FileUtil.hasFiles(destination);
    }
}

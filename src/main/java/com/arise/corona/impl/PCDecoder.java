package com.arise.corona.impl;

import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.corona.dto.ContentInfo;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.images.Artwork;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Icon;


public class PCDecoder extends ContentInfoDecoder {
    Map<String, ContentInfo> cache = new HashMap<>();


    @Override
    protected ContentInfo decodeFile(File file) {
        if (cache.containsKey(file.getAbsolutePath())){
            return cache.get(file.getAbsolutePath());
        }
        ContentInfo info = new ContentInfo(file);

        String s = info.getExt();
        if (!file.getName().endsWith("mp3")){
            return info;
        }
        System.out.println(s);
//        tryAudioTagger(info, file);
        trySwing(info, file);

        cache.put(file.getAbsolutePath(), info);

        return info;
    }



    private void trySwing(ContentInfo info, File file) {
        Icon ico = javax.swing.filechooser.FileSystemView.getFileSystemView().getSystemIcon(
               file
        );






        BufferedImage bi;
        bi = new BufferedImage(ico.getIconWidth(),ico.getIconHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics g = bi.createGraphics();
        ico.paintIcon(null, g, 0, 0);
        g.setColor(Color.WHITE);
        g.drawString("text", 10, 20);
        g.dispose();

//         ByteArrayOutputStream os = new ByteArrayOutputStream();
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(new File(info.getExt() + "thumb.jpg"));
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
        try {
            ImageIO.write(bi, "jpg", os);
        } catch (IOException e) {

            e.printStackTrace();
        }
        try {
            os.close();
        }catch (Throwable t){

        }
    }


    private void tryAudioTagger(ContentInfo info,File file){
        AudioFile f = null;
        try {
            f = AudioFileIO.read(file);
        } catch (Exception e) {
            f = null;
        }
        if (f == null){
            return;
        }

        Tag t = f.getTag();

        try {
            info.setAlbumName(t.getFirst(FieldKey.ALBUM));
        }catch (Exception e){

        }

        try {

        }catch (Exception e){
            info.setArtist(t.getFirst(FieldKey.ARTIST));
        }

        try {
            info.setComposer(t.getFirst(FieldKey.COMPOSER));
        }catch (Exception e){

        }

        try {
            Artwork artwork = t.getFirstArtwork();
            byte [] data = artwork.getBinaryData();
            File out = new File(info.getName() + "_art.jpg");
            FileOutputStream fileOutputStream
                    = new FileOutputStream(file);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
            System.out.println("Successfully written " + out.getAbsolutePath());
        } catch (Exception e){

        }

        System.out.println("Audiotagger tryed " + file.getAbsolutePath());

    }

    @Override
    protected File getStateDirectory() {
        return FileUtil.findAppDir();
    }

}

package com.arise.weland.impl;

import com.arise.core.tools.*;
import com.arise.core.tools.models.Convertor;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.impl.unarchivers.MediaInfoSolver;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.Util.close;


public class PCDecoder extends ContentInfoDecoder {

    private static final Mole log = Mole.getInstance(PCDecoder.class);


    public PCDecoder(){
        //adauga convertoare de PC:
        suggestionService.addConvertor(new Convertor<SuggestionService.Data, ContentInfo>() {
            @Override
            public SuggestionService.Data convert(ContentInfo data) {
                try {
                   return MediaInfoSolver.solve(data);
                } catch (Exception e){

                }
                return null;
            }
        });
    }

    public static File thumbnailsDirectory(){
        File f = new File(FileUtil.findAppDir(), "wlndicns");
        if (!f.exists()){
            f.mkdirs();
        }
        return f;
    }



//    private void innerFileSearch(ContentInfo info, File file){
//        File currDir = file.getParentFile();
//
//        File images[] = currDir.listFiles(new FilenameFilter() {
//            @Override
//            public boolean accept(File file, String s) {
//                return ContentType.isPicture(s);
//            }
//        });
//
//        if (!isEmpty(images)){
//            java.util.List<File> imgList = new ArrayList<>();
//            //TODO is there a better way????
//            for (File img: images){
//                imgList.add(img);
//            }
//            Collections.sort(imgList, new Comparator<File>() {
//                @Override
//                public int compare(File t1, File t2) {
//                    //TODO sort by size? name? AlbumArt?
//                    return t1.getName().compareTo(t2.getName());
//                }
//            });
//
//
//            File albumArt = imgList.get(0);
//
//            System.out.println("FOR " + info.getName() + " found " + albumArt.getAbsolutePath());
//            String thumbnailId = albumArt.getAbsolutePath();
//            try {
//                thumbnailId = URLEncoder.encode(thumbnailId, "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            info.setThumbnailId(thumbnailId);
//        }//exit if imglist
//    }

    @Override
    public ContentInfo decode(File file) {
        if (contentCache.containsKey(file.getAbsolutePath())){
            return contentCache.get(file.getAbsolutePath());
        }
        final ContentInfo info = new ContentInfo(file);
        if (file.getParentFile() != null){
            info.setGroupId(file.getParentFile().getName());
        }

        this.fixThumbnails(info);


        contentCache.put(file.getAbsolutePath(), info);

        return info;
    }

    @Override
    public void onScanComplete() {

    }



//    private void trySwing(ContentInfo info, File file) {
//
//
//        Icon ico = javax.swing.filechooser.FileSystemView.getFileSystemView().getSystemIcon(
//                file
//        );
//
//        BufferedImage bi;
//        bi = new BufferedImage(ico.getIconWidth(),ico.getIconHeight(), BufferedImage.TYPE_INT_RGB);
//
//        Graphics g = bi.createGraphics();
//        ico.paintIcon(null, g, 0, 0);
//        g.setColor(Color.WHITE);
//        g.drawString("text", 10, 20);
//        g.dispose();
//
//        FileOutputStream os = null;
//        String id = info.getExt() + "thumb.jpg";
//
//
//
//        File out = new File(getStateDirectory(), id);
//        if (out.exists()){
//            return;
//        }
//        try {
//
//            os = new FileOutputStream(out);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        if (bi != null){
//            try {
//                ImageIO.write(bi, "jpg", os);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
////            if (out != null && out.exists() && !bytesCache.containsKey(id)){
////                try {
////                    bytesCache.put(id, StreamUtil.fullyReadFileToBytes(out));
////                } catch (IOException e) {
////                    bytesCache.remove(id);
////                }
////            }
//        }
//        info.setThumbnailId(id);
//        close(os);
//
//    }

//    private void tryAudioTagger(ContentInfo info,File file){
//        AudioFile f = null;
//        try {
//            f = AudioFileIO.read(file);
//        } catch (Throwable e) {
//
//            f = null;
//        }
//        if (f == null){
//            log.info("AudioTagger null for " + file.getAbsolutePath());
//            return;
//        }
//
//        Tag t = f.getTag();
//
//        try {
//            info.setAlbumName(t.getFirst(FieldKey.ALBUM));
//        }catch (Exception e){
//
//        }
//
//        try {
//
//        }catch (Exception e){
//            info.setArtist(t.getFirst(FieldKey.ARTIST));
//        }
//
//        try {
//            info.setComposer(t.getFirst(FieldKey.COMPOSER));
//        }catch (Exception e){
//
//        }
//
//        FileOutputStream fileOutputStream = null;
//        try {
//            Artwork artwork = t.getFirstArtwork();
//            byte [] data = artwork.getBinaryData();
//            String id = info.getName() + "_art.jpg";
//            File out = new File(getStateDirectory(), id);
//            fileOutputStream = new FileOutputStream(out);
//            fileOutputStream.write(data);
//            fileOutputStream.flush();
//            fileOutputStream.close();
//        } catch (Exception e){
//
//        }
//        close(fileOutputStream);
//    }

    @Override
    protected File getStateDirectory() {
       return thumbnailsDirectory();
    }



}
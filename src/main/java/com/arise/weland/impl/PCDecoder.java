package com.arise.weland.impl;

import com.arise.core.tools.*;
import com.arise.weland.dto.ContentInfo;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import uk.co.caprica.vlcj.player.MediaMeta;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.core.tools.CollectionUtil.isEmpty;


public class PCDecoder extends ContentInfoDecoder {
    Map<String, ContentInfo> cache = new HashMap<>();

    private static final Mole log = Mole.getInstance(PCDecoder.class);





    @Override
    protected ContentInfo decodeFile(File file) {
        if (cache.containsKey(file.getAbsolutePath())){
            return cache.get(file.getAbsolutePath());
        }
        ContentInfo info = new ContentInfo(file);

//        System.out.println("decode " + file);
        String s = info.getExt();



        try {
            trySwing(info, file);
        } catch (Throwable t){

        }


        if (ContentType.isVideo(file)){
            VLCPlayer.getInstance().solveSnapshot(info, getStateDirectory());
        }
        else if (ContentType.isMusic(file)){
            MediaMeta mediaMeta = VLCPlayer.getInstance().fetchData(info, getStateDirectory());

            if (mediaMeta != null && mediaMeta.getArtworkUrl() != null){
                System.out.println("\n");
                System.out.println("FOR " + info.getName() + " found META " + mediaMeta.getArtworkUrl());
                System.out.println("\n");

            }


            else {
                File currDir = file.getParentFile();

                File images[] = currDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        return ContentType.isPicture(s);
                    }
                });

                if (!isEmpty(images)){
                    java.util.List<File> imgList = new ArrayList<>();
                    //TODO is there a better way????
                    for (File img: images){
                        imgList.add(img);
                    }
                    Collections.sort(imgList, new Comparator<File>() {
                        @Override
                        public int compare(File t1, File t2) {
                            //TODO sort by size? name? AlbumArt?
                            return t1.getName().compareTo(t2.getName());
                        }
                    });


                    File albumArt = imgList.get(0);

                    System.out.println("FOR " + info.getName() + " found " + albumArt.getAbsolutePath());
                    String thumbnailId = albumArt.getAbsolutePath();
                    try {
                        thumbnailId = URLEncoder.encode(thumbnailId, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    info.setThumbnailId(thumbnailId);
                }//exit if imglist
            }



        }



        cache.put(file.getAbsolutePath(), info);

        //VLCPlayer.getInstance().checkSnpUeue();
        return info;
    }


    @Override
    public void onScanComplete() {
        if (VLCPlayer.getInstance().snapshotMediaComponent != null){
            VLCPlayer.getInstance().snapshotMediaComponent.onScanComplete();
        }
    }

    private Map<String, byte[]> bytesCache = new ConcurrentHashMap<>();

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

        FileOutputStream os = null;
        String id = info.getExt() + "thumb.jpg";



        File out = new File(getStateDirectory(), id);
        if (out.exists()){
            return;
        }
        try {

            os = new FileOutputStream(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (bi != null){
            try {
                ImageIO.write(bi, "jpg", os);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (out != null && out.exists() && !bytesCache.containsKey(id)){
                try {
                    bytesCache.put(id, StreamUtil.fullyReadFileToBytes(out));
                } catch (IOException e) {
                    bytesCache.remove(id);
                }
            }
        }
        info.setThumbnailId(id);
        Util.close(os);

    }


    private void tryAudioTagger(ContentInfo info,File file){
        AudioFile f = null;
        try {
            f = AudioFileIO.read(file);
        } catch (Throwable e) {
            f = null;
        }
        if (f == null){
            log.info("AudioTagger null for " + file.getAbsolutePath());
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

        FileOutputStream fileOutputStream = null;
        try {
            Artwork artwork = t.getFirstArtwork();
            byte [] data = artwork.getBinaryData();
            String id = info.getName() + "_art.jpg";
            File out = new File(getStateDirectory(), id);
            fileOutputStream = new FileOutputStream(out);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e){

        }
        Util.close(fileOutputStream);
    }

    @Override
    protected File getStateDirectory() {
        File f = new File(FileUtil.findAppDir(), "wlndicns");
        if (!f.exists()){
            f.mkdirs();
        }
        return f;
    }

    @Override
    public byte[] getThumbnail(String id) {
        if (bytesCache.containsKey(id)){
            return bytesCache.get(id);
        }

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }



        File f = new File(id);

        if (!f.exists()){
            f = new File(getStateDirectory(), id);
        }

        if (f.exists()){
            byte[] res;
            try {
                res = StreamUtil.fullyReadFileToBytes(f);
            } catch (IOException e) {
                res = null;
            }
            if (res != null && res.length > 1){
                bytesCache.put(id, res);
                return res;
            }
        }
        return new byte[0];
    }

    @Override
    public ContentType getThumbnailContentType(String id) {
        return ContentType.IMAGE_JPEG;
    }

}
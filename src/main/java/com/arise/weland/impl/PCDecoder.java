package com.arise.weland.impl;

import com.arise.cargo.management.DependencyManager;
import com.arise.core.tools.*;
import com.arise.weland.IDGen;
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
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.arise.core.tools.CollectionUtil.isEmpty;


public class PCDecoder extends ContentInfoDecoder {

    private static final Mole log = Mole.getInstance(PCDecoder.class);


    private static final SuggestionService suggestionService = new SuggestionService()
            .load("weland/config/commons/suggestions.json");



    @Override
    public ContentInfo decode(File file) {
        if (contentCache.containsKey(file.getAbsolutePath())){
            return contentCache.get(file.getAbsolutePath());
        }
        final ContentInfo info = new ContentInfo(file);
        if (file.getParentFile() != null){
            info.setGroupId(file.getParentFile().getName());
        }

        try {
            MediaInfoSolver.solve(info);
        }catch (Exception e){

        }

        if (!StringUtil.hasText(info.getThumbnailId())){
            suggestionService.searchIcons(file.getName(), new SuggestionService.Manager() {
                @Override
                public boolean manage(String id, String path, URL url) {
                    byte bytes[] = get(id, url, getStateDirectory());
                    if (bytes != null && bytes.length > 2){
                        info.setThumbnailId(id);
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean manageBytes(String x, byte[] bytes, ContentType contentType) {
                    if (bytes != null){
                        info.setThumbnailId(x);
                        bytesCache.put(x, bytes);
                    }
                    return true;
                }
            });
        }



        if (!StringUtil.hasText(info.getThumbnailId())){
            try {
                trySwing(info, file);
            } catch (Throwable t){

            }
        }
        contentCache.put(file.getAbsolutePath(), info);

        return info;
    }


    public byte[] get(String id, URL url, File cacheDir){

        if (bytesCache.containsKey(id)){
            return bytesCache.get(id);
        }
        byte[] bytes = readLocalIfExists(cacheDir, id);
        if (bytes != null){
            bytesCache.put(id, bytes);
            return bytes;
        }


        HttpURLConnection connection;
        try {
            connection = DependencyManager.getConnection(url);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = connection.getInputStream();
            bytes = StreamUtil.toBytes(inputStream);
        } catch (IOException e) {
            System.out.println("Failed to fetch " + url);
        }
        finally {
            Util.close(inputStream);
        }

        if (bytes != null){
            File f = new File(cacheDir + File.separator + id);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(f);
                fileOutputStream.write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Util.close(fileOutputStream);
        }

        if (id != null && bytes != null){
            bytesCache.put(id, bytes);
        }
        return bytes;
    }


    private void innerFileSearch(ContentInfo info, File file){
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


    @Override
    public void onScanComplete() {

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
        System.out.println("GET THUMBNAIL " + id);
        if (!StringUtil.hasText(id)){
            return null;
        }

        if (id.startsWith("data:image")){
            try {
                return SuggestionService.decodeBase64Image(id).first();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (bytesCache.containsKey(id)){
            return bytesCache.get(id);
        }

        try {
            URL url = new URL(id);
            String actualId = IDGen.parsePath(id);
            byte bytes[] = get(actualId, url, getStateDirectory());
            if (bytes != null){
                bytesCache.put(id, bytes);
                return bytes;
            }
        } catch (Exception e) {

        }




        File f = new File(id);

        if (!f.exists()){
            try {
                f = new File(new URI(id));
            } catch (Exception e) {
                System.out.println("-----FAILED to read uri " + id);
                f = null;
            }
        }

        if (f == null || !f.exists()){
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
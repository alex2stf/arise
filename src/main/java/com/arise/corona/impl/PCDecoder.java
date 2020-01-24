package com.arise.corona.impl;

import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.Util;
import com.arise.corona.dto.ContentInfo;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaMeta;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class PCDecoder extends ContentInfoDecoder {
    Map<String, ContentInfo> cache = new HashMap<>();

    private static final Mole log = Mole.getInstance(PCDecoder.class);

    static {
        NativeLibrary.addSearchPath(
                RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files (x86)\\VideoLAN\\VLC");

        String vlcLibName = RuntimeUtil.getLibVlcName();
        String vlcLibCoreName = RuntimeUtil.getLibVlcCoreName();
        try {
            Native.loadLibrary(vlcLibName, LibVlc.class);
        } catch (Throwable e){

        }
        try {
            Native.loadLibrary("C:\\Program Files (x86)\\VideoLAN\\VLC\\libvlc.dll", LibVlc.class);
        }
        catch (Throwable t){

        }
        try {
            Native.loadLibrary("C:\\Program Files (x86)\\VideoLAN\\VLC\\libvlccore.dll", LibVlc.class);
        }catch (Throwable r){

        }
    }

    MediaPlayerFactory factory = new MediaPlayerFactory();
    MediaPlayer mediaPlayer = factory.newEmbeddedMediaPlayer();

    @Override
    protected ContentInfo decodeFile(File file) {
        if (cache.containsKey(file.getAbsolutePath())){
            return cache.get(file.getAbsolutePath());
        }
        ContentInfo info = new ContentInfo(file);

        System.out.println("decode " + file);
        String s = info.getExt();



        try {
            trySwing(info, file);
        } catch (Throwable t){

        }

        tryAudioTagger(info, file);


        cache.put(file.getAbsolutePath(), info);

        return info;
    }



    //todo recursive
    private void vlcSnap(ContentInfo info){
        mediaPlayer.stop();
        mediaPlayer.setVolume(0);
        mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter(){

            @Override
            public void videoOutput(MediaPlayer m, int newCount) {
                System.out.println("videoOutput::::");
                m.setVolume(0);
                String id = info.getName() + "vlcsnap.jpg";
                mediaPlayer.saveSnapshot(new File(getStateDirectory(), id), 200, 200);
                mediaPlayer.pause();
                info.setThumbnailId(id);

            }

            @Override
            public void mediaStateChanged(MediaPlayer mediaPlayer, int newState) {
                System.out.println("mediaStateChanged::::");
                mediaPlayer.setVolume(0);
            }

            @Override
            public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
                System.out.println("snapshotTaken(filename=" + filename + ")");
            }

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                System.out.println("playing::::");
                mediaPlayer.setVolume(0);
            }
        });



        mediaPlayer.prepareMedia(info.getPath());
        mediaPlayer.parseMedia();

        MediaMeta mediaMeta = mediaPlayer.getMediaMeta();
        int duration = (int) mediaMeta.getLength();
        info.setDuration(duration)
                .setArtist(mediaMeta.getArtist())
                .setTitle(mediaMeta.getTitle());


        mediaPlayer.play();


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
        try {
            File out = new File(getStateDirectory(), id);
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
        }
        info.setThumbnailId(id);
        Util.close(os);
    }


    private void tryAudioTagger(ContentInfo info,File file){
        AudioFile f = null;
        try {
            f = AudioFileIO.read(file);
        } catch (Exception e) {
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
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e){

        }
        Util.close(fileOutputStream);
    }

    @Override
    protected File getStateDirectory() {
        return FileUtil.findAppDir();
    }

    @Override
    public byte[] getThumbnail(String id) {
        return new byte[0];
    }

    @Override
    public ContentType getThumbnailContentType(String id) {
        return null;
    }

}
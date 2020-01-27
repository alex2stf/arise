package com.arise.weland.impl;

import com.arise.weland.dto.ContentInfo;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.LibVlcFactory;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.logger.Logger;
import uk.co.caprica.vlcj.player.MediaMeta;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.DefaultDirectMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Created by Alexandru on 1/24/2020.
 */
public class VLCPlayer {

    public static final String VLC_PATH =
            "C:\\Users\\Alexandru\\Downloads\\vlc-2.1.0";
    public static final String VLC_PATH_LIB_VLC = VLC_PATH + File.separator + "libvlc.dll";
    public static final String VLC_PATH_LIB_VLC_CORE = VLC_PATH + File.separator + "libvlccore.dll";


    static {

        System.setProperty("VLC_PLUGIN_PATH", "C:\\Program Files\\VideoLAN\\VLC\\plugins");
        NativeLibrary.addSearchPath(
                RuntimeUtil.getLibVlcLibraryName(), VLC_PATH);

        try {
            Native.loadLibrary(VLC_PATH_LIB_VLC, LibVlc.class);
        }
        catch (Throwable t){
            t.printStackTrace();
        }
        try {
            Native.loadLibrary(VLC_PATH_LIB_VLC_CORE, LibVlc.class);
        }catch (Throwable t){
            t.printStackTrace();
        }


    }


//    private final MediaPlayerFactory factory = new MediaPlayerFactory();
//    private final MediaPlayer mediaPlayer;

//    final int max_volume;


    public static final VLCPlayer instance = new VLCPlayer();

    final MyStandardEmbeddedMediaPlayerComponent mediaPlayerComponent;

    public static VLCPlayer getInstance() {
        return instance;
    }
    SnapshotMediaComponent snapshotMediaComponent = buildSnapshotMediaComponent();


    public SnapshotMediaComponent buildSnapshotMediaComponent(){
        SnapshotMediaComponent snapshotMediaComponent = new SnapshotMediaComponent();;
        JFrame frame = new JFrame("Snapshot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(0, 0);
        frame.setSize(1200, 800);
        frame.setContentPane(snapshotMediaComponent);
        frame.setVisible(true);
        return snapshotMediaComponent;
    }

    private VLCPlayer(){
        Logger.setLevel(Logger.Level.ERROR);
        JFrame frame = new JFrame("VLC Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(100, 100);
        frame.setSize(1200, 800);

        mediaPlayerComponent = new MyStandardEmbeddedMediaPlayerComponent();
        frame.setContentPane(mediaPlayerComponent);
        frame.setVisible(true);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {

                System.out.println(keyEvent.getKeyCode());
                if (27 == keyEvent.getKeyCode()){
                    System.exit(-32);//merge
                }
            }
        });
//        frame.setVisible(false);

//        mediaPlayer.addMediaPlayerEventListener(SNAPSHOT_ADAPTER);
    }


//    volatile boolean isDoingSnpshot= false;
//    volatile boolean cnDoSnpshot = false;

    volatile ContentInfo currentInfo = null;

    public synchronized void play(ContentInfo info){
        currentInfo = info;
        mediaPlayerComponent.play(info);
    }

    boolean ccc = false;

    public synchronized void solveSnapshot(ContentInfo contentInfo) {
        snapshotMediaComponent.takeSnapshot(contentInfo);
    }







    private static class MyStandardEmbeddedMediaPlayerComponent extends EmbeddedMediaPlayerComponent {

        EmbeddedMediaPlayer mediaPlayer;
        public MyStandardEmbeddedMediaPlayerComponent(){
            super();
            mediaPlayer = this.getMediaPlayer();
            mediaPlayer.setFullScreen(true);
        }

        ContentInfo currentInfo;

        public void play(ContentInfo info){

            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            fetchPrepareInfo(info);

            System.out.println("PLAY " + info.getPath());
            mediaPlayer.play();
        }

        void fetchPrepareInfo(ContentInfo info){
            currentInfo = info;
            File file = new File(info.getPath());
//            URI uri = file.toURI();

            mediaPlayer.prepareMedia(file.toURI().toASCIIString().replace("file:/", "file:///"));
            mediaPlayer.parseMedia();
            MediaMeta mediaMeta = mediaPlayer.getMediaMeta();
            int duration = (int) mediaMeta.getLength();
            info.setDuration(duration)
                    .setArtist(mediaMeta.getArtist())
                    .setTitle(mediaMeta.getTitle());

//            BufferedImage artwork = mediaMeta.getArtwork();//
        }
    }


    private static class SnapshotMediaComponent extends MyStandardEmbeddedMediaPlayerComponent {

        public SnapshotMediaComponent(){
            super();
        }

        @Override
        public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
            mediaPlayer.setVolume(0);
            synchronized (mediaPlayer){

                //Wait for file write
//                try {
//                    Thread.sleep(5 * 1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            mediaPlayer.stop();

            }
        }

        @Override
        public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
            System.out.println("snapshotTaken " + filename);
            checking = false;
            check();
        }

        Queue<ContentInfo> snapshotQueue = new LinkedTransferQueue<>();

        public void takeSnapshot(ContentInfo contentInfo) {
            snapshotQueue.add(contentInfo);
            check();
        }

        volatile boolean checking = false;

        private void check() {
            if (checking){
                return;
            }



                checking = true;
                if (!snapshotQueue.isEmpty()){


                        if (mediaPlayer.isPlaying()){
                            mediaPlayer.stop();
                        }
                        currentInfo = snapshotQueue.remove();
                        fetchPrepareInfo(currentInfo);

                        mediaPlayer.setVolume(0);
                        mediaPlayer.play();
                        mediaPlayer.skip(currentInfo.getDuration() / 2);

                        String id = URLEncoder.encode(currentInfo.getName()).replaceAll("\\+", "p")
                                .replaceAll("%", "9").replaceAll("\\.", "7");
                        File out = new File("application_directory/"+id+"vs.jpeg");
                        System.out.println("SnapshotMediaComponent#videoOutput " + out.getAbsolutePath());

                        mediaPlayer.pause();
                        while (!out.exists()){
                            mediaPlayer.saveSnapshot(out);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        checking = false;
                        check();


                }
        }


    }
}

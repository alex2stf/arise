package com.arise.weland.impl;

import com.arise.astox.net.clients.JHttpClient;
import com.arise.cargo.management.Dependencies;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.tools.Mole;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.impl.ui.desktop.WelandFrame;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.logger.Logger;
import uk.co.caprica.vlcj.player.MediaMeta;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * Created by Alexandru on 1/24/2020.
 */
public class VLCPlayer {


    private static final Mole log = Mole.getInstance(VLCPlayer.class);

    public static String VLC_BIN;

    static{

        JHttpClient.disableSSL();
        String VLC_PATH = null;
        String VLC_PATH_LIB_VLC = null;
        String VLC_PATH_LIB_VLC_CORE = null;
        try {
            DependencyManager.Resolution resolution = DependencyManager.solve(Dependencies.VLC_2_1_0);
            if (resolution != null){
                VLC_PATH = resolution.uncompressed().getAbsolutePath();
                VLC_PATH_LIB_VLC = VLC_PATH + File.separator + "libvlc.dll";
                VLC_PATH_LIB_VLC_CORE = VLC_PATH + File.separator + "libvlccore.dll";
                VLC_BIN = VLC_PATH + File.separator + "vlc.exe";
            }
            else {
                VLC_PATH = "/usr/lib/vlc";
                VLC_PATH_LIB_VLC="/usr/lib/libvlc.so.5";
                VLC_PATH_LIB_VLC_CORE="/usr/lib/libvlccore.so.8.0.0";
                VLC_BIN = "usr/bin/vlc";
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null != VLC_PATH) {

            System.setProperty("VLC_PLUGIN_PATH", VLC_PATH + File.separator + "plugins");

            NativeLibrary.addSearchPath(
                    RuntimeUtil.getLibVlcLibraryName(), VLC_PATH);
        }
        if (null != VLC_PATH_LIB_VLC) {
            try {
                Native.loadLibrary(VLC_PATH_LIB_VLC, LibVlc.class);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (null != VLC_PATH_LIB_VLC_CORE) {
            try {
                Native.loadLibrary(VLC_PATH_LIB_VLC_CORE, LibVlc.class);
            } catch (Throwable t) {
                t.printStackTrace();
            }
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



    JFrame mainFrame;
    private VLCPlayer(){
        Logger.setLevel(Logger.Level.ERROR);


        mainFrame = new JFrame("VLC Player");
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.setLocation(0, 0);
//       WelandFrame.fullScreen(mainFrame, 1200, 800);

        mediaPlayerComponent = new MyStandardEmbeddedMediaPlayerComponent();
        mainFrame.setContentPane(mediaPlayerComponent);
        mainFrame.setVisible(false);
//        mainFrame.setExtendedState( mainFrame.getExtendedState()|JFrame.MAXIMIZED_BOTH );

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stop();
            }
        });

        mainFrame.setUndecorated(true);
        mainFrame.setAlwaysOnTop(true);

        mainFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                System.out.println(keyEvent.getKeyCode());
                if (27 == keyEvent.getKeyCode()){
                    mainFrame.setState(JFrame.ICONIFIED);
                }
            }
        });
    }


    public void stop(){


        mediaPlayerComponent.stop();
        mainFrame.setVisible(false);
    }


    volatile ContentInfo currentInfo = null;

    private synchronized void showFrame(){
        if (!mainFrame.isVisible()){
            mainFrame.setVisible(true);
            mainFrame.show();
            mainFrame.setAlwaysOnTop(true);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    public synchronized void play(ContentInfo info){
        currentInfo = info;
        showFrame();
        mediaPlayerComponent.play(info);
    }

    public synchronized void play(String path){
        showFrame();
        mediaPlayerComponent.play(path);
    }



    public synchronized void solveSnapshot(ContentInfo contentInfo, File outputDirectory) {

    }

    public MediaMeta fetchData(ContentInfo info, File outputDirectory) {
        return mediaPlayerComponent.fetchPrepareInfo(info);
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

            log.trace("cplay " + info.getPath());
            mediaPlayer.play();


        }

        public void play(String path){
            log.trace("play " + path);
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            preparePath(path);
            mediaPlayer.play();
        }

        MediaMeta fetchPrepareInfo(ContentInfo info){
            currentInfo = info;
            preparePath(info.getPath());
            try {
                MediaMeta mediaMeta = mediaPlayer.getMediaMeta();
                int duration = (int) mediaMeta.getLength();
                info.setDuration(duration)
                        .setAlbumName(mediaMeta.getAlbum())
                        .setArtist(mediaMeta.getArtist())
                        .setTitle(mediaMeta.getTitle());


            return mediaMeta;

            } catch (Throwable t){
                System.out.println("Could not fetch medata for " + info.getPath());
            }
            return null;
        }

        void preparePath(String path){
            File file = new File(path);
            if (!file.exists()){
                throw new RuntimeException("File " + file.getAbsolutePath() + " does not exist");
            }
            String actual = file.toURI().toASCIIString().replace("file:/", "file:///");
           try {


               mediaPlayer.prepareMedia(actual);
              // mediaPlayer.parseMedia();
           } catch (Throwable t){
               System.out.println(actual);
               t.printStackTrace();
           }
        }

        public void stop() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
    }

    public static class MmrData extends MyStandardEmbeddedMediaPlayerComponent {
        @Override
        MediaMeta fetchPrepareInfo(ContentInfo info) {
            MediaMeta mediaMeta = super.fetchPrepareInfo(info);
            if (mediaMeta != null && mediaMeta.getArtworkUrl() != null){
                String thumbnailUrl = mediaMeta.getArtworkUrl();
                File f;
                try {
                    f = new File(new URI(thumbnailUrl));
                } catch (URISyntaxException e) {
                    f = new File(thumbnailUrl);
                }
                if (f.exists()){
                    info.setThumbnailId(f.getAbsolutePath());
                    log.info("Found thumbnail " + f.getAbsolutePath() + "\n  for " + info.getPath());
                }
            }
            mediaPlayer.release();
            return mediaMeta;
        }
    }


    public static void main(String[] args) {

        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
//        mediaPlayerFactory.newHeadlessMediaPlayer().playMedia(
//                "/home/alex/Desktop/muzic/Baniciu/2 Dealul cu dor.mp3"
//        );
//
        mediaPlayerFactory.newEmbeddedMediaPlayer().playMedia(
                "/home/alex/Desktop/muzic/Baniciu/2 Dealul cu dor.mp3"
        );

//        JFrame frame = new JFrame();
//
//        EmbeddedMediaPlayerComponent
//        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
//
//        frame.setContentPane(mediaPlayerComponent);
//
//        frame.setLocation(0, 0);
//        frame.setSize(300,400);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setVisible(true);
//
//        mediaPlayerComponent.getMediaPlayer().playMedia(
//                "/home/alex/Desktop/muzic/Baniciu/2 Dealul cu dor.mp3"
//        );//Movie name which want to play


        //VLCPlayer.getInstance().play("/home/alex/Desktop/muzic/Baniciu/2 Dealul cu dor.mp3");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
    }
}

package com.arise.weland.impl;

import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.Mole;
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
import uk.co.caprica.vlcj.player.TrackInfo;
import uk.co.caprica.vlcj.player.TrackType;
import uk.co.caprica.vlcj.player.VideoTrackInfo;
import uk.co.caprica.vlcj.player.direct.DefaultDirectMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Created by Alexandru on 1/24/2020.
 */
public class VLCPlayer {

    public static final String VLC_PATH =
            "C:\\Users\\alexandru2.stefan\\Downloads\\vlc-2.1.0-win32\\vlc-2.1.0";
    public static final String VLC_PATH_LIB_VLC = VLC_PATH + File.separator + "libvlc.dll";
    public static final String VLC_PATH_LIB_VLC_CORE = VLC_PATH + File.separator + "libvlccore.dll";

    private static final Mole log = Mole.getInstance(VLCPlayer.class);

    static {

        System.setProperty("VLC_PLUGIN_PATH", VLC_PATH + "\\plugins");
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
    public static final SnapshotMediaComponent snapshotMediaComponent = new SnapshotMediaComponent();




    private VLCPlayer(){
        Logger.setLevel(Logger.Level.ERROR);
        JFrame frame = new JFrame("VLC Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(100, 100);
        frame.setSize(1200, 800);

        mediaPlayerComponent = new MyStandardEmbeddedMediaPlayerComponent();
        frame.setContentPane(mediaPlayerComponent);
        frame.setVisible(false);

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



    public synchronized void solveSnapshot(ContentInfo contentInfo, File outputDirectory) {

        snapshotMediaComponent.takeSnapshot(contentInfo, outputDirectory);
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


            BufferedImage artwork = mediaMeta.getArtwork();
            if (artwork != null){
                System.out.println(artwork);
            }


        }
    }


    static class SnapshotMediaComponent extends MyStandardEmbeddedMediaPlayerComponent {

        private File outputDirectory;
        JFrame frame;
        public SnapshotMediaComponent(){
            super();
            frame = new JFrame("Snapshot");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocation(0, 0);
            frame.setSize(400, 400);
            frame.setContentPane(this);
            frame.setVisible(true);
        }

        int frameCount = 0;
        volatile boolean checking = false;

        @Override
        public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
            log.info("videoOutput " + currentInfo.getPath());
            mediaPlayer.setVolume(0);
            float total_frames = mediaPlayer.getFps() * mediaPlayer.getLength() / 1000;
            mediaPlayer.skip((long) (total_frames / 2));
            frameCount = 0;
        }



        @Override
        public void opening(MediaPlayer mediaPlayer) {
            mediaPlayer.setVolume(0);
            log.info("opening " + currentInfo.getPath());
        }


        @Override
        public void buffering(MediaPlayer mediaPlayer, float newCache) {
            mediaPlayer.setVolume(0);
            log.info("buffering " + currentInfo.getPath());
        }

        @Override
        public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
            mediaPlayer.setVolume(0);
            log.info(frameCount + ") positionChanged " + currentInfo.getPath());
            frameCount++;
            if (frameCount > 10){
                File out = getSnapshotFile(currentInfo);
                int width = currentInfo.getWidth();
                int height = currentInfo.getHeight();

                if (width > 0 && height > 0){
                    int newWidth = (width * 220) / height;
                    mediaPlayer.saveSnapshot(out, newWidth, 220);
                } else {
                    mediaPlayer.saveSnapshot(out);
                }
            }
        }

        static String thumbnailId(ContentInfo info){
            String id = URLEncoder.encode(info.getName())
                    .replaceAll("\\+", "p")
                    .replaceAll("%", "9")
                    .replaceAll("_", "")
                    .replaceAll("\\.", "7");
            return  id + "vs.jpg";
        }

        private File getSnapshotFile(ContentInfo info){
            String id = thumbnailId(info);
            File out = new File(outputDirectory, id);
            return out;
        }

        @Override
        public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
            mediaPlayer.setVolume(0);
            File f = new File(filename);
            if (f.exists()){
                log.info( "snapshotTaken " + filename);
                mediaPlayer.pause();
                currentInfo.setThumbnailId(thumbnailId(currentInfo));
                checking = false;
                check();
            }
        }

        Queue<ContentInfo> snapshotQueue = new LinkedTransferQueue<>();

        public void takeSnapshot(ContentInfo contentInfo, File outputDirectory)  {
            this.outputDirectory = outputDirectory;
            snapshotQueue.add(contentInfo);
            check();
        }

        @Override
        void fetchPrepareInfo(ContentInfo info) {
            super.fetchPrepareInfo(info);
            frameCount = 0;
        }



        private void check() {
            if (checking){
                return;
            }


            if (!snapshotQueue.isEmpty()){
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
                currentInfo = snapshotQueue.remove();
                fetchPrepareInfo(currentInfo);
                mediaPlayer.setVolume(0);
                File outputFile = getSnapshotFile(currentInfo);
                if (outputFile.exists()){
                    log.info("Assign  " + outputFile.getAbsolutePath() + " to " + currentInfo.getPath());
                    currentInfo.setThumbnailId(thumbnailId(currentInfo));
                    checking = false;
                    check();
                } else {
                    List<TrackInfo> trackInfos = mediaPlayer.getTrackInfo(TrackType.VIDEO);
                    if (!CollectionUtil.isEmpty(trackInfos)){
                        checking = true;

                        for (TrackInfo trackInfo: trackInfos){
                            if (trackInfo instanceof VideoTrackInfo){
                                VideoTrackInfo videoTrackInfo = (VideoTrackInfo) trackInfo;
                                currentInfo.setWidth(videoTrackInfo.width());
                                currentInfo.setHeight(videoTrackInfo.height());
                            }
                        }
                        mediaPlayer.play();
                    }
                    else {
                        log.info("Skip " + currentInfo.getPath());
                        checking = false;
                        check();
                    }
                }
            }
            else if(shouldClose){
                close();
            }
        }


        volatile boolean shouldClose = false;

        public void onScanComplete() {
            if (snapshotQueue.isEmpty() && !checking){
                close();
            }
            else {
                shouldClose = true;
            }
        }

        private void close(){
            mediaPlayer.release();
            this.release();
            frame.setVisible(false);
            frame.dispose();
            log.info("Snapshot maker closed");
        }
    }
}

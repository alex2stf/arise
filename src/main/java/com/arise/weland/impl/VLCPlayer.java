package com.arise.weland.impl;

import com.arise.astox.net.clients.JHttpClient;
import com.arise.cargo.management.Dependencies;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.tools.*;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.Message;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.logger.Logger;
import uk.co.caprica.vlcj.player.MediaMeta;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.TrackInfo;
import uk.co.caprica.vlcj.player.TrackType;
import uk.co.caprica.vlcj.player.VideoTrackInfo;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.LinkedTransferQueue;


/**
 * Created by Alexandru on 1/24/2020.
 */
public class VLCPlayer {


    private static final Mole log = Mole.getInstance(VLCPlayer.class);

    static {
        JHttpClient.disableSSL();
        String VLC_PATH = null;
        try {
            VLC_PATH = DependencyManager.solve(Dependencies.VLC_2_1_0).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        String VLC_PATH_LIB_VLC = VLC_PATH + File.separator + "libvlc.dll";
        String VLC_PATH_LIB_VLC_CORE = VLC_PATH + File.separator + "libvlccore.dll";

        try {
            DependencyManager.solve(Dependencies.NWJS_0_12_0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

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



    JFrame mainFrame;
    private VLCPlayer(){
        Logger.setLevel(Logger.Level.ERROR);

        mainFrame = new JFrame("VLC Player");
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.setLocation(100, 100);
        mainFrame.setSize(1200, 800);

        mediaPlayerComponent = new MyStandardEmbeddedMediaPlayerComponent();
        mainFrame.setContentPane(mediaPlayerComponent);
        mainFrame.setVisible(false);

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stop();
            }
        });

        mainFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {

                System.out.println(keyEvent.getKeyCode());
                if (27 == keyEvent.getKeyCode()){
                    System.exit(-32);//merge
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
        snapshotMediaComponent.takeSnapshot(contentInfo, outputDirectory);
    }

    public MediaMeta fetchData(ContentInfo info, File outputDirectory) {
        if (snapshotMediaComponent.outputDirectory == null){
            snapshotMediaComponent.outputDirectory = outputDirectory;
        }
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

            System.out.println("PLAY ContentInfo " + info.getPath());
            mediaPlayer.play();
        }

        public void play(String path){
            System.out.println("PLAY path " + path);
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
               mediaPlayer.parseMedia();
           } catch (Throwable t){
               System.out.println(actual);
               t.printStackTrace();
           }
        }

        public void stop() {
            mediaPlayer.stop();
        }
    }


    static class SnapshotMediaComponent extends MyStandardEmbeddedMediaPlayerComponent {

        private File outputDirectory;
        File propsFile = new File(FileUtil.getAppDirChild("vlccnfg"), "snapshot_ignore");
        HashSet<String> ignorables = FileUtil.serializableRead(propsFile);
        ThreadUtil.TimerResult timerResult;
        JFrame frame;
        public SnapshotMediaComponent(){
            super();
            if (ignorables == null){
                ignorables = new HashSet<>();
            }
            frame = new JFrame("Snapshot");
            frame.setUndecorated(true);
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setLocation(0, 0);
            frame.setSize(200, 200);
            frame.setContentPane(this);
            frame.setVisible(true);
            frame.setAlwaysOnTop(false);

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

            frameCount++;
            if (frameCount > 10){
                File out = getSnapshotFile(currentInfo);
                int width = currentInfo.getWidth();
                int height = currentInfo.getHeight();

                if (width > 0 && height > 0){
                    int newWidth = (width * 30) / height;
                    mediaPlayer.saveSnapshot(out, newWidth, 30);
                } else {
                    mediaPlayer.saveSnapshot(out);
                }

                if (out.exists()){
                    ThreadUtil.closeTimer(timerResult);
                    log.info( "snapshot verified at " + out.getAbsolutePath());
                    mediaPlayer.stop();
                    currentInfo.setThumbnailId(thumbnailId(currentInfo));
                    checking = false;
                }
                else {
                    log.info(frameCount + ") positionChanged expect " + out.getAbsolutePath());;
                }
            }
            else {
                log.info(frameCount + ") positionChanged " + currentInfo.getPath());;
            }
        }

        static String thumbnailId(ContentInfo info){

            if (StringUtil.hasText(info.getArtist()) && StringUtil.hasText(info.getName())){
                return Message.sanitize(info.getArtist() + info.getName());
            }
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
//            log.info("snapshot taken event");
            mediaPlayer.setVolume(0);
            if (!checking){
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
        MediaMeta fetchPrepareInfo(ContentInfo info) {
            frameCount = 0;
            return super.fetchPrepareInfo(info);
        }



        int fucked = 0;
        private void check() {
            if (checking){
                return;
            }

            ThreadUtil.closeTimer(timerResult);
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
                    }
                    else if (ignorables.contains(currentInfo.getPath())){
                        checking = false;
                        check();
                    }
                    else {
                        log.info("check " + currentInfo.getPath());
                        List<TrackInfo> trackInfos = mediaPlayer.getTrackInfo(TrackType.VIDEO);
                        if(!CollectionUtil.isEmpty(trackInfos)) {
                            for (TrackInfo trackInfo : trackInfos) {
                                if (trackInfo instanceof VideoTrackInfo) {
                                    VideoTrackInfo videoTrackInfo = (VideoTrackInfo) trackInfo;
                                    currentInfo.setWidth(videoTrackInfo.width());
                                    currentInfo.setHeight(videoTrackInfo.height());
                                }
                            }
                        }


                        boolean shouldCheck = !CollectionUtil.isEmpty(trackInfos) &&
                                currentInfo.getHeight() > 0 && currentInfo.getWidth() > 0 && currentInfo.getDuration() > 0;

                        if (shouldCheck){
                            checking = true;


                            mediaPlayer.play();
                            timerResult = ThreadUtil.delayedTask(new Runnable() {
                                @Override
                                public void run() {
                                    String level = "is";
                                    if (fucked > 100){
                                        level = "is absolutely completely";
                                    }
                                    else if (fucked > 50){
                                        level = "is really";
                                    }
                                    else if (fucked > 10){
                                        level = "seems pretty";
                                    }


                                    log.info("Dude this " + level + " fucked ! No event dispatch for " + currentInfo.getPath() );
                                    mediaPlayer.stop();
                                    ignorables.add(currentInfo.getPath());
                                    FileUtil.serializableSave(ignorables, propsFile);
                                    checking = false;
                                    check();
                                    fucked ++;

                                }
                            }, 20 * 1000);
                        }
                        else {
                            log.info("Skip " + currentInfo.getPath());
                            checking = false;
                            check();
                            ignorables.add(currentInfo.getPath());
                            FileUtil.serializableSave(ignorables, propsFile);
                            ThreadUtil.closeTimer(timerResult);
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

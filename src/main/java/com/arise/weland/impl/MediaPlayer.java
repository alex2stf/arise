package com.arise.weland.impl;

import com.arise.canter.CommandRegistry;
import com.arise.core.AppSettings;
import com.arise.core.exceptions.DependencyException;
import com.arise.core.models.Handler;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.Util;
import com.arise.weland.dto.DeviceStat;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.cargo.management.DependencyManager.withJar;
import static com.arise.core.tools.ReflectUtil.getMethod;
import static com.arise.core.tools.ThreadUtil.fireAndForget;
import static com.arise.core.tools.Util.close;
import static java.lang.Runtime.getRuntime;

public class MediaPlayer {

    public static final Map<String, MediaPlayer> m = new ConcurrentHashMap<>();
    private static final Mole log = Mole.getInstance(MediaPlayer.class);
    Object winst = null;
    private CommandRegistry r;

    static volatile boolean isAppClosed = false;

    private MediaPlayer(CommandRegistry r){
        this.r = r;
        getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
            isAppClosed = true;
            log.warn("Close all media player instances");
            }
        });
    }

    public static MediaPlayer getInstance(Class n, CommandRegistry r){
        return getInstance(n + "", r);
    }

    public static MediaPlayer getInstance(String n, CommandRegistry r){
        if (!m.containsKey(n)){
            m.put(n, new MediaPlayer(r));
        }
        return m.get(n);
    }

    public static long getAudioDurationMs(File f, long dv) {
        long len = (f.length() / 152) / 10;
        Mole.todo(f.getName() + " len=" + (len / 1000) + "sec");
        return len;
    }

    AudioInputStream audioIn = null;
    Clip clip = null;

    public Object play(final String path) {
        if (isAppClosed){
            log.warn("App received shutdown hook");
            return null;
        }
        String strategy = AppSettings.getProperty(AppSettings.Keys.MEDIA_PLAY_STRATEGY, "vlc");
        log.info("Open media " + path + " using strategy [" + strategy + "]");

        if (path.endsWith(".wav")){
            try {
                audioIn = AudioSystem.getAudioInputStream(new File(path).toURI().toURL());
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }



            try {
                clip = AudioSystem.getClip();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
            try {
                clip.open(audioIn);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clip.start();
            return audioIn;
        }

        if ("commands".equalsIgnoreCase(strategy)) {
            if (r.containsCommand("close-media-player")) {
                r.execute("close-media-player", new String[]{});
            }
            if (r.containsCommand("play-media")) {
                r.execute("play-media", new String[]{path});
            }
        } else if ("javazone-player".equalsIgnoreCase(strategy)) {

            withJar("JAVAZOOM_JLAYER_101", new Handler<URLClassLoader>() {
                @Override
                public void handle(URLClassLoader classLoader) {
                    if (winst != null) {
                        getMethod(winst, "close").call();
                    }

                    try {
                        winst = Class.forName("javazoom.jl.player.Player", true, classLoader).getConstructor(InputStream.class).newInstance(new BufferedInputStream(new FileInputStream(path)));
                        fireAndForget(new Runnable() {
                            @Override
                            public void run() {
                                getMethod(winst, "play").call();
                            }
                        }, "media-play-" + path);
                    } catch (Exception e) {
                        throw new DependencyException("javazoom.jl.player.Player failed", e);
                    }
                }
            });
        } else {
            if (winst != null && winst instanceof Process) {
                ((Process) winst).destroy();
            }
            winst = r.getCommand("play-media").execute(path);
            if (winst instanceof Process){
                try {
                    ((Process) winst).waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return winst;
    }

    final Process proc[] = new Process[]{null};

    public void playStream(String u) {
        if (proc[0] != null){
            proc[0].destroy();
        }
        proc[0] = (Process) r.getCommand("browser-open").execute(u);
    }

    public void stop() {
        if (clip != null){
            try {
                clip.stop();
            }catch (Exception e){
            }
            close(clip);
        }
        if (audioIn != null){
            close(clip);
        }
        if (proc[0] != null){
            proc[0].destroy();
        }
        r.getCommand("browser-close").execute();
    }

    public void pause() {

    }

    public String setVolume(String v) {
        if(r.containsCommand("set-master-volume")){
            return "" + r.execute("set-master-volume", new String[]{v});
        }
        return getVolume();
    }

    public String getVolume() {
        if(r.containsCommand("get-master-volume")){
            return r.execute("get-master-volume", new String[]{}) + "";
        }
        return "";
    }
}

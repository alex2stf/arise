package com.arise.weland.impl;

import com.arise.canter.CommandRegistry;
import com.arise.core.AppSettings;
import com.arise.core.AppSettings.Keys;
import com.arise.core.exceptions.DependencyException;
import com.arise.core.models.Handler;
import com.arise.core.tools.Mole;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.cargo.management.DependencyManager.withJar;
import static com.arise.core.AppSettings.getProperty;
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

    public static MediaPlayer getMediaPlayer(String n, CommandRegistry r){
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

    public Object play(final String p) {
        return play(p, null);
    }

    public Object play(final String path, final Handler<String> c) {
        if (isAppClosed){
            log.warn("App received shutdown hook");
            return null;
        }
        if (!new File(path).exists()){
            log.e("File " + path + " does not exists");
        }
        String strategy = getProperty(Keys.MEDIA_PLAY_STRATEGY, "vlc");
        log.info("Open media " + path + " using strategy [" + strategy + "]");

        if (path.endsWith(".wav")){
            stopClips();
            try {
                audioIn = AudioSystem.getAudioInputStream(new File(path).toURI().toURL());
//                AudioFormat format = audioIn.getFormat();
//                DataLine.Info info = new DataLine.Info(Clip.class, format);
                clip = AudioSystem.getClip(null);
//                clip = (Clip)AudioSystem.getLine(info);

                clip.addLineListener(new LineListener() {
                    @Override
                    public void update(LineEvent event) {
                        if (event.getType() == LineEvent.Type.STOP) {
                            c.handle(path);
                            return;
                        }
                    }
                });
                clip.open(audioIn);
                clip.start();
            } catch (Exception e) {
                log.error("Failed to play sound " + path, e);
                c.handle(path);
            }

            return audioIn;
        }

        if ("commands".equalsIgnoreCase(strategy)) {
            if (r.containsCommand("close-media-player")) {
                r.execute("close-media-player", new String[]{});
            }
            if (r.containsCommand("play-media")) {
                r.execute("play-media", new String[]{path});
            }
            c.handle(path);
        } else if ("javazone-player".equalsIgnoreCase(strategy)) {
            //TODO use JProxy
            withJar("JAVAZOOM_JLAYER_101", new Handler<URLClassLoader>() {
                @Override
                public void handle(URLClassLoader classLoader) {
                    if (winst != null) {
                        getMethod(winst, "close").call();
                    }

                    try {
                        winst = Class.forName("javazoom.jl.player.Player", true, classLoader).getConstructor(InputStream.class).newInstance(new BufferedInputStream(new FileInputStream(path)));
                        getMethod(winst, "play").call();
                    } catch (Exception e) {
                        throw new DependencyException("javazoom.jl.player.Player failed", e);
                    }
                    c.handle(path);
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
            c.handle(path);
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


    void stopClips(){
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
    }

    public void stop() {
        stopClips();
        for (Process p: proc){
           if (p != null) {
               p.destroy();
           }
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

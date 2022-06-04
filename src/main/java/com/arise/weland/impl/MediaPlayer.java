package com.arise.weland.impl;

import com.arise.canter.CommandRegistry;
import com.arise.core.AppSettings;
import com.arise.core.exceptions.DependencyException;
import com.arise.core.models.Handler;
import com.arise.core.tools.Mole;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.cargo.management.DependencyManager.withJar;
import static com.arise.core.tools.ReflectUtil.getMethod;
import static com.arise.core.tools.ThreadUtil.fireAndForget;
import static com.arise.core.tools.ThreadUtil.threadId;

public class MediaPlayer {

    public static final Map<String, MediaPlayer> m = new ConcurrentHashMap<>();
    private static final Mole log = Mole.getInstance(MediaPlayer.class);
    Object winst = null;
    private CommandRegistry r;

    private MediaPlayer(CommandRegistry r){
        this.r = r;
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

    public Object play(final String path) {
        String strategy = AppSettings.getProperty(AppSettings.Keys.MEDIA_PLAY_STRATEGY, "vlc");
        log.info("Open media " + path + " using strategy [" + strategy + "]");

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
                        }, threadId(path));
                    } catch (Exception e) {
                        throw new DependencyException("javazoom.jl.player.Player failed", e);
                    }
                }
            });
        } else {
//            mediaplayer = commandRegistry.getCommand("play-media").getProperty("process");

            if (winst != null && winst instanceof Process) {
                ((Process) winst).destroy();
            }
            winst = r.getCommand("play-media").execute(path);
        }
        return winst;
    }
}

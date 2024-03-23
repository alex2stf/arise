package com.arise.weland.desk;

import com.arise.astox.net.models.Peer;
import com.arise.canter.Command;
import com.arise.canter.CommandRegistry;
import com.arise.core.AppSettings;
import com.arise.core.AppSettings.Keys;
import com.arise.core.exceptions.DependencyException;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.OSProxies;
import com.arise.weland.model.MediaPlayer;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLClassLoader;

import static com.arise.cargo.management.DependencyManager.withJar;
import static com.arise.core.AppSettings.getProperty;
import static com.arise.core.tools.ReflectUtil.getMethod;
import static com.arise.core.tools.StringUtil.urlEncodeUTF8;
import static com.arise.core.tools.Util.close;
import static java.lang.Runtime.getRuntime;


public class DeskMPlayer extends MediaPlayer {


    private static final Mole log = Mole.getInstance(DeskMPlayer.class);
    final Process proc[] = new Process[]{null};
    Object winst = null;
    AudioInputStream aStream = null;
    Clip clip = null;
    private ContentInfoProvider cip;
    private volatile boolean is_play = false;

    public DeskMPlayer(){
        getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {

                isAppClosed = true;

                try {
                    DeskMPlayer.this.stop(null);
                }catch (Exception e){

                }
                log.warn("Close all media player instances");
            }
        });
    }

    public Object play(final String p) {
        return play(p, null);
    }

    public Object play(final String path, final Handler<String> c) {
        if (isAppClosed){
            log.warn("App received shutdown hook");
            return null;
        }
        if (!new File(path).exists()){
            log.e("File " + path + " does not exists, implemnent error handler");
            System.exit(-1);
            return null;
        }

        is_play = true;
        if (path.endsWith(".wav")){
            stopClips();
            try {
                aStream = AudioSystem.getAudioInputStream(new File(path).toURI().toURL());
                final Clip clip = AudioSystem.getClip(null);
                clip.addLineListener(new LineListener() {
                    @Override
                    public void update(LineEvent event) {
                        if (LineEvent.Type.STOP.equals(event.getType())){
                            close(clip);
                            c.handle(path);
                            is_play = false;
                        }
                        log.trace("LINE_EVENT " + event.getType() + " for " + path);
                    }
                });

                clip.open(aStream);
                clip.start();
            } catch (Exception e) {
                log.error("Failed to play sound " + path + " go next", e);
                c.handle(path);
            }

            return aStream;
        }

        String strategy = getProperty(Keys.MEDIA_PLAY_STRATEGY, "vlc");
        log.info("Open media " + path + " using strategy [" + strategy + "]");

        if ((strategy + "").indexOf("javazone-player") > -1) {
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
                    is_play = false;
                }
            });
        } else {
            if (winst != null && winst instanceof Process) {
                ((Process) winst).destroy();
            }
            Command playCmd = CommandRegistry.getInstance().getCommand("play-media");
            if (null == playCmd) {
                AppSettings.throwOrExit("No play-media command found");
            }

            winst = playCmd.execute(path);

            wait_to_execute(winst);
            log.info("winst executed" + winst);
            is_play = false;
            c.handle(path);
        }

        return winst;
    }

    void wait_to_execute(Object o){
        if(o instanceof Process){
            try {
                int i = ((Process) o).waitFor();
                log.info("process " + o + " exited with " + i);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void playStream(String u) {
        if (proc[0] != null){
            proc[0].destroy();
        }
        is_play = true;
        if (u.startsWith("https") ){
            if (!CommandRegistry.getInstance().containsCommand("browser-open")){
                 AppSettings.throwOrExit("no browser-open command defined");
            }
            proc[0] = (Process) CommandRegistry.getInstance().getCommand("browser-open").execute(u);
        } else {
            String uri = "http://localhost:8221/proxy-skin?type=radio&uri=" + urlEncodeUTF8(u);
                if (cip != null){
                ContentInfo ci = cip.findByPath(u);
                if (ci != null && ci.getThumbnailId() != null){
                    uri += "&thumbnailId=" + ci.getThumbnailId();
                }
                if (ci != null && StringUtil.hasText(ci.getTitle())){
                    uri += "&title=" + urlEncodeUTF8(ci.getTitle());
                }
            }
            proc[0] = (Process) CommandRegistry.getInstance().getCommand("browser-open").execute(uri);
        }
    }


    private void stopClips(){
        if (clip != null){
            try {
               clip.stop();
            }catch (Exception e){
            }
            close(clip);
        }
        close(aStream);
    }

    public void stop(Handler<MediaPlayer> comp) {
        log.info("Stop called");
        stopClips();
        for (Process p: proc){
           if (p != null) {
               p.destroy();
           }
        }
        if(CommandRegistry.getInstance().containsCommand("browser-close")) {
            wait_to_execute(
                    CommandRegistry.getInstance().getCommand("browser-close").execute()
            );
        }
        if(CommandRegistry.getInstance().containsCommand("close-media")) {
            wait_to_execute(
                    CommandRegistry.getInstance().getCommand("close-media").execute()
            );
        }
        is_play = false;
        if(comp != null){
            comp.handle(this);
        }
    }

    public void pause() {

    }

    public String setVolume(String v) {
        try {
            OSProxies.setVolume(v);
        }catch (Exception e){
            log.e(e);
        }
        return getVolume();
    }

    public String getVolume() {
        try {
            return OSProxies.getMasterVolume();
        }catch (Exception e){
            log.error(e);
        }
        return "0";
    }

    @Override
    public String getMaxVolume() {
        return null;
    }


    public void validateStreamUrl(String u, Handler<HttpURLConnection> suk, Handler<Tuple2<Throwable, Peer>> erh) {
        this.validateSync(u, suk, erh);
    }

    public boolean isPlaying() {
        return is_play;
    }

    public DeskMPlayer setContentInfoProvider(ContentInfoProvider cip) {
        this.cip = cip;
        return this;
    }
}

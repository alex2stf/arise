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
import com.arise.weland.impl.SGService;
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


    private static final Mole log = Mole.getInstance("PCMPLAYER");

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



        File fileToPlay = new File(path);
        if (isAppClosed){
            log.warn("App received shutdown hook");
            return null;
        }
        if (!fileToPlay.exists()){
            log.e("File " + path + " does not exists, implement error handler");
            System.exit(-1);
            return null;
        }

        if(fileToPlay.length() > 3000000) {
            log.info("setDesktopImage allowed ptr fisier de " + fileToPlay.length());
            SGService.setDesktopImage(path);
        } else {
            log.warn("NU SCHIMBA DESKTOP PENTRU FISIER DE " + fileToPlay.length());
        }


        if (path.endsWith(".wav")){
            stopClips();
            try {
                is_play = true;


                aStream = AudioSystem.getAudioInputStream(fileToPlay.toURI().toURL());
                final Clip clip = AudioSystem.getClip(null);
                clip.addLineListener(new LineListener() {
                    @Override
                    public void update(LineEvent event) {

                        boolean isStopEvent = LineEvent.Type.STOP.equals(event.getType()) || LineEvent.Type.CLOSE.equals(event.getType());
                        if (isStopEvent){
                            log.trace("HANDLE STOP EVENT " + event.getType() + " for " + path);
                            close(clip);
                            c.handle(path);
                            is_play = false;
                        } else {
                            log.trace("WAV INLINE_EVENT " + event.getType() + " for " + path);
                        }

                    }
                });

                clip.open(aStream);
                clip.start();
            } catch (Exception e) {
                is_play = true;
                log.error("Failed to play sound " + path + " go next", e);
                c.handle(path);
            }

            return aStream;
        }


        else {
            if (winst != null && winst instanceof Process) {
                ((Process) winst).destroy();
            }
            Command playCmd = CommandRegistry.getInstance().getCommand("play-media");
            if (null == playCmd) {
                AppSettings.throwOrExit("No play-media command found");
            }
            is_play = true;
            winst = playCmd.execute(fileToPlay.getAbsolutePath());

            wait_to_execute(winst, "play " + path);
            log.info("winst executed" + winst);
            is_play = false;
            c.handle(path);
        }

        return winst;
    }

    void wait_to_execute(Object o, String identifier){
        if(o instanceof Process){
            try {
                int i = ((Process) o).waitFor();
                Process p = (Process) o;
                log.info("process " + identifier  + " exited with " + i);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void playStreamSync(String u) {
        SGService.setDesktopImage(u);
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
        is_play = false;
    }

    public void stop(Handler<MediaPlayer> comp) {
        log.info("Stop called");
        stopClips();
        for (Process p: proc){
           if (p != null) {
               p.destroy();
               log.info("destroy process with exitValue=" + p.exitValue());
           }
        }
        if(CommandRegistry.getInstance().containsCommand("browser-close")) {
            wait_to_execute(
                    CommandRegistry.getInstance().getCommand("browser-close").execute(), "browser-close"
            );

        }
        if(CommandRegistry.getInstance().containsCommand("close-media")) {
            wait_to_execute(
                    CommandRegistry.getInstance().getCommand("close-media").execute(), "close-media"
            );
            log.info("media instances closed");
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



    public boolean isPlaying() {
        return is_play;
    }

    public DeskMPlayer setContentInfoProvider(ContentInfoProvider cip) {
        this.cip = cip;
        return this;
    }
}

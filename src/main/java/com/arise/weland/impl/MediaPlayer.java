package com.arise.weland.impl;

import static com.arise.cargo.management.DependencyManager.withJar;
import static com.arise.core.AppSettings.getProperty;
import static com.arise.core.tools.ReflectUtil.getClassByName;
import static com.arise.core.tools.ReflectUtil.getMethod;
import static com.arise.core.tools.StringUtil.urlEncodeUTF8;
import static com.arise.core.tools.Util.close;
import static java.lang.Runtime.getRuntime;

import com.arise.astox.net.clients.JHttpClient;
import com.arise.astox.net.models.Peer;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.canter.CommandRegistry;
import com.arise.core.AppSettings.Keys;
import com.arise.core.exceptions.DependencyException;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ReflectUtil;
import com.arise.core.tools.StringUtil;
import com.arise.weland.dto.ContentInfo;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MediaPlayer {

    public static final Map<String, MediaPlayer> m = new ConcurrentHashMap<>();
    private static final Mole log = Mole.getInstance(MediaPlayer.class);
    Object winst = null;
    private CommandRegistry r;

    static volatile boolean isAppClosed = false;

    private ContentInfoProvider cip;

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

    Object audioInputStream = null;
    Object javaxClip = null;
    private volatile boolean is_play = false;

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
        is_play = true;
        if (path.endsWith(".wav")){
            stopClips();
            try {
                audioInputStream = ReflectUtil.getStaticMethod(
                        getClassByName("javax.sound.sampled.AudioSystem"),
                        "getAudioInputStream",
                        URL.class
                ).call(new File(path).toURI().toURL());
//                        AudioSystem.getAudioInputStream(new File("C:\\Users\\Tarya\\Music\\sounds\\Dinica - 6.wav").toURI().toURL());
//                AudioFormat format = audioIn.getFormat();
//                DataLine.Info info = new DataLine.Info(Clip.class, format);
                javaxClip = ReflectUtil.getStaticMethod("javax.sound.sampled.AudioSystem", "getClip").call(null);
                        //AudioSystem.getClip(null);
//                clip = (Clip)AudioSystem.getLine(info);

                Class lineListenerClass = getClassByName("javax.sound.sampled.LineListener");
                Object lineListener = Proxy.newProxyInstance(
                        lineListenerClass.getClassLoader(),
                        new Class[]{ lineListenerClass },
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                if ("update".equals(method.getName())){
//                                    Object stopEvent = ReflectUtil.getStaticObjectProperty()
//                                    if (LineEvent.Type.STOP.equals(getMethod(args[0], "getType").call() )) {
                                    if ("Stop".equals(getMethod(args[0], "getType").call() + "" )) {
                                        c.handle(path);
                                    }
                                }
                                return null;
                            }
                        }
                );
                getMethod(javaxClip, "addLineListener", lineListenerClass).call(lineListener);
                ((Clip)javaxClip).open((AudioInputStream) audioInputStream);
//                AudioSystem.getClip().open((AudioInputStream) audioInputStream);
//                getMethod(javaxClip, "open", AudioInputStream.class).call(audioInputStream);
//                getMethod(javaxClip, "open", getClassByName("javax.sound.sampled.AudioInputStream")).call(audioInputStream);
                getMethod(javaxClip, "start").call();

            } catch (Exception e) {
                log.error("Failed to play sound " + path, e);
            }
            is_play = false;
            if (c != null) {
                c.handle(path);
            }
            return audioInputStream;
        }

        if ("commands".equalsIgnoreCase(strategy)) {
            if (r.containsCommand("close-media-player")) {
                r.execute("close-media-player", new String[]{});
            }
            if (r.containsCommand("play-media")) {
                r.execute("play-media", new String[]{path});
            }
            c.handle(path);
            is_play = false;
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
                    is_play = false;
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
            is_play = false;
            c.handle(path);
        }

        return winst;
    }

    final Process proc[] = new Process[]{null};

    public void playStream(String u) {
        if (proc[0] != null){
            proc[0].destroy();
        }
        is_play = true;
        if (u.startsWith("https")){
            proc[0] = (Process) r.getCommand("browser-open").execute(u);
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
            proc[0] = (Process) r.getCommand("browser-open")
                    .execute(uri);
        }
    }


    private void stopClips(){
        if (javaxClip != null){
            try {
                getMethod(javaxClip, "stop").call();
            }catch (Exception e){
            }
            close(javaxClip);
        }
        if (audioInputStream != null){
            close(audioInputStream);
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
        is_play = false;
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


    public void validateStreamUrl(String u, Handler<HttpURLConnection> suk, Handler<Tuple2<Throwable, Peer>> erh) {

        JHttpClient c = new JHttpClient();
        c.setAbsoluteUrl(u);
        HttpRequest request = new HttpRequest();
        request.setMethod("GET");
        c.setErrorHandler(erh);
        c.connectSync(request, suk);


    }

    public boolean isPlaying() {
        return is_play;
    }

    public MediaPlayer setContentInfoProvider(ContentInfoProvider cip) {
        this.cip = cip;
        return this;
    }
}

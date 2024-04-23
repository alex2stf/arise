package com.arise.weland.model;

import com.arise.astox.net.clients.JHttpClient;
import com.arise.astox.net.models.Peer;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.canter.CommandRegistry;
import com.arise.core.exceptions.CommunicationException;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ReflectUtil;
import com.arise.core.tools.SYSUtils;
import com.arise.weland.impl.ContentInfoProvider;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.core.tools.Util.close;

public abstract class MediaPlayer {

    private static final Mole log = Mole.getInstance(MediaPlayer.class);

    public static volatile boolean isAppClosed = false;

    public long getAudioDurationMs(File f, long dv) {
        long len = (f.length() / 152) / 10;
        Mole.todo(f.getName() + " len=" + (len / 1000) + "sec");
        return len;
    }

    public static final Map<String, MediaPlayer> m = new ConcurrentHashMap<>();

    public static MediaPlayer getMediaPlayer(String n){

        if (m.containsKey(n)){
            return m.get(n);
        }
        if (SYSUtils.isAndroid()){
            try {
                Class dmpl = Class.forName("com.arise.droid.impl.AndroidMediaPlayer");
                MediaPlayer mpl = (MediaPlayer) ReflectUtil.getStaticMethod(dmpl, "getInstance").call();
                m.put(n, mpl);
                return mpl;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!m.containsKey(n)) {
            try {
                Class dmpl = Class.forName("com.arise.weland.desk.DeskMPlayer");
                MediaPlayer mpl = (MediaPlayer) dmpl.getConstructor().newInstance();
                m.put(n, mpl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return m.get(n);
    }

    public Object play(String path){
        return play(path, null);
    }

    public abstract Object play(String path, final Handler<String> c);

    public abstract void stop(Handler<MediaPlayer> onComplete);

    public abstract MediaPlayer setContentInfoProvider(ContentInfoProvider cip);

    public abstract void pause();

    public abstract void playStreamSync(String path);

    public abstract String setVolume(String mVol);

    public abstract String getVolume();

    public abstract String getMaxVolume();

    public abstract boolean isPlaying();






}

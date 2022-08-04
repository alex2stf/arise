package com.arise.weland.model;

import com.arise.astox.net.models.Peer;
import com.arise.canter.CommandRegistry;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.weland.impl.ContentInfoProvider;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MediaPlayer {

    public static volatile boolean isAppClosed = false;

    public static long getAudioDurationMs(File f, long dv) {
        long len = (f.length() / 152) / 10;
        Mole.todo(f.getName() + " len=" + (len / 1000) + "sec");
        return len;
    }

    public static final Map<String, MediaPlayer> m = new ConcurrentHashMap<>();

    public static MediaPlayer getMediaPlayer(String n, CommandRegistry r){

        if (SYSUtils.isAndroid()){
            throw new LogicalException("NOT IMMPLEMENTED");
        }

        if (!m.containsKey(n)) {
            try {
                Class dmpl = Class.forName("com.arise.weland.desk.DeskMPlayer");
                MediaPlayer mpl = (MediaPlayer) dmpl.getConstructor(CommandRegistry.class).newInstance(r);
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

    public abstract void stop();

    public abstract MediaPlayer setContentInfoProvider(ContentInfoProvider cip);

    public abstract void pause();

    public abstract void playStream(String path);

    public abstract String setVolume(String mVol);

    public abstract String getVolume();

    public abstract boolean isPlaying();

    public abstract void validateStreamUrl(String u, Handler<HttpURLConnection> handler, Handler<Tuple2<Throwable, Peer>> tuple2Handler);
}

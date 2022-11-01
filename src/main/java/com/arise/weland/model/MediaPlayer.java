package com.arise.weland.model;

import static com.arise.core.tools.Util.close;

import com.arise.astox.net.clients.JHttpClient;
import com.arise.astox.net.models.Peer;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.canter.CommandRegistry;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ReflectUtil;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.Util;
import com.arise.weland.impl.ContentInfoProvider;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MediaPlayer {

    public static volatile boolean isAppClosed = false;

    public long getAudioDurationMs(File f, long dv) {
        long len = (f.length() / 152) / 10;
        Mole.todo(f.getName() + " len=" + (len / 1000) + "sec");
        return len;
    }

    public static final Map<String, MediaPlayer> m = new ConcurrentHashMap<>();

    public static MediaPlayer getMediaPlayer(String n, CommandRegistry r){

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

    protected void validateSync(String u, final Handler<HttpURLConnection> suk, final Handler<Tuple2<Throwable, Peer>> erh) {

        final JHttpClient c = new JHttpClient();
        c.setAbsoluteUrl(u);
        HttpRequest request = new HttpRequest();
        request.setMethod("GET");
        c.setErrorHandler(new Handler<Tuple2<Throwable, Peer>>() {
            @Override
            public void handle(Tuple2<Throwable, Peer> c) {
                erh.handle(c);
                close(c);
            }
        });
        c.connectSync(request, new Handler<HttpURLConnection>() {
            @Override
            public void handle(HttpURLConnection x) {
                suk.handle(x);
                close(c);
                try {
                    x.disconnect();
                    close(x.getInputStream());
                    close(x.getErrorStream());
                    close(x.getOutputStream());
                } catch (Exception e){

                }

            }
        });
    }
}

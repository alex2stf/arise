package com.arise.droid.impl;

import android.content.Context;
import android.media.AudioManager;
import com.arise.astox.net.models.Peer;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.droid.MainActivity;
import com.arise.droid.fragments.BrowserFragment;
import com.arise.droid.fragments.MediaPlaybackFragment;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.model.MediaPlayer;

import java.net.HttpURLConnection;

import static com.arise.core.tools.StringUtil.urlEncodeUTF8;
import static com.arise.core.tools.ThreadUtil.startThread;

public class AndroidMediaPlayer extends MediaPlayer {


    static AndroidMediaPlayer instance = new AndroidMediaPlayer();
    int lV = -99;



    private volatile boolean is_play = false;

    public static MediaPlayer getInstance(){
        return instance;
    }

    @Override
    public Object play(String path, Handler<String> next) {

        MediaPlaybackFragment.playPath(path, next);
        return null;
    }

    @Override
    public void stop(Handler<MediaPlayer> onComplete) {
        final MediaPlayer self = this;
       BrowserFragment.stopWebViewOnMainThread(new Handler() {
           @Override
           public void handle(Object any) {
               is_play = false;
               onComplete.handle(self);
           }
       });
    }

    @Override
    public MediaPlayer setContentInfoProvider(ContentInfoProvider cip) {
        return this;
    }

    @Override
    public void pause() {

    }

    @Override
    public void playStream(String path) {
        if (path.startsWith("http:")) {
            BrowserFragment.openUrlOnMainThread(
                    "http://localhost:8221/proxy-skin?type=radio&uri=" + urlEncodeUTF8(path)
            );
        } else {
            BrowserFragment.openUrlOnMainThread(path);
        }
        is_play = true;
    }

    @Override
    public String setVolume(String mVol) {
        Integer x;
        try {
            x = Integer.valueOf(mVol);
        } catch (Exception e){
            e.printStackTrace();
            return "0";
        }
        if (x == lV){
            return lV + "";
        }

        if (MainActivity.getStaticAppContext() != null){
            AudioManager audioManager = (AudioManager) MainActivity.getStaticAppContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, x, 0);
            lV = x;
            return lV + "";
        }
        return "0";
    }



    @Override
    public String getVolume() {

        if (lV == -99 && MainActivity.getStaticAppContext() != null){
            AudioManager audioManager = (AudioManager) MainActivity.getStaticAppContext().getSystemService(Context.AUDIO_SERVICE);
            lV = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        return lV + "";
    }

    String maxVol = null;

    @Override
    public String getMaxVolume() {
        if (null == maxVol) {
            AudioManager audioManager = (AudioManager) MainActivity.getStaticAppContext().getSystemService(Context.AUDIO_SERVICE);
            maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) + "";
        }
        return maxVol;
    }

    @Override
    public boolean isPlaying() {
        return is_play;
    }

    @Override
    public void validateStreamUrl(String u, Handler<HttpURLConnection> suk, Handler<Tuple2<Throwable, Peer>> erh) {
        String fU = u.trim();

        if (fU.startsWith("html-content:")){
            suk.handle(null);
            return;
        }

        startThread(new Runnable() {
            @Override
            public void run() {
                validateSync(fU, suk, erh);
            }
        }, "validate-stream-" + u);
    }
}

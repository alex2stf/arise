package com.arise.droid.impl;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.os.Looper;

import com.arise.astox.net.clients.JHttpClient;
import com.arise.astox.net.models.Peer;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.Util;
import com.arise.droid.MainActivity;
import com.arise.droid.fragments.BrowserFragment;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.model.MediaPlayer;

import java.net.HttpURLConnection;

public class AndroidMediaPlayer extends MediaPlayer {


    static AndroidMediaPlayer instance = new AndroidMediaPlayer();
    int lV = -99;




    public static MediaPlayer getInstance(){
        return instance;
    }

    @Override
    public Object play(String path, Handler<String> c) {
        c.handle(path);
        return null;
    }

    @Override
    public void stop() {

       BrowserFragment.stopWebViewOnMainThread();
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
        BrowserFragment.openUrlOnMainThread(path);
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

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public void validateStreamUrl(String u, Handler<HttpURLConnection> suk, Handler<Tuple2<Throwable, Peer>> erh) {
        ThreadUtil.startThread(new Runnable() {
            @Override
            public void run() {
                validateSync(u, suk, erh);
            }
        }, "validate-stream-" + u);
    }
}

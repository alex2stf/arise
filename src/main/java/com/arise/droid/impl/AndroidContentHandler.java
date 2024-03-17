package com.arise.droid.impl;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.AppSettings;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;
import com.arise.droid.AppUtil;
import com.arise.droid.MainActivity;
import com.arise.droid.tools.AndroidUrlSolver;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.Message;
import com.arise.weland.impl.RadioPlayer;
import com.arise.weland.model.ContentHandler;
import com.arise.weland.model.MediaPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.arise.droid.AppUtil.OPEN_PATH;

public class AndroidContentHandler extends ContentHandler {


    private final Service service;
    private static final Mole log = Mole.getInstance(AndroidContentHandler.class);


    public AndroidContentHandler(Service service) {
        this.service = service;
    }



    @Override
    public HttpResponse openPath(String path) {
        log.info("received open " + path);


        String preferredYoutubePlayer = AppSettings.getProperty(AppSettings.Keys.PREFERRED_YOUTUBE_PLAYER);

        if (StringUtil.isUrlFormat(path)){
            if ("external".equalsIgnoreCase(preferredYoutubePlayer) || AndroidUrlSolver.canRunInExternalBrowser(path, contentInfoProvider) ) {
                Uri link = Uri.parse(path);
                Intent intent = new Intent(Intent.ACTION_VIEW, link);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                service.startActivity(intent);
                return null;
            }
            else {
                path = AndroidUrlSolver.urlRewrite(path);
            }
        }


        if (!MainActivity.IS_RUNNING ){
            Intent intent = new Intent(service.getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            service.startActivity(intent);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        broadcastOpenPathIntent(service, path);

        return null;
    }

    public static void broadcastOpenPathIntent(Service s, String path){
        Intent brodcastMsg = new Intent();
        brodcastMsg.setAction(OPEN_PATH);
        brodcastMsg.putExtra(AppUtil.PATH, path);
        s.sendBroadcast(brodcastMsg);
    }

    @Override
    protected HttpResponse pause(String path) {
        return stop(path);
    }


    @Override
    public HttpResponse stop(String s) {
        log.info("received stopPreviews " + s);
        broadcastStopIntent(service, s);
        return null;
    }

    public static void broadcastStopIntent(Service s, String p){
        Intent brodcastMsg = new Intent();
        brodcastMsg.setAction("weland.closeFile");
        brodcastMsg.putExtra("path", p);
        s.sendBroadcast(brodcastMsg);
    }


    @Override
    public void onMessageReceived(Message message) {
        log.info("received message " + message);
        Intent brodcastMsg = new Intent();
        brodcastMsg.setAction("onMessage");
        brodcastMsg.putExtra("message", message.toJson());
        service.sendBroadcast(brodcastMsg);

        //TODO save wall messages
    }

    @Override
    public void onPlaylistPlay(String name) {
        log.info("received play playlist " + name);
        Intent brodcastMsg = new Intent();
        brodcastMsg.setAction("weland.onPlaylistPlay");
        brodcastMsg.putExtra("playlistName", name);
        service.sendBroadcast(brodcastMsg);
    }



    @Override
    public MediaPlayer mPlayer() {
        return RadioPlayer.getMediaPlayer();
    }

    @Override
    public RadioPlayer rPlayer() {
        return AppUtil.rPlayer;
    }


    @Override
    public DeviceStat getDeviceStat() {
        DeviceStat deviceStat = DeviceStat.getInstance();

        decorateStandard(deviceStat);

        if (!deviceStat.hasProp("audio.music.volume")){
            deviceStat.setProp("audio.music.volume",  AndroidMediaPlayer.getInstance().getVolume());
        }

        int numberOfCameras = Camera.getNumberOfCameras();
        List<Tuple2<String, String>> ids = new ArrayList<>();
        for (int i = 0; i < numberOfCameras; i++) {
            ids.add(Tuple2.str(i+"", "Cam " + (i + 1)));
        }

//        deviceStat.setProp("audio.music.volume", MainActivity.getMusicVolume() + "");
//        deviceStat.setProp("audio.music.volume.max", MainActivity.getMusicMaxVoume() + "");


        deviceStat.setProp("cams.active.v", "v1");
//        deviceStat.setProp("cams.active.id", CameraLayout.getActiveCameraIndex() + "");
//        deviceStat.setProp("cams.active.run", CameraLayout.isActiveCameraRecordingState() + "" );
//        deviceStat.setProp("flash.modes.active", CameraLayout.getActiveFlashMode() + "" );
        deviceStat.setProp("flash.modes.v1", Arrays.asList(
                Tuple2.str(Camera.Parameters.FLASH_MODE_OFF, "Off"),
                Tuple2.str(Camera.Parameters.FLASH_MODE_TORCH, "Torch"),
                Tuple2.str(Camera.Parameters.FLASH_MODE_RED_EYE, "Red-Eye")
        ));
        deviceStat.setProp("cams.v1",ids);
        return deviceStat;
    }

    @Override
    public void onCloseRequested() {
        try {
            service.stopSelf();
        } catch (Exception e){
            log.info("Failed to onStop service");
        }
        System.exit(0);
    }

    @Override
    public void takeSnapshot(String id) {
        log.info("TODO");
    }



}

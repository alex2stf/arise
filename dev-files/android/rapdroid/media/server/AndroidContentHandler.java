package com.arise.rapdroid.media.server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;

import com.arise.astox.net.models.SingletonHttpResponse;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.AppCache;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;
import com.arise.rapdroid.media.server.fragments.CameraLayout;
import com.arise.rapdroid.net.WavRecorderResponse;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.Message;
import com.arise.weland.model.ContentHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AndroidContentHandler extends ContentHandler {


    private final Service service;
    private static final Mole log = Mole.getInstance(AndroidContentHandler.class);
    private WavRecorderResponse wavRecorderResponse;




    public AndroidContentHandler setWavRecorderResponse(WavRecorderResponse wavRecorderResponse) {
        this.wavRecorderResponse = wavRecorderResponse;
        return this;
    }

    public AndroidContentHandler(Service service) {
        this.service = service;
    }


    @Override
    protected HttpResponse openInfo(ContentInfo info) {
        return openPath(info.getPath());
    }


    public static boolean isAppInstalled(String uri, Context context) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }


    @Override
    public HttpResponse openPath(String path) {
        log.info("received open " + path);


        String preferredYoutubePlayer = AppSettings.getProperty(AppSettings.Keys.PREFERRED_YOUTUBE_PLAYER);

        if (MainActivity.isUrl(path)){
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

        Intent brodcastMsg = new Intent();
        brodcastMsg.setAction("weland.openFile");
        brodcastMsg.putExtra("path", path);
        service.sendBroadcast(brodcastMsg);
        return null;
    }

    @Override
    protected HttpResponse pause(String path) {
        return stop(path);
    }


    @Override
    public HttpResponse stop(String string) {
        log.info("received stopPreviews " + string);
        Intent brodcastMsg = new Intent();
        brodcastMsg.setAction("weland.closeFile");
        brodcastMsg.putExtra("path", string);
        service.sendBroadcast(brodcastMsg);
        return null;
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
    public <T extends SingletonHttpResponse> T getLiveWav() {
        return null;
    }

    @Override
    public DeviceStat getDeviceStat() {
        DeviceStat deviceStat = DeviceStat.getInstance();

        int numberOfCameras = Camera.getNumberOfCameras();
        List<Tuple2<String, String>> ids = new ArrayList<>();
        for (int i = 0; i < numberOfCameras; i++) {
            ids.add(Tuple2.str(i+"", "Cam " + (i + 1)));
        }

        deviceStat.setProp("audio.music.volume", MainActivity.getMusicVolume() + "");
        deviceStat.setProp("audio.music.volume.max", MainActivity.getMusicMaxVoume() + "");


        deviceStat.setProp("cams.active.v", "v1");
        deviceStat.setProp("cams.active.id", CameraLayout.getActiveCameraIndex() + "");
        deviceStat.setProp("cams.active.run", CameraLayout.isActiveCameraRecordingState() + "" );
        deviceStat.setProp("flash.modes.active", CameraLayout.getActiveFlashMode() + "" );
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

    @Override
    public void takeSnapshot() {
        Intent brodcastMsg = new Intent();
        brodcastMsg.setAction("weland.onDeviceUpdate");
        brodcastMsg.putExtra("takeSnapshot", new Date() + "");
        service.sendBroadcast(brodcastMsg);
    }


    @Override
    public DeviceStat onDeviceUpdate(Map params) {

        int now = (int) System.currentTimeMillis();
        int lastUpdate = AppCache.getInt("crmfx", (now - 3004));
        int diff = now - lastUpdate;
        if (diff < 3000) {
            log.info("Need to wait more than " + (3000 - diff) + " miliseconds");
            return getDeviceStat();
        }

        AppCache.putInt("crmfx", (int) System.currentTimeMillis());

        String musicVolume = MapUtil.findQueryParamString(params, "musicVolume");
        String camId = MapUtil.findQueryParamString(params, "camId");
        String camEnabled = MapUtil.findQueryParamString(params, "camEnabled");
        String lightMode = MapUtil.findQueryParamString(params, "lightMode");


        Intent brodcastMsg = new Intent();
        brodcastMsg.setAction("weland.onDeviceUpdate");
        brodcastMsg.putExtra("camId", camId);
        brodcastMsg.putExtra("camEnabled", camEnabled + "");
        brodcastMsg.putExtra("lightMode", lightMode);
        brodcastMsg.putExtra("musicVolume", musicVolume);
        service.sendBroadcast(brodcastMsg);




        if ("false".equalsIgnoreCase(camEnabled) && wavRecorderResponse != null) {
            try {
                wavRecorderResponse.stopRecording();
            }catch (Exception e){

            }
        }
        DeviceStat deviceStat = getDeviceStat();

        if (StringUtil.hasText(musicVolume)){
            try {
                Integer.valueOf(musicVolume);
                deviceStat.setProp("audio.music.volume",  musicVolume);
            }catch (Exception e){

            }
        }




        return deviceStat;
    }




}

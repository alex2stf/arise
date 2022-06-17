package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpResponse;
import com.arise.canter.CommandRegistry;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.Message;
import com.arise.weland.model.ContentHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.arise.core.tools.StringUtil.hasText;
import static com.arise.weland.dto.DeviceStat.getInstance;

//import org.openqa.selenium.WebDriver;

public class DesktopContentHandler extends ContentHandler {

    private static final Mole log = Mole.getInstance(DesktopContentHandler.class);

    private final CommandRegistry cmdReg;

    Set<String> exes = new HashSet<>();

    private final MediaPlayer mediaPlayer;
    public DesktopContentHandler(ContentInfoProvider contentInfoProvider, CommandRegistry cmdReg) {
        this.contentInfoProvider = contentInfoProvider;
        this.cmdReg = cmdReg;
        mediaPlayer = RadioPlayer.getMediaPlayer();
    }



    public HttpResponse openPath(final String path) {
        return openString(path);
    }



    @Override
    protected HttpResponse pause(String path) {
        mediaPlayer.pause();
        return HttpResponse.oK();
    }

    //TODO fa stopPreviews mai destept
    private HttpResponse openString(final String path){
           log.info("OPEN " + path);
           DeviceStat deviceStat = getInstance();

            if (isHttpPath(path)){
                System.out.println(path);
            }
            else if (isPicture(path)){
                openPicture(path);
            }
            else if (isMedia(path)){
                mediaPlayer.play(path);
            }
            else {
                SYSUtils.open(path);
            }
            return deviceStat.toHttp();
    }







    private boolean isMedia(String path) {
        return ContentType.isMusic(path) || ContentType.isVideo(path);
    }



    private boolean isPicture(String path){
        return ContentType.isPicture(path);
    }

    public void openPicture(String path){
        if (SYSUtils.isWindows()){

            //TODO use jframe
            SYSUtils.Result result = SYSUtils.exec("C:\\Windows\\System32\\rundll32.exe", "C:\\Program Files\\Windows Photo Viewer\\PhotoViewer.dll", path);
        }
        else {
            throw new RuntimeException("TODO");
        }
    }













    @Override
    public HttpResponse stop(String x) {
        log.info("STOP " + x);
        mediaPlayer.stop();
        return null;
    }




    @Override
    public void onMessageReceived(Message message) {

    }

    @Override
    public void onPlaylistPlay(String name) {
        System.out.println("TO DO");
    }




    @Override
    public DeviceStat onDeviceUpdate(Map<String, List<String>> p) {

        DeviceStat deviceStat = getDeviceStat();


        String mVol = null;
        if(p.containsKey("musicVolume")) {
            mVol = p.get("musicVolume").get(0);
        }

        if(hasText(mVol)) {
            mVol = mediaPlayer.setVolume(mVol);
            getInstance().setProp("audio.music.volume", mVol);
        }
        else {
            mVol = mediaPlayer.getVolume();
            if (StringUtil.hasText(mVol)){
                getInstance().setProp("audio.music.volume", mVol);
            }
        }




        return deviceStat;
    }



    @Override
    public DeviceStat getDeviceStat() {
        final DeviceStat deviceStat = getInstance();

        JARProxies.getCamIds(new Handler<List<Tuple2<String, String>>>() {
            @Override
            public void handle(List<Tuple2<String, String>> camIds) {
                deviceStat.setProp("cams.v1", camIds);
            }
        });



        deviceStat.setProp("cams.active.v", "v1");
        deviceStat.setProp("cams.active.id", "0");
        deviceStat.setProp("flash.modes.active", "off" );

        deviceStat.setProp("flash.modes.v1", Arrays.asList(new Tuple2<>("0", "ON"), new Tuple2<>("1", "OFF")));



        return deviceStat;
    }

    @Override
    public void onCloseRequested() {
        System.exit(0);
    }

    @Override
    public void takeSnapshot(String id) {
       JARProxies.takeSnapshot(id);
    }







}

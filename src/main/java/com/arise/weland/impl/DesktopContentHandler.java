package com.arise.weland.impl;

import com.arise.astox.net.models.SingletonHttpResponse;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.canter.CommandRegistry;
import com.arise.core.AppSettings;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.Message;
import com.arise.weland.model.ContentHandler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.arise.core.AppSettings.Keys.TAKE_SNAPSHOT_CMD;
import static com.arise.core.tools.StringUtil.hasText;

//import org.openqa.selenium.WebDriver;

public class DesktopContentHandler extends ContentHandler {

    private static final Mole log = Mole.getInstance(DesktopContentHandler.class);

    private final CommandRegistry cmdRegistry;

    Set<String> exes = new HashSet<>();
    private DesktopCamStream desktopCamStream;

    private final MediaPlayer mediaPlayer;
    public DesktopContentHandler(ContentInfoProvider contentInfoProvider, CommandRegistry cmdRegistry) {
        this.contentInfoProvider = contentInfoProvider;
        this.cmdRegistry = cmdRegistry;
        mediaPlayer = RadioPlayer.getMediaPlayer();
    }






    @Override
    public HttpResponse openInfo(ContentInfo info) {
        openString(info.getPath());
        return null;
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
           DeviceStat deviceStat = DeviceStat.getInstance();

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

    private boolean isInternal(String path) {
        return path.startsWith("{host}");
    }

    private String fix(String data){
        return  "http://localhost:8221" + data.substring("{host}".length());
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
    public <T extends SingletonHttpResponse> T getLiveWav() {
        return null;
    }



    @Override
    public DeviceStat onDeviceUpdate(Map<String, List<String>> params) {

        DeviceStat deviceStat = getDeviceStat();



        String volumeStr = null;
        if(params.containsKey("musicVolume")) {
            volumeStr = params.get("musicVolume").get(0);
        }

        if(hasText(volumeStr)) {
            if(cmdRegistry.containsCommand("set-master-volume")){
                String v = "" + cmdRegistry.execute("set-master-volume", new String[]{volumeStr});
                deviceStat.setProp("audio.music.volume", v);
            }
        }
        else if(cmdRegistry.containsCommand("get-master-volume")){
            String x = cmdRegistry.execute("get-master-volume", new String[]{}) + "";
            deviceStat.setProp("audio.music.volume", x);
        }




        return deviceStat;
    }



    @Override
    public DeviceStat getDeviceStat() {
        DeviceStat deviceStat = DeviceStat.getInstance();

        deviceStat.setProp("cams.active.v", "v1");
        deviceStat.setProp("cams.active.id", "0");
        deviceStat.setProp("cams.active.run", desktopCamStream.isRunning()  + "" );
        deviceStat.setProp("flash.modes.active", "off" );

        deviceStat.setProp("flash.modes.v1", Arrays.asList(new Tuple2<>("0", "ON"), new Tuple2<>("1", "OFF")));
        deviceStat.setProp("cams.v1", Arrays.asList(Tuple2.str("0", "Cam 1"), Tuple2.str("1", "Cam 2")));


        return deviceStat;
    }

    @Override
    public void onCloseRequested() {
        System.exit(0);
    }

    @Override
    public void takeSnapshot() {
        if(AppSettings.isDefined(TAKE_SNAPSHOT_CMD)){
            String cmd = AppSettings.getProperty(TAKE_SNAPSHOT_CMD);
            SYSUtils.exec(cmd.split(" "));
        }

    }

    private void execute(String args[]){
        log.info(StringUtil.join(args, " "));
        exes.add(new File(args[0]).getName());
        try {
            Runtime.getRuntime().exec(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setCameraStream(DesktopCamStream desktopCamStream) {
        this.desktopCamStream = desktopCamStream;
    }
}

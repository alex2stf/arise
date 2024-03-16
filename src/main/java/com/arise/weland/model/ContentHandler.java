package com.arise.weland.model;

import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.AppSettings;
import com.arise.core.models.Handler;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.Message;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.RadioPlayer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.arise.core.tools.SYSUtils.getLinuxDetails;
import static com.arise.core.tools.StringUtil.hasText;
import static com.arise.core.tools.ThreadUtil.sleep;
import static com.arise.weland.dto.DeviceStat.getInstance;

public abstract  class ContentHandler {
    protected ContentInfoProvider contentInfoProvider;

    public ContentHandler setContentInfoProvider(ContentInfoProvider contentInfoProvider) {
        this.contentInfoProvider = contentInfoProvider;
        return this;
    }

    public final HttpResponse openRequest(HttpRequest request){
        String path = request.getQueryParam("path");
        return openPath(ContentInfo.decodePath(path));
    }

    public abstract HttpResponse openPath(String path);
    protected abstract HttpResponse pause(String path);

    public  HttpResponse pauseRequest(HttpRequest request){
        return pause(request.getQueryParam("path"));
    }

    public HttpResponse stop(HttpRequest request){
        return stop(request.getQueryParam("path"));
    }



    public boolean isLocalFileRadioFormat(String in){
        return in.startsWith("file:");
    }

    public abstract HttpResponse stop(String x);

    public HttpResponse stop(ContentInfo info){
        return stop(info.getPath());
    }

    public abstract void onMessageReceived(Message message);

    public abstract void onPlaylistPlay(String name);


    public abstract MediaPlayer mPlayer();

    public abstract RadioPlayer rPlayer();

    public DeviceStat onDeviceUpdate(Map<String, List<String>> p) {

        DeviceStat deviceStat = getDeviceStat();


        String mVol = null;
        if(p.containsKey("musicVolume")) {
            mVol = p.get("musicVolume").get(0);
        }

        if(hasText(mVol)) {
            mVol = mPlayer().setVolume(mVol);
            getInstance().setProp("audio.music.volume", mVol);
        }
        else {
            mVol = mPlayer().getVolume();
            if (StringUtil.hasText(mVol)){
                getInstance().setProp("audio.music.volume", mVol);
            }
        }


        if (rPlayer() != null && p.containsKey("rplayer") ){
            String x = p.get("rplayer").get(0);

            if ("play".equalsIgnoreCase(x)){
                if (mPlayer().isPlaying()){
                    mPlayer().stop(null);
                    sleep(1000 * 8);
                }
                rPlayer().play();
                deviceStat.setProp("rplayer.play", "true");
            }
            if ("stop".equalsIgnoreCase(x)){
                rPlayer().stop();
                deviceStat.setProp("rplayer.play", "false");
            }


        }



        return deviceStat;
    }


    protected void decorateStandard(DeviceStat dS) {
        if(rPlayer() != null) {
            dS.setProp("rplayer.path", rPlayer().getCurrentPath());

            RadioPlayer.Show sh = rPlayer().getActiveShow();
            if (sh != null) {
                dS.setProp("rplayer.show.name", rPlayer().getActiveShow().name());
            }
        }
    }

    public abstract DeviceStat getDeviceStat();

    public abstract void onCloseRequested();


    private void append(StringBuilder sb, String key, String value){

        sb.append(StringUtil.jsonVal(key)).append(":")
                .append(StringUtil.jsonVal(value))
                .append(",");
    }


    private void readProperties(String prefix, StringBuilder sb, Properties p){
        final Set<String> keys = p.stringPropertyNames();
        for (final String key : keys) {
            append(sb, prefix + key, p.getProperty(key));
        }
    }

    static String deviceInfoJson = null;

    public final String getDeviceInfoJson() {
        if(deviceInfoJson != null){
            return deviceInfoJson;
        }
        final Properties sp = System.getProperties();

        StringBuilder sb = new StringBuilder().append("{");

        append(sb, "_util.is_raspberry_pi", SYSUtils.isRaspberryPI() + "");
        append(sb, "_util.is_android", SYSUtils.isAndroid() + "");
        append(sb, "_util.is_windows", SYSUtils.isWindows()+ "");
        append(sb, "_util.is_mac", SYSUtils.isMac()+ "");
        append(sb, "_util.is_unix", SYSUtils.isUnix()+ "");
        append(sb, "_util.is_32_bits", SYSUtils.is32Bits()+ "");
        append(sb, "_util.device_name", SYSUtils.getDeviceName()+ "");
        append(sb, "_util.device_id", SYSUtils.getDeviceId());


        readProperties("_sys.", sb, sp);
        readProperties("_app.", sb, AppSettings.getApplicationProperties());

        Properties x = getLinuxDetails();
        if(x != null){
            readProperties("_lnx.", sb, x);
        }

        Map<String, String> env = System.getenv();

        for(Map.Entry<String, String> e : env.entrySet()){
            append(sb, "_env." + e.getKey(), e.getValue());
        }

        sb.append("\"___eof\"").append(":").append("0");
        sb.append("}");
        deviceInfoJson = sb.toString();

        return deviceInfoJson;
    }



//    public HttpResponse getLatestSnapshot() {
//
//        File file;
//
//        if(AppSettings.isDefined(LATEST_SNAPSHOT_PATH)){
//            file = new File(AppSettings.getProperty(LATEST_SNAPSHOT_PATH));
//        } else {
//            file = new File(FileUtil.findAppDir(), "snapshot.jpeg");
//        }
//
//        HttpResponse response = new HttpResponse();
//
//        byte[] bytes;
//        try {
//            bytes = FileUtil.readBytes(file);
//        } catch (IOException e) {
//            bytes = new byte[0];
//        }
//        response.setBytes(bytes)
//                .setContentType(ContentType.IMAGE_JPEG);
//        return response;
//
//    }

    public abstract void takeSnapshot(String id);




}

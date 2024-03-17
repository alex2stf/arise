package com.arise.weland.desk;

import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.Message;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.JARProxies;
import com.arise.weland.impl.RadioPlayer;
import com.arise.weland.model.ContentHandler;
import com.arise.weland.model.MediaPlayer;
import com.arise.weland.ui.WelandForm;

import java.util.List;

import static com.arise.core.tools.ContentType.isHttpPath;
import static com.arise.core.tools.ContentType.isMedia;
import static com.arise.core.tools.ThreadUtil.sleep;
import static com.arise.weland.dto.DeviceStat.getInstance;


public class DesktopContentHandler extends ContentHandler {

    private static final Mole log = Mole.getInstance(DesktopContentHandler.class);


    private final MediaPlayer deskMPlayer;
    private RadioPlayer rplayer;
    private com.arise.weland.ui.WelandForm _f;


    public DesktopContentHandler(ContentInfoProvider cip) {
        this.contentInfoProvider = cip;
        deskMPlayer = RadioPlayer.getMediaPlayer().setContentInfoProvider(cip);
    }

    public DesktopContentHandler setRadioPlayer(RadioPlayer r){
        this.rplayer = r;
        return this;
    }



    public HttpResponse openPath(final String path) {
        return openString(path);
    }



    @Override
    protected HttpResponse pause(String path) {
        deskMPlayer.pause();
        return HttpResponse.oK();
    }

    //TODO fa stopPreviews mai destept
    private HttpResponse openString(final String path){
        log.info("OPEN " + path);
        DeviceStat deviceStat = getInstance();

        if (rplayer != null && rplayer.isPlaying()){

            if(isHttpPath(path) || isLocalFileRadioFormat(path) || isMedia(path)) {
                log.info("RESTARTING RADIO for " + path);
                System.setProperty("radio.forced.path", path);
                ThreadUtil.delayedTask(new Runnable() {
                    @Override
                    public void run() {
                        rplayer.restart();
                    }
                }, 1000 * 3);
                return deviceStat.toHttp();
            } else {
                rplayer.stop();
            }
        }



        if (isHttpPath(path)){
            ContentInfo info = contentInfoProvider.findByPath(path);
            if (info != null && info.isStream() || path.indexOf("youtube") > -1){
                deskMPlayer.stop(new Handler<MediaPlayer>() {
                    @Override
                    public void handle(MediaPlayer mediaPlayer) {

                    }
                });
                sleep(1000 * 8);
                if (_f != null) {
                    ThreadUtil.delayedTask(new Runnable() {
                        @Override
                        public void run() {
                            _f.toFront();
                            _f.repaint();
                        }
                    }, 12 * 1000);

                }
                deskMPlayer.playStream(path);
                return deviceStat.toHttp();
            } else {
                System.out.println("WTF????");
            }
        }
        else if (isPicture(path)){
            openPicture(path);
        }
        else if (isMedia(path)){
            deskMPlayer.stop(null);
            if (_f != null) {
                _f.toFront();
            }
            deskMPlayer.play(path);
        }
        else {
            SYSUtils.open(path);
        }
        return deviceStat.toHttp();
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
        deskMPlayer.stop(null);
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
    public MediaPlayer mPlayer() {
        return deskMPlayer;
    }

    @Override
    public RadioPlayer rPlayer() {
        return rplayer;
    }


    @Override
    public DeviceStat getDeviceStat() {
        final DeviceStat dS = getInstance();

        JARProxies.getCamIds(new Handler<List<Tuple2<String, String>>>() {
            @Override
            public void handle(List<Tuple2<String, String>> camIds) {
                dS.setProp("cams.v1", camIds);
            }
        });

        decorateStandard(dS);
//        dS.setProp("cams.active.v", "v1");
//        dS.setProp("cams.active.id", "0");
//        dS.setProp("flash.modes.active", "off" );
//
//        dS.setProp("flash.modes.v1", Arrays.asList(new Tuple2<>("0", "ON"), new Tuple2<>("1", "OFF")));
        return dS;
    }



    @Override
    public void onCloseRequested() {
        System.exit(0);
    }

    @Override
    public void takeSnapshot(String id) {
       JARProxies.takeSnapshot(id);
       if (_f != null) {
           _f.refreshSnapshot();
       }
    }


    public void setForm(WelandForm _f) {
        this._f = _f;
    }
}

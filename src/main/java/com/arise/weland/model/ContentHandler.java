package com.arise.weland.model;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.SingletonHttpResponse;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.tools.MapUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.Detail;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.Message;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.utils.JPEGOfferResponse;
import com.arise.weland.utils.MJPEGResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract  class ContentHandler {
    protected ContentInfoProvider contentInfoProvider;

    MJPEGResponse mjpegResponse = new MJPEGResponse();
    JPEGOfferResponse jpegOfferResponse = new JPEGOfferResponse();



    public ContentHandler setContentInfoProvider(ContentInfoProvider contentInfoProvider) {
        this.contentInfoProvider = contentInfoProvider;
        return this;
    }

    public final HttpResponse openRequest(HttpRequest request){
        String path = request.getQueryParam("path");
        return openPath(ContentInfo.decodePath(path));
    }

    protected abstract HttpResponse openInfo(ContentInfo info);
    public abstract HttpResponse openPath(String path);
    protected abstract HttpResponse pause(String path);

    public  HttpResponse pauseRequest(HttpRequest request){
        return pause(request.getQueryParam("path"));
    }

    public HttpResponse stop(HttpRequest request){
        return stop(request.getQueryParam("path"));
    }

    public boolean isHttpPath(String in){
        try {
            URL uri = new URL(in);
            return uri != null
                    && (uri.getProtocol().startsWith("http") );
        }  catch (MalformedURLException e) {
            return false;
        }
    }

    public abstract HttpResponse stop(String string);

    public HttpResponse stop(ContentInfo info){
        return stop(info.getPath());
    }


    public abstract void onMessageReceived(Message message);

    public abstract void onPlaylistPlay(String name);

//    public abstract List<Detail> getCameraIdsV1();
//
//    public abstract List<Detail> getFlashModesV1();

    public final MJPEGResponse getLiveMjpegStream(){
        return mjpegResponse;
    };

    public final JPEGOfferResponse getLiveJpeg() {
        return jpegOfferResponse;
    }

    public abstract <T extends SingletonHttpResponse> T getLiveWav();


    public abstract DeviceStat onDeviceUpdate(Map<String, List<String>> params);

    public abstract DeviceStat getDeviceStat();

    public abstract void onCloseRequested();
}

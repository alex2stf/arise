package com.arise.weland.model;

import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.Message;
import com.arise.weland.impl.ContentInfoProvider;

import java.net.MalformedURLException;
import java.net.URL;

public abstract  class ContentHandler {
    protected ContentInfoProvider contentInfoProvider;

    public ContentHandler setContentInfoProvider(ContentInfoProvider contentInfoProvider) {
        this.contentInfoProvider = contentInfoProvider;
        return this;
    }

    public final HttpResponse openRequest(HttpRequest request){
        String path = request.getQueryParam("path");
        if (!isHttpPath(path)) {
            ContentInfo info = contentInfoProvider.findByPath(path);
            if (info != null) {
                return openInfo(info);
            }
        }
        return openPath(path);
    }


    protected abstract HttpResponse openInfo(ContentInfo info);
    protected abstract HttpResponse openPath(String path);

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


//    public HttpResponse open(ContentInfo info){
//        return open(info.getPath());
//    }


    public HttpResponse stop(ContentInfo info){
        return stop(info.getPath());
    }


    public abstract void onMessageReceived(Message message);

//    public HttpResponse play(HttpRequest request, String mode){
//        return play(request.getQueryParam("path"), mode.equalsIgnoreCase("native") ? Mode.NATIVE : Mode.EMBEDDED);
//    }

//    protected abstract HttpResponse play(String path, Mode mode);



//    public enum Mode {
//        NATIVE, EMBEDDED;
//    }
}

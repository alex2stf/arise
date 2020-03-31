package com.arise.weland.model;

import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.Message;
import com.arise.weland.impl.ContentInfoProvider;

public abstract  class ContentHandler {
    protected ContentInfoProvider contentInfoProvider;

    public ContentHandler setContentInfoProvider(ContentInfoProvider contentInfoProvider) {
        this.contentInfoProvider = contentInfoProvider;
        return this;
    }

    public HttpResponse play(HttpRequest request){
        return open(request.getQueryParam("path"));
    }

    protected abstract HttpResponse open(String path);

    public HttpResponse stop(HttpRequest request){
        return stop(request.getQueryParam("path"));
    }

    public HttpResponse pause(HttpRequest request){
        return pause(request.getQueryParam("path"));
    }

    public abstract HttpResponse stop(String string);
    public abstract HttpResponse pause(String string);


    public HttpResponse open(ContentInfo info){
        return open(info.getPath());
    }
    public HttpResponse stop(ContentInfo info){
        return stop(info.getPath());
    }
    public HttpResponse pause(ContentInfo info){
        return pause(info.getPath());
    }

    public abstract void onMessageReceived(Message message);

    public HttpResponse play(HttpRequest request, String mode){
        return play(request.getQueryParam("path"), mode.equalsIgnoreCase("native") ? Mode.NATIVE : Mode.EMBEDDED);
    }

    protected abstract HttpResponse play(String path, Mode mode);



    public enum Mode {
        NATIVE, EMBEDDED;
    }
}

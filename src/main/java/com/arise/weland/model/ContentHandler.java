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
        return play(request.getQueryParam("path"));
    }

    public HttpResponse stop(HttpRequest request){
        return stop(request.getQueryParam("path"));
    }

    public HttpResponse pause(HttpRequest request){
        return pause(request.getQueryParam("path"));
    }

    public abstract HttpResponse play(String string);
    public abstract HttpResponse stop(String string);
    public abstract HttpResponse pause(String string);
    public HttpResponse play(ContentInfo info){
        return play(info.getPath());
    }
    public HttpResponse stop(ContentInfo info){
        return stop(info.getPath());
    }
    public HttpResponse pause(ContentInfo info){
        return pause(info.getPath());
    }

    public abstract void onMessageReceived(Message message);
}

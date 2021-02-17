package com.arise.weland.model;

import com.arise.astox.net.models.AbstractServer;
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
        //if (!isHttpPath(path)) {
         //   ContentInfo info = contentInfoProvider.findByPath(path);
          //  if (info != null) {
            //    return openInfo(info);
            //}
        //}
        
        
        return openPath(ContentInfo.decodePath(path));
    }

    protected abstract HttpResponse openInfo(ContentInfo info);
    protected abstract HttpResponse openPath(String path);
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


}

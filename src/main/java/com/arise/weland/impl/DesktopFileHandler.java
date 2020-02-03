package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.canter.Command;
import com.arise.canter.Registry;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.model.ContentHandler;
import com.arise.weland.utils.URLBeautifier;
import com.arise.weland.utils.WelandServerHandler;

import java.io.File;

import java.net.URI;
import java.net.URISyntaxException;

public class DesktopFileHandler extends ContentHandler {

    private final ContentInfoProvider contentInfoProvider;
    private final Registry registry;


    public DesktopFileHandler(ContentInfoProvider contentInfoProvider, Registry registry) {

        this.contentInfoProvider = contentInfoProvider;
        this.registry = registry;
    }


    public HttpResponse play(String path) {

        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {

                System.out.println("OPEN " + path);

                if (isHttpPath(path)){
                    openUrlInBrowser(path);
                    return;
                }
                if (isPicture(path)){
                    openPicture(path);
                    return;
                }

                if (isMedia(path)){
                    openMedia(path);
                    return;
                }
                SYSUtils.open(path);
                return;
            }
        });
        return null;
    }

    private void openMedia(String path) {

        ContentInfo info = contentInfoProvider.findByPath(path);
        if (info != null){
            VLCPlayer.getInstance().play(info);
        }
        else {
            VLCPlayer.getInstance().play(path);
        }
    }

    private boolean isMedia(String path) {
        return ContentType.isMusic(path) || ContentType.isVideo(path);
    }

    private boolean isHttpPath(String in){
        try {
            URI uri = new URI(in);
            return uri != null && uri.getScheme().startsWith("http");
        } catch (URISyntaxException e) {
            return false;
        }
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

    public void openUrlInBrowser(String urlPath){

        //TODO default bahaviour when no command registry
        String args[] = new String[]{URLBeautifier.beautify(urlPath)};
        registry.getCommand("browserOpen").execute(args);
    }



    @Override
    public HttpResponse stop(String string) {
        VLCPlayer.getInstance().stop();
        return null;
    }

    @Override
    public HttpResponse pause(String string) {
        return null;
    }
}

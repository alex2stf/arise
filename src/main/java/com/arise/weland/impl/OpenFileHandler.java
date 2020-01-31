package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.canter.Command;
import com.arise.canter.Registry;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.utils.URLBeautifier;
import com.arise.weland.utils.WelandServerHandler;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class OpenFileHandler implements WelandServerHandler.Handler<HttpRequest> {

    private final ContentInfoProvider contentInfoProvider;
    private final Registry registry;


    public OpenFileHandler(ContentInfoProvider contentInfoProvider, Registry registry) {

        this.contentInfoProvider = contentInfoProvider;
        this.registry = registry;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {

        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                String path = request.getQueryParam("path");
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
                SYSUtils.open(request.getQueryParam("path"));
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
}

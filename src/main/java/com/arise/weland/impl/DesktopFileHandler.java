package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpResponse;
import com.arise.canter.Command;
import com.arise.canter.Registry;
import com.arise.cargo.management.Dependencies;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.Message;
import com.arise.weland.model.ContentHandler;
import com.arise.weland.utils.URLBeautifier;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DesktopFileHandler extends ContentHandler {

    private final ContentInfoProvider contentInfoProvider;
    private final Registry registry;

    private static final Mole log = Mole.getInstance(DesktopFileHandler.class);
    private static File nwjsExe;
    static {
        try {
            File nwjsDir = DependencyManager.solve(Dependencies.NWJS_0_12_0);
            nwjsExe = new File(nwjsDir, "nw.exe");
            if (!nwjsExe.exists()){
                log.error("Unable to solve NWJS");
            }
        } catch (IOException e) {
            System.exit(-1);
        }
    }

    public DesktopFileHandler(ContentInfoProvider contentInfoProvider, Registry registry) {
        this.contentInfoProvider = contentInfoProvider;
        this.registry = registry;
    }


    public HttpResponse play(final String path) {

        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {

                log.info("PLAY " + path);

                if (isHttpPath(path)){
                    openUrl(path);
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
        closeSpawnedProcesses();
        File local = new File(path);
        if (local.exists()){
            ContentInfo info = contentInfoProvider.findByPath(local.getAbsolutePath().replaceAll("\\\\", "/"));
            if (info != null){
                VLCPlayer.getInstance().play(info);
            }
            else {
                VLCPlayer.getInstance().play(path);
            }
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



    public void openUrl(String urlPath){
        if (shouldUseNwjs(urlPath)){
            String url = URLBeautifier.beautify(urlPath);
            File outputDir = new File(contentInfoProvider.getDecoder().getStateDirectory(), "nw-current-app");
            if (!outputDir.exists() && !outputDir.mkdirs()){
                log.warn("Failed to create app dir " + outputDir.getAbsolutePath());
                openInStandardBrowser(url);
                return;
            }
            openInNwjs(url, outputDir);
        }
        else {
           openInStandardBrowser(urlPath);
        }


    }

    private boolean shouldUseNwjs(String path){
        return path.toLowerCase().indexOf("youtube") > -1 && nwjsExe.exists();
    }

    private void openInNwjs(String url, File outputDir){
        String packageContent = "{\n" +
                "  \"name\": \"player\",\n" +
                "  \"main\": \""+url+"\",\n" +
                "  \"inject-js-end\": \"main.js\",\n" +
                "  \"window\": {\n" +
//                "    \"title\": \"Nws player\",\n" +
//                "    \"toolbar\": false,\n" +
//                "    \"frame\": true,\n" +
                "    \"fullscreen\": false\n" +
                "  }\n" +
                "}";

        String mainInject = StreamUtil.toString( FileUtil.findStream("src/main/resources#weland/main.js"));
        FileUtil.writeStringToFile(new File(outputDir, "package.json"), packageContent);
        FileUtil.writeStringToFile(new File(outputDir, "main.js"), mainInject);
        execute(new String[]{
                nwjsExe.getAbsolutePath(), outputDir.getAbsolutePath()
        });
    }

    private void openInStandardBrowser(String path){
        Command command = registry.getCommand("browserOpen");
        Object binaries = command.getProperty("binaries");
        if (binaries != null && binaries instanceof Map){
            try {
                Map<String, String> bins = (Map<String, String>) binaries;
                for (Map.Entry<String, String> e: bins.entrySet()){
                    File f = new File(e.getValue());
                    if (f.exists()){
                        execute(new String[]{f.getAbsolutePath(), path});
                        return;
                    }
                }
            } catch (Exception e){
                log.error("openInBrowser failed because ", e);
            }
        }

        log.error("NO browser exe detected");
    }



    @Override
    public HttpResponse stop(String x) {
        log.info("STOP " + x);
        VLCPlayer.getInstance().stop();
        closeSpawnedProcesses();
        return null;
    }

    volatile boolean clearing = false;
    private void closeSpawnedProcesses(){
        if (clearing || exes.isEmpty()) {
            return;
        }
        synchronized (exes){
            if (clearing) {
                return;
            }
            clearing = true;
            for (String s: exes){
                try {
                    if (SYSUtils.isWindows()){
                        String args[] = new String[]{"taskkill", "/IM", s};
                        log.info(StringUtil.join(args, " "));
                        Runtime.getRuntime().exec(args);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            exes.clear();
            clearing = false;
        }
    }

    @Override
    public HttpResponse pause(String string) {
        return null;
    }

    @Override
    public void onMessageReceived(Message message) {
        System.out.println("RECEIVED MESSAGE " + message);
    }


    Set<String> exes = new HashSet<>();

    private void execute(String args[]){
        log.info(StringUtil.join(args, " "));
        exes.add(new File(args[0]).getName());
        try {
            Runtime.getRuntime().exec(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}

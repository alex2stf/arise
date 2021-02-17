package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpResponse;
import com.arise.canter.Registry;
import com.arise.cargo.management.Dependencies;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.Arr;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapObj;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.Message;
import com.arise.weland.dto.Playlist;
import com.arise.weland.model.ContentHandler;
import com.arise.weland.utils.URLBeautifier;
import com.arise.weland.wrappers.VLCWrapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DesktopFileHandler extends ContentHandler {

    private static final Mole log = Mole.getInstance(DesktopFileHandler.class);
    private static final String CURRENT_RUNNING = "cbin";
    static boolean nwjsEnabled = !"false".equalsIgnoreCase(System.getProperty("nwjs.enabled"));
    private static File nwjsExe;

    static {
        try {
            DependencyManager.Resolution resolution = DependencyManager.solve(Dependencies.NWJS_0_12_0);
            if (resolution != null){
                File nwjsDir = DependencyManager.solve(Dependencies.NWJS_0_12_0).uncompressed();
                nwjsExe = new File(nwjsDir, "nw.exe");
                if (!nwjsExe.exists()){
                    log.error("Unable to solve NWJS");
                    nwjsEnabled = false;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            nwjsEnabled = false;
        }
    }

    private final ContentInfoProvider contentInfoProvider;
    private final Registry registry;
    boolean fullScreenNwjs = "true".equalsIgnoreCase(System.getProperty("nwjs.fullscreen"));
    MapObj commands;
    volatile boolean clearing = false;
    JFrame nf = null;
    JLabel label = null;
    ThreadUtil.TimerResult result;
    Set<String> exes = new HashSet<>();


    public DesktopFileHandler(ContentInfoProvider contentInfoProvider, Registry registry) {
        this.contentInfoProvider = contentInfoProvider;
        this.registry = registry;
        setupArgs();
    }

    public static void main(String[] args) {
        Message message = new Message();
        message.setText("some text");
        DesktopFileHandler f = new DesktopFileHandler(null, null);
                f.onMessageReceived(message);
        message.setText("some text2");
                f.onMessageReceived(message);
    }

    private void setupArgs() {
        String s = StreamUtil.toString(
                FileUtil.findStream("weland/config/commons/executables.json")
        ).replaceAll("\\s+", " ");

        commands = (MapObj) Groot.decodeBytes(s);
    }


    private ThreadUtil.TimerResult timerResult = null;

    @Override
    public HttpResponse openInfo(ContentInfo info) {
        ThreadUtil.closeTimer(timerResult);
        openString(info.getPath());
        return null;
    }



    public HttpResponse openPath(final String path) {
        ThreadUtil.closeTimer(timerResult);
//        stop("-");
        return openString(path);

    }



    @Override
    protected HttpResponse pause(String path) {
        VLCWrapper.pauseHttp();
        return HttpResponse.oK();
    }

    //TODO fa stop mai destept
    private HttpResponse openString(final String path){
            log.info("OPEN " + path);
           DeviceStat deviceStat = DeviceStat.getInstance();
           deviceStat.setProp("ks", "false");

            if (path.endsWith("package-info.json")){
                Object[] args = ContentInfoProvider.decodePackageInfo(new File(path));
                if (args != null){
                    ContentInfo contentInfo = (ContentInfo) args[0];
                    //TODO use AppSettings and package info settings
                    String host = "http://" + VLCWrapper.VLC_HTTP_HOST + ":8221/pack?root=" + contentInfo.getPath();
                    openUrl(host);
                    deviceStat.setProp("ks", "true");
                }
            }

            else if (isInternal(path)){
                openUrl(fix(path));
            }
            else if (isHttpPath(path)){
                openUrl(URLBeautifier.getFixedUri(path));
            }
            else if (isPicture(path)){
                openPicture(path);
            }
            else if (isMedia(path)){
                openMedia(path);
            }
            else {
                SYSUtils.open(path);
            }
            return deviceStat.toHttp();
    }

    private boolean isInternal(String path) {
        return path.startsWith("{host}");
    }

    private String fix(String data){
        return  "http://localhost:8221" + data.substring("{host}".length());
    }


    private void openMedia(String path) {
        log.info("received request to open ", path);
        File vlcExecutable = VLCWrapper.open(getCommands("media", path));
        if (vlcExecutable != null){
            exes.add(vlcExecutable.getAbsolutePath());
            DeviceStat.getInstance().setProp(CURRENT_RUNNING, "vlc");
        } else {
            execute(getCommands("media", path));
        }
    }

    private boolean isMedia(String path) {
        return ContentType.isMusic(path) || ContentType.isVideo(path);
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
            openInNwjs(urlPath);
        }
        else {
           openInStandardBrowser(urlPath);
        }
    }

    private void openInNwjs(String url){
        File outputDir = new File(contentInfoProvider.getDecoder().getStateDirectory(), "nw-current-app");
        if (!outputDir.exists() && !outputDir.mkdirs()){
            log.warn("Failed to create app dir " + outputDir.getAbsolutePath());
            openInStandardBrowser(url);
            return;
        }
        openInNwjs(url, outputDir);
    }

    private boolean shouldUseNwjs(String path){
        return false;
//                nwjsEnabled &&  nwjsExe != null && nwjsExe.exists() &&
//                path.toLowerCase().indexOf("youtube") > -1;
    }

    private void openInNwjs(String url, File outputDir){
        String packageContent = "{\n" +
                "  \"name\": \"player\",\n" +
                "  \"main\": \""+url+"\",\n" +
                "  \"inject-js-end\": \"main.js\",\n" +
                "  \"window\": {\n" +
                "    \"title\": \"Nws player\",\n" +
                "    \"toolbar\": " + (fullScreenNwjs ? "false" : "true") + ",\n" +
                "    \"frame\": true,\n" +
                "    \"fullscreen\": " + (fullScreenNwjs ? "true" : "false") +
                "  }\n" +
                "}";

        String mainInject = StreamUtil.toString( FileUtil.findStream("src/main/resources#weland/main.js"));
        FileUtil.writeStringToFile(new File(outputDir, "package.json"), packageContent);
        FileUtil.writeStringToFile(new File(outputDir, "main.js"), mainInject);
        execute(new String[]{
                nwjsExe.getAbsolutePath(), outputDir.getAbsolutePath()
        });
    }

    private String[] getCommands(String rname, String arg){
        Arr arr = commands.getArray(rname);

        for (int i = 0; i < arr.size(); i++){
            List<String> act = (List<String>) arr.get(i);
            File f = new File(act.get(0));
            if (f.exists()){
                String r[] = new String[act.size()];
                r[0] = f.getAbsolutePath();
                for (int j = 1; j < act.size(); j++){
                    if ("{arg}".equals(act.get(j))){
                        r[j] = arg;
                    }
                    else {
                        r[j] = act.get(j);
                    }
                }

                return r;
            }
        }
        return null;
    }

    private void openInStandardBrowser(String path){
        execute(getCommands("browser", path));
    }



    @Override
    public HttpResponse stop(String x) {
        log.info("STOP " + x);
        ThreadUtil.closeTimer(timerResult);

        //pause vlc http instance
        if (VLCWrapper.isHttpOpened()){
            log.info("vlc stop " + x);
            VLCWrapper.stopHttp();
        }
        closeSpawnedProcesses();

        return DeviceStat.getInstance().setProp(CURRENT_RUNNING, "").toHttp();
    }

    private void closeSpawnedProcesses(){
        if (clearing || exes.isEmpty()) {
            return;
        }
        synchronized (exes){
            if (clearing) {
                return;
            }
            clearing = true;
            boolean shouldWait = exes.size() > 0;
            for (String s: exes){
                try {
                    if (SYSUtils.isWindows()){
                        String args[] = getCloseCommand(s);
                        log.info(StringUtil.join(args, " "));
                        Runtime.getRuntime().exec(args);
                    }

                    if (SYSUtils.isLinux()){
                        String args[] = new String[]{"pkill", "-9", s};
                        log.info(StringUtil.join(args, " "));
                        Runtime.getRuntime().exec(args);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            exes.clear();
            if (shouldWait){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    ;;
                }
            }
            clearing = false;
        }
    }

    String[] getCloseCommand(String s){
        return new String[]{"taskkill", "/IM", s, "/T", "/F"};
    }



    @Override
    public void onMessageReceived(Message message) {
        ThreadUtil.closeTimer(result);
        if (nf == null){
            nf = new JFrame();
            nf.setAlwaysOnTop(true);
            label = new JLabel();
            label.setText(message.getText());
            Font labelFont = label.getFont();
            label.setFont(new Font(labelFont.getName(), Font.PLAIN, 24));
            label.setBorder(new EmptyBorder(10,10,10,10));
            nf.add(label);
            nf.setUndecorated(true);
            nf.pack();
            nf.setVisible(true);
        }
        else {
            label.setText(message.getText());
            nf.pack();
            nf.setVisible(true);
        }

        result = ThreadUtil.delayedTask(new Runnable() {
            @Override
            public void run() {
                nf.setVisible(false);
            }
        }, 1000);
    }

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

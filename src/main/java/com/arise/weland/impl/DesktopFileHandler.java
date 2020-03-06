package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpResponse;
import com.arise.canter.Command;
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
import com.arise.weland.dto.Message;
import com.arise.weland.model.ContentHandler;
import com.arise.weland.utils.URLBeautifier;
import uk.co.caprica.vlcj.player.MediaMeta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DesktopFileHandler extends ContentHandler {

    private final ContentInfoProvider contentInfoProvider;
    private final Registry registry;
    static boolean nwjsEnabled = !"false".equalsIgnoreCase(System.getProperty("nwjs.enabled"));
    static boolean vlcEmbedded = !"false".equalsIgnoreCase(System.getProperty("vlc.enabled"));
    private static final Mole log = Mole.getInstance(DesktopFileHandler.class);
    private static File nwjsExe;
    static {
        try {
            File nwjsDir = DependencyManager.solve(Dependencies.NWJS_0_12_0).uncompressed();
            nwjsExe = new File(nwjsDir, "nw.exe");
            if (!nwjsExe.exists()){
                log.error("Unable to solve NWJS");
                nwjsEnabled = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            nwjsEnabled = false;
        }
    }

    boolean fullScreenNwjs = "true".equalsIgnoreCase(System.getProperty("nwjs.fullscreen"));


    public DesktopFileHandler(ContentInfoProvider contentInfoProvider, Registry registry) {
        this.contentInfoProvider = contentInfoProvider;
        this.registry = registry;
        setupArgs();
    }
    MapObj commands;

    private void setupArgs() {
        String s = StreamUtil.toString(
                FileUtil.findStream("weland/config/commons/executables.json")
        ).replaceAll("\\s+", " ");

        commands = (MapObj) Groot.decodeBytes(s);
    }


    public HttpResponse open(final String path) {

        stop("-");
        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {

                log.info("OPEN " + path);

                if (isInternal(path)){
                    openUrl(fix(path));
                    return;
                }

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

    private boolean isInternal(String path) {
        return path.startsWith("{host}");
    }

    private String fix(String data){
        return  "http://localhost:8221" + data.substring("{host}".length());
    }


    @Override
    protected HttpResponse play(String path, Mode mode) {
        stop("-");
        log.info("PLAY " + mode + " " + path);
        if (isInternal(path)){
            return play(fix(path), mode);
        }

        if (isHttpPath(path)){
            path = URLBeautifier.beautify(path);
            if (mode.equals(Mode.NATIVE)) {
                openInStandardBrowser(path);
            }
            else {
                openInNwjs(path);
            }
        }

        else if (isMedia(path)){
            if (mode.equals(Mode.NATIVE)) {
                execute(getCommands("media", path));
            }
            else {
                VLCPlayer.getInstance().play(path);
            }
        }
        else {
            open(path);
        }

        return null;
    }


    private void openMedia(String path) {
        File local = new File(path);
        if (local.exists() && vlcEmbedded){
            ContentInfo info = contentInfoProvider.findByPath(local.getAbsolutePath().replaceAll("\\\\", "/"));
            if (info != null){
                VLCPlayer.getInstance().play(info);
            }
            else {
                VLCPlayer.getInstance().play(path);
            }
        }
        else {
            execute(getCommands("media", path));
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
        return nwjsEnabled &&  nwjsExe.exists() &&
                path.toLowerCase().indexOf("youtube") > -1;
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
            boolean shouldWait = exes.size() > 0;
            for (String s: exes){
                try {
                    if (SYSUtils.isWindows()){
                        String args[] = getCloseCommand(s);
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
    public HttpResponse pause(String string) {
        return null;
    }


    JFrame nf = null;
    JLabel label = null;
    ThreadUtil.TimerResult result;

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


    public static void main(String[] args) {
        Message message = new Message();
        message.setText("some text");
        DesktopFileHandler f = new DesktopFileHandler(null, null);
                f.onMessageReceived(message);
        message.setText("some text2");
                f.onMessageReceived(message);
    }



}

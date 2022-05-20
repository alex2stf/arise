package com.arise.weland;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.servers.draft_6455.WSDraft6455;
import com.arise.astox.net.servers.io.IOServer;
import com.arise.canter.Arguments;
import com.arise.canter.Command;
import com.arise.canter.Cronus;
import com.arise.canter.Registry;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.AppSettings;
import com.arise.core.tools.*;
import com.arise.weland.impl.*;
import com.arise.weland.impl.unarchivers.MediaInfoSolver;
import com.arise.weland.ui.WelandForm;
import com.arise.weland.utils.WelandServerHandler;

import javax.net.ssl.SSLContext;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.arise.canter.Defaults.PROCESS_EXEC;
import static com.arise.weland.dto.DeviceStat.getInstance;

public class Main {

    private static final SSLContext context;
    private static final Mole log = Mole.getInstance(Main.class);

    static {
//        String protocol = "TLSv1.2";
//        SSLContext localContext = null;
//        try {
//            localContext = SSLContext.getInstance(protocol);
//            localContext.init(
//                    NioSslPeer.createKeyManagers("./src/main/resources/weland/certificates/server.jks", "storepass", "keypass"),
//                    NioSslPeer.createTrustManagers("./src/main/resources/weland/certificates/trustedCerts.jks", "storepass"), new SecureRandom());
//        } catch (Exception e) {
////            e.printStackTrace();
////            Mole.getInstance(Main.class).error(e);
//        }
//        context = localContext;
        context = null;
    }



    private static volatile boolean cmd_executing = false;

    private static final Command<String> PLAY_MP3_RANDOM_CMD = new Command<String>("play-music-random") {
        @Override
        public String execute(Arguments arguments) {
            if (cmd_executing){
                return "xx";
            }
            cmd_executing = true;
            String path = Paths.get(arguments.get(0)).normalize().toAbsolutePath().toString();

            if (!new File(path).exists()){
                log.warn("Path " + path + " does not exist");
            } else {
                log.trace("PLAY_MP3_RANDOM_CMD called with " + path);
            }

            AppCache.StoredList storedList = AppCache.getStoredList("mp3-rand");
            if (storedList.isEmpty() || storedList.isIndexExceeded()){

                File dir = new File(path);
                File[] files = dir.listFiles();
                if (files == null || files.length == 0){
                    return dir.getAbsolutePath();
                }
                List<String> items = new ArrayList<>();
                for (File f: files){
                    items.add(f.getAbsolutePath());
                }
                Collections.shuffle(items);

                storedList = AppCache.storeList("mp3-rand", items, 0);
                log.info("RESHUFFLED LIST");
            }

            List<String> saved = storedList.getItems();
            int index = storedList.getIndex();
            AppCache.storeList("mp3-rand", saved, index + 1);

            String selected = saved.get(index);

            if (!new File(selected).exists()){
                log.warn("Path " + selected + " does not exist anymore");
            } else {
                log.info(new Date() + " ] PLAY "+ index + " from " + selected);
            }

            desktopContentHandler.openPath(selected);
            cmd_executing = false;
            return saved.get(index);
        }
    };


    private static final Command<String> MOUSE_PING = new Command<String>("mouse-ping") {
        @Override
        public String execute(Arguments arguments) {
            try {
                new Robot().mouseMove(0, 0);
                if ("debug".equalsIgnoreCase(arguments.get(0))) {
                    log.info("MOUSE_PING at " + new Date());
                }
            } catch (AWTException e) {
                e.printStackTrace();
            }
            return null;
        }
    };




    private static void detect_device_capabilities(){
        try {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

            // Returns an array of all the screen GraphicsDevice objects.
            GraphicsDevice[] devices = env.getScreenDevices();

            int gdTotal = devices.length;
            getInstance().setProp("gd_total", gdTotal + "");

            for(GraphicsDevice gd: devices){
                String id = gd.getIDstring();
                getInstance().setProp("gd_" + id + "_type", gd.getType() + "");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }




    static BluecoveServer bluecoveServer;
    static AbstractServer ioServer;
    static  DesktopContentHandler desktopContentHandler;

    public static void main(String[] args) throws IOException {

        log.info("init weland server");
        log.info("SYSUtils.isLinux() " + SYSUtils.isLinux());
        detect_device_capabilities();

        DependencyManager.importDependencyRules("_cargo_/dependencies.json");
        MediaInfoSolver.load();
        AppCache.setWorker(new StandardCacheWorker());

        try {
            ContentType.loadDefinitions();
            log.info("Successfully loaded content-type definitions");
        } catch (Exception e){
            log.error("Failed to load content-type definitions", e);
        }

        final Registry registry = new Registry();
        registry.addCommand(PROCESS_EXEC)
                .addCommand(MOUSE_PING)
                .addCommand(PLAY_MP3_RANDOM_CMD);

//        try {
//            registry.loadJsonResource("src/main/resources#/weland/config/commons/commands.json");
//            if (SYSUtils.isWindows()){
//                registry.loadJsonResource("src/main/resources#/weland/config/win/commands.json");
//            } else {
//                registry.loadJsonResource("src/main/resources#/weland/config/unix/commands.json");
//            }
//            log.info("Successfully loaded commands definitions");
//        } catch (Exception e){
//            log.error("Failed to load commands definitions", e);
//        }

        String localCommands = AppSettings.getProperty(AppSettings.Keys.LOCAL_COMANDS_FILE);
        if (StringUtil.hasText(localCommands) && new File(localCommands).exists()){
            registry.loadJsonResource(localCommands);
        }

        final IDeviceController deviceController = new PCDeviceController();
        final ContentInfoDecoder decoder = new PCDecoder();
        final ContentInfoProvider contentInfoProvider = new ContentInfoProvider(decoder)
                .addFromLocalResource("weland/config/commons/content-infos.json");


        for (File file: AppSettings.getScannableLocations()){
            if (!file.exists()){
                log.warn("scannable location " + file.getAbsolutePath() + " not found...");
            } else {
                log.info("added scannable root ", file.getAbsolutePath());
            }
            contentInfoProvider.addRoot(file);

        }

        contentInfoProvider.get();



        desktopContentHandler = new DesktopContentHandler(contentInfoProvider,  registry);


        DesktopCamStream desktopCamStream = new DesktopCamStream(
                desktopContentHandler.getLiveMjpegStream(),
                desktopContentHandler.getLiveJpeg()
        );

        Cronus cronus = null;

        if (!AppSettings.isFalse(AppSettings.Keys.CRONUS_ENABLED)){
            cronus = new Cronus(registry, AppSettings.getProperty(AppSettings.Keys.CRONUS_CONFIG_FILE, "resources#weland/config/cronus.json"));
        }

        desktopContentHandler.setCameraStream(desktopCamStream);

        final WelandServerHandler welandServerHandler = new WelandServerHandler(registry)
                .setContentProvider(contentInfoProvider)
                .setContentHandler(desktopContentHandler)
                .setDeviceController(deviceController);


        final WelandRequestBuilder requestBuilder = new WelandRequestBuilder(deviceController);


        if(AppSettings.isTrue(AppSettings.Keys.UI_ENABLED)) {
            final Cronus finalCronus = cronus;
            ThreadUtil.fireAndForget(new Runnable() {
                @Override
                public void run() {

                    WelandForm welandForm = new WelandForm(registry);
                    welandForm.pack();
                    welandForm.setVisible(true);

                    if(finalCronus != null) {
                        finalCronus.registerTask(welandForm);
                    }
                    else {
                        ThreadUtil.repeatedTask(welandForm, 1000);
                    }

                    if(AppSettings.isTrue(AppSettings.Keys.UI_CLOSE_ON_EXIT)){
                        welandForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    }
                }
            }, ThreadUtil.threadId("ui-thread"));

        }


        ThreadUtil.fireAndForget(new Runnable() {

            @Override
            public void run() {

                int port = AppSettings.getInt(AppSettings.Keys.SERVER_PORT, 8221);
                ioServer = new IOServer()
                        .setPort(port)
                        .setName("DR_" + SYSUtils.getDeviceName())
                        .setUuid(UUID.randomUUID().toString())
                        .setRequestBuilder(requestBuilder)
                        .addDuplexDraft(new WSDraft6455())
                        .setHost("localhost")
                        .setStateObserver(welandServerHandler)
                        .setRequestHandler(welandServerHandler);
                log.info("SERVER STARTED at http://" + NetworkUtil.getCurrentIPV4AddressSync() + ":"+port+"/app");
                try {
                    ioServer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Main#startServer" + UUID.randomUUID().toString());

        //DO NOT DELETE BluecoveServer
//        ThreadUtil.fireAndForget(new Runnable() {
//            @Override
//            public void run() {
//                bluecoveServer = new BluecoveServer();
//                bluecoveServer.setDeviceController(deviceController);
//                bluecoveServer.setStateObserver(welandServerHandler)
//                        .setRequestBuilder(requestBuilder)
//                        .setRequestHandler(welandServerHandler)
//                        .setName("CB_" + SYSUtils.getDeviceName());
//
//                try {
//                    bluecoveServer.start();
//                } catch (Exception e) {
//                    log.error("Failed to start bluetooth server because", e);
//                }
//            }
//        });


    }

    private void stopAll() {
        ioServer.stop();
        if ( bluecoveServer != null){
            bluecoveServer.stop();
        }
    }

}

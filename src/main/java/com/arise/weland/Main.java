package com.arise.weland;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.servers.draft_6455.WSDraft6455;
import com.arise.astox.net.servers.io.IOServer;
import com.arise.canter.Command;
import com.arise.canter.CommandRegistry;
import com.arise.canter.Cronus;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.AppSettings;
import com.arise.core.tools.AppCache;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.NetworkUtil;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StandardCacheWorker;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.impl.BluecoveServer;
import com.arise.weland.impl.ContentInfoDecoder;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.DesktopCamStream;
import com.arise.weland.impl.DesktopContentHandler;
import com.arise.weland.impl.IDeviceController;
import com.arise.weland.impl.PCDecoder;
import com.arise.weland.impl.PCDeviceController;
import com.arise.weland.impl.RadioPlayer;
import com.arise.weland.impl.WelandRequestBuilder;
import com.arise.weland.impl.unarchivers.MediaInfoSolver;
import com.arise.weland.ui.WelandForm;
import com.arise.weland.utils.WelandServerHandler;

import javax.net.ssl.SSLContext;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.arise.canter.Defaults.PROCESS_EXEC;
import static com.arise.core.tools.FileUtil.*;
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




    private static final Command<String> PLAY_MP3_RANDOM_CMD = new Command<String>("play-music-random") {
        @Override
        public String execute(List<String> arguments) {
            String path = Paths.get(arguments.get(0)).normalize().toAbsolutePath().toString();
            File f = getRandomFileFromDirectory(path);
            return f.getAbsolutePath();
        }
    };


    private static final Command<String> MOUSE_PING = new Command<String>("mouse-ping") {
        @Override
        public String execute(List<String> arguments) {
            try {
                new Robot().mouseMove(-2, -2);
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
    static  RadioPlayer rplayer;

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

        final CommandRegistry cmdReg = DependencyManager.getCommandRegistry();
        cmdReg.addCommand(PROCESS_EXEC)
                .addCommand(MOUSE_PING)
                .addCommand(PLAY_MP3_RANDOM_CMD);


        String localCommands = AppSettings.getProperty(AppSettings.Keys.LOCAL_COMANDS_FILE);
        if (StringUtil.hasText(localCommands) && new File(localCommands).exists()){
            cmdReg.loadJsonResource(localCommands);
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



        desktopContentHandler = new DesktopContentHandler(contentInfoProvider, cmdReg);


        DesktopCamStream desktopCamStream = new DesktopCamStream(
                desktopContentHandler.getLiveMjpegStream(),
                desktopContentHandler.getLiveJpeg()
        );

        Cronus cronus = null;

        if (!AppSettings.isFalse(AppSettings.Keys.CRONUS_ENABLED)){
            cronus = new Cronus(cmdReg, AppSettings.getProperty(AppSettings.Keys.CRONUS_CONFIG_FILE, "resources#weland/config/cronus.json"));
        }

        if (AppSettings.isTrue(AppSettings.Keys.RADIO_ENABLED)){
            rplayer = new RadioPlayer();
            rplayer.loadShowsResourcePath(AppSettings.getProperty(AppSettings.Keys.RADIO_SHOWS_PATH));

            ThreadUtil.startThread(new Runnable() {
                @Override
                public void run() {
                    rplayer.play();
                }
            }, "radio-play");
        }

        desktopContentHandler.setCameraStream(desktopCamStream);

        final WelandServerHandler welandServerHandler = new WelandServerHandler(cmdReg)
                .setContentProvider(contentInfoProvider)
                .setContentHandler(desktopContentHandler)
                .setDeviceController(deviceController);


        final WelandRequestBuilder requestBuilder = new WelandRequestBuilder(deviceController);


        if(AppSettings.isTrue(AppSettings.Keys.UI_ENABLED)) {
            final Cronus finalCronus = cronus;
            ThreadUtil.fireAndForget(new Runnable() {
                @Override
                public void run() {

                    WelandForm welandForm = new WelandForm(cmdReg);
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
            }, "ui-thread");

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
        }, "Main#startServer");

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

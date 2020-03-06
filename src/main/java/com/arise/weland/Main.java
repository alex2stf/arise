package com.arise.weland;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.servers.draft_6455.WSDraft6455;
import com.arise.astox.net.servers.io.IOServer;
import com.arise.canter.Registry;
import com.arise.core.tools.*;
import com.arise.weland.impl.BluecoveServer;
import com.arise.weland.impl.ContentInfoDecoder;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.DesktopFileHandler;
import com.arise.weland.impl.IDeviceController;
import com.arise.weland.impl.PCDecoder;
import com.arise.weland.impl.PCDeviceController;
import com.arise.weland.impl.SelfUpdater;
import com.arise.weland.impl.VLCPlayer;
import com.arise.weland.impl.WelandRequestBuilder;
import com.arise.weland.impl.ui.desktop.WelandFrame;
import com.arise.weland.utils.WelandServerHandler;

import javax.net.ssl.SSLContext;
import javax.swing.*;
import java.io.File;
import java.util.UUID;

import static com.arise.canter.Defaults.PROCESS_EXEC;
import static com.arise.canter.Defaults.PROCESS_EXEC_WHEN_FOUND;

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









    static BluecoveServer bluecoveServer;
    static AbstractServer ioServer;

    public static void main(String[] args) {
        //VIP!!! load vlc native libs and dependencies
        VLCPlayer.getInstance();
        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
//                new SelfUpdater().check();
            }
        });



        try {
            ContentType.loadDefinitions();
            log.info("Successfully loaded content-type definitions");
        } catch (Exception e){
            log.error("Failed to load content-type definitions", e);
        }

        final Registry registry = new Registry();
        registry.addCommand(PROCESS_EXEC)
                .addCommand(PROCESS_EXEC_WHEN_FOUND);

        try {
            registry.loadJsonResource("src/main/resources#/weland/config/commons/commands.json");
            if (SYSUtils.isWindows()){
                registry.loadJsonResource("src/main/resources#/weland/config/win/commands.json");
            } else {
                registry.loadJsonResource("src/main/resources#/weland/config/unix/commands.json");
            }
            log.info("Successfully loaded commands definitions");
        } catch (Exception e){
            log.error("Failed to load commands definitions", e);
        }

        final IDeviceController deviceController = new PCDeviceController();
        final ContentInfoDecoder decoder = new PCDecoder();
        final ContentInfoProvider contentInfoProvider = new ContentInfoProvider(decoder).importJson("weland/config/commons/content-infos.json");



        if (StringUtil.hasText(System.getProperty("scan.root.file"))){
            File rootFile = new File(System.getProperty("scan.root.file"));
            contentInfoProvider.addRoot(rootFile);
        }
        else {
            File musicDir = FileUtil.findMusicDir();
            File videosDir = FileUtil.findMoviesDir();

            contentInfoProvider.addRoot(musicDir);
            contentInfoProvider.addRoot(videosDir);
            for (File f: File.listRoots()){
                if (!f.getAbsolutePath().startsWith("C:")) {
                    contentInfoProvider.addRoot(f);
                }
            }

        }


        contentInfoProvider.get();

        final WelandServerHandler welandServerHandler = new WelandServerHandler()
                .setContentProvider(contentInfoProvider);;

        DesktopFileHandler desktopFileHandler = new DesktopFileHandler(contentInfoProvider,  registry);
        welandServerHandler.setContentHandler(desktopFileHandler);
        welandServerHandler.setDeviceController(deviceController);

        final WelandRequestBuilder requestBuilder = new WelandRequestBuilder(deviceController);

        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                ioServer = new IOServer()
                        .setPort(8221)
                        .setName("DR_" + SYSUtils.getDeviceName())
                        .setUuid(UUID.randomUUID().toString())
                        .setRequestBuilder(requestBuilder)
                        .addDuplexDraft(new WSDraft6455())
                        .setHost("localhost")
                        .setStateObserver(welandServerHandler)
                        .setRequestHandler(welandServerHandler);
                try {
                    ioServer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                bluecoveServer = new BluecoveServer();
                bluecoveServer.setDeviceController(deviceController);
                bluecoveServer.setStateObserver(welandServerHandler)
                        .setRequestBuilder(requestBuilder)
                        .setRequestHandler(welandServerHandler)
                        .setName("CB_" + SYSUtils.getDeviceName());

                try {
                    bluecoveServer.start();
                } catch (Exception e) {
                    log.error("Failed to start bluetooth server because", e);
                }
            }
        });

        if ("true".equalsIgnoreCase(System.getProperty("weland.ui.enabled"))){
            JFrame.setDefaultLookAndFeelDecorated(true);
            WelandFrame welandFrame= new WelandFrame(contentInfoProvider);
            welandFrame.setVisible(true);
            welandFrame.initComponents();
            welandFrame.pack();
            welandFrame.fullscreen();
        }

    }

    private void stopAll() {
        ioServer.stop();
        if ( bluecoveServer != null){
            bluecoveServer.stop();
        }
    }

}

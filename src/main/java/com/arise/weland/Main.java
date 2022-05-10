package com.arise.weland;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.servers.draft_6455.WSDraft6455;
import com.arise.astox.net.servers.io.IOServer;
import com.arise.canter.Cronus;
import com.arise.canter.Registry;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.AppSettings;
import com.arise.core.tools.AppCache;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.Mole;
import com.arise.core.tools.NetworkUtil;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StandardCacheWorker;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.impl.BluecoveServer;
import com.arise.weland.impl.ContentInfoDecoder;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.DesktopCamStream;
import com.arise.weland.impl.DesktopContentHandler;
import com.arise.weland.impl.IDeviceController;
import com.arise.weland.impl.PCDecoder;
import com.arise.weland.impl.PCDeviceController;
import com.arise.weland.impl.WelandRequestBuilder;
import com.arise.weland.impl.unarchivers.MediaInfoSolver;
import com.arise.weland.utils.WelandServerHandler;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
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

    public static void main(String[] args) throws IOException {

        log.info("init weland server");
        log.info("SYSUtils.isLinux() " + SYSUtils.isLinux());

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
        final ContentInfoProvider contentInfoProvider = new ContentInfoProvider(decoder)
                .addFromLocalResource("weland/config/commons/content-infos.json");


        for (File file: AppSettings.getScannableLocations()){
            contentInfoProvider.addRoot(file);
            log.info("added scannable root ", file.getAbsolutePath());
        }

        contentInfoProvider.get();



        DesktopContentHandler desktopContentHandler = new DesktopContentHandler(contentInfoProvider,  registry);

        DesktopCamStream desktopCamStream = new DesktopCamStream(
                desktopContentHandler.getLiveMjpegStream(),
                desktopContentHandler.getLiveJpeg()
        );

        Cronus cronus = new Cronus(registry);
        desktopContentHandler.setCameraStream(desktopCamStream);

        final WelandServerHandler welandServerHandler = new WelandServerHandler(registry)
                .setContentProvider(contentInfoProvider)
                .setContentHandler(desktopContentHandler)
                .setDeviceController(deviceController);




        final WelandRequestBuilder requestBuilder = new WelandRequestBuilder(deviceController);



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

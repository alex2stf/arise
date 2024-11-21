package com.arise.weland;

import com.arise.astox.net.clients.JHttpClient;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.servers.io.IOServer;
import com.arise.canter.Command;
import com.arise.canter.CommandRegistry;
import com.arise.canter.Cronus;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.AppSettings;
import com.arise.core.AppSettings.Keys;
import com.arise.core.models.Handler;
import com.arise.core.tools.*;
import com.arise.weland.desk.DesktopContentHandler;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.impl.*;
import com.arise.weland.model.MediaPlayer;
import com.arise.weland.ui.ClockForm;
import com.arise.weland.utils.WelandServerHandler;

import javax.net.ssl.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

import static com.arise.canter.DefaultCommands.PROCESS_EXEC;
import static com.arise.core.AppSettings.isTrue;
import static com.arise.core.tools.ThreadUtil.startThread;
import static com.arise.weland.dto.DeviceStat.getInstance;

public class Main {

    private static final Mole log = Mole.getInstance(">>>  MAIN");




    private static final Command<String> MOUSE_PING = new Command<String>("mouse-ping") {
        @Override
        public String execute(List<String> arguments) {
            try {
                new Robot().mouseMove(-2, -2);
                if ("debug".equalsIgnoreCase(arguments.get(0))) {
                    log.info("MOUSE_PING at " + Util.now());
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

            getInstance().setSysProp("gd.total" , "Graphic devices", "The number of graphic devices", gdTotal);

            for(GraphicsDevice gd: devices){
                String id = gd.getIDstring();
                getInstance().setSysProp("gd." + id, "Graphic device " + id, "Display mode " + gd.getDisplayMode(), gd.getType());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    static AbstractServer ioServer;
    static  DesktopContentHandler desktopContentHandler;
    static  RadioPlayer rplayer;


    public static void doTrustToCertificates() throws Exception {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                        return;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                        return;
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    System.out.println("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    public static void main(String[] args) throws IOException {
        try {
            doTrustToCertificates();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int u = 0;
        File[] rts = File.listRoots();
        if(rts != null && rts.length > 0) {
            for (File f : File.listRoots()) {
                FileSystemView view = FileSystemView.getFileSystemView();
                if(view != null) {
                    String desc = view.getSystemTypeDescription(f);
                    if (StringUtil.hasContent(desc) && desc.toLowerCase().indexOf("usb ") > -1) {
                        String k = "usb.drive." + u;
                        String v = f.getAbsolutePath();
                        System.setProperty(k, v);
                        log.info("Set " + k + " = " + v);
                        u++;

                    }
                }
            }
        }


        JHttpClient.disableSSL();

        detect_device_capabilities();

        DependencyManager.importDependencyRules("_cargo_/dependencies.json");
        AppCache.setWorker(new StandardCacheWorker());

        try {
            ContentType.loadDefinitions();
            log.info("Successfully loaded content-type definitions");
        } catch (Exception e){
            log.error("Failed to load content-type definitions", e);
        }

        CommandRegistry.getInstance()
                .addCommand(PROCESS_EXEC)
                .addCommand(MOUSE_PING);

        String cmds = AppSettings.getProperty(Keys.LOCAL_COMANDS_FILE);
        if (StringUtil.hasText(cmds)){
            CommandRegistry.getInstance().loadJsonResource(cmds);
        }

        final ContentInfoDecoder decoder = new PCDecoder();
        final ContentInfoProvider contentInfoProvider = new ContentInfoProvider(decoder)
                //asta nu tb sa mai contina ce este deja in radio shows
                .addFromLocalResource("weland/config/commons/content-infos.json");


        for (File file: AppSettings.getScannableLocations()){
            if (!file.exists()){
                log.warn("scannable location " + file.getAbsolutePath() + " not found...");
            } else {
                log.info("added scannable root ", file.getAbsolutePath());
            }
            contentInfoProvider.addRoot(file);
        }



        desktopContentHandler = new DesktopContentHandler(contentInfoProvider);



        if (isTrue(Keys.RADIO_ENABLED)){
            rplayer = new RadioPlayer();
            rplayer.setContentInfoProvider(contentInfoProvider);
//            rplayer.loadShowsResourcePath("test_radio_show.json");
            rplayer.loadShowsResourcePath("radio_shows_special.json");
            rplayer.loadShowsResourcePath(AppSettings.getProperty(Keys.RADIO_SHOWS_PATH));



            ContentInfoProvider.printReport();
            System.exit(-1);

            if(isTrue(Keys.FORCE_CLOSE_ON_STARTUP)){
                RadioPlayer.getMediaPlayer().stop(new Handler<MediaPlayer>() {
                    @Override
                    public void handle(MediaPlayer mediaPlayer) {
                        log.info("Force close media player, de aici ar trebui continuat");
                    }
                });
            }

            rplayer.onPlay(new Handler<RadioPlayer>() {
                @Override
                public void handle(RadioPlayer rpl) {
                    DeviceStat.getInstance().setProp("rplayer.play", "true");
                }
            });
            rplayer.onStop(new Handler<RadioPlayer>() {
                @Override
                public void handle(RadioPlayer rpl) {
                    DeviceStat.getInstance().setProp("rplayer.play", "false");
                }
            });



            rplayer.onStreamChanged(new Handler<Show>() {
                @Override
                public void handle(Show show) {
                    AppDispatcher.tick();

                }
            });
            desktopContentHandler.setRadioPlayer(rplayer);
            startThread(new Runnable() {
                @Override
                public void run() {
                    rplayer.play();
                }
            }, "radio-play");
        }

        //porneste scan DUPA ce pornesti rPlayer
        contentInfoProvider.get();

        final WelandServerHandler welandServerHandler = new WelandServerHandler()
                .setContentProvider(contentInfoProvider)
                .setContentHandler(desktopContentHandler);

        final WelandRequestBuilder requestBuilder = new WelandRequestBuilder();


        if(isTrue(Keys.UI_CLOCK_ENABLED)) {

            ThreadUtil.fireAndForget(new Runnable() {
                @Override
                public void run() {
                    ClockForm clockForm = new ClockForm();
                    clockForm.pack();
                    clockForm.setVisible(true);
                }
            }, "ui-thread");

        }


        ThreadUtil.fireAndForget(new Runnable() {

            @Override
            public void run() {

                int port = AppSettings.getInt(Keys.SERVER_PORT, 8221);
                ioServer = new IOServer()
                        .setPort(port)
                        .setName("DR_" + SYSUtils.getDeviceName())
                        .setUuid(UUID.randomUUID().toString())
                        .setRequestBuilder(requestBuilder)
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


    }

    private void stopAll() {
        ioServer.stop();
    }




}

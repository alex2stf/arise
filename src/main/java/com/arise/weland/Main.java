package com.arise.weland;

import com.arise.astox.net.clients.JHttpClient;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.servers.draft_6455.WSDraft6455;
import com.arise.astox.net.servers.io.IOServer;
import com.arise.canter.Command;
import com.arise.canter.CommandRegistry;
import com.arise.canter.Cronus;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.AppSettings;
import com.arise.core.AppSettings.Keys;
import com.arise.core.models.Handler;
import com.arise.core.tools.AppCache;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.Mole;
import com.arise.core.tools.NetworkUtil;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StandardCacheWorker;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.Util;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.impl.ContentInfoDecoder;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.desk.DesktopContentHandler;
import com.arise.weland.impl.PCDecoder;
import com.arise.weland.impl.RadioPlayer;
import com.arise.weland.impl.WelandRequestBuilder;
import com.arise.weland.impl.unarchivers.MediaInfoSolver;
import com.arise.weland.model.MediaPlayer;
import com.arise.weland.ui.WelandForm;
import com.arise.weland.utils.WelandServerHandler;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.arise.canter.Defaults.PROCESS_EXEC;
import static com.arise.core.AppSettings.isTrue;
import static com.arise.core.tools.ThreadUtil.repeatedTask;
import static com.arise.core.tools.ThreadUtil.startThread;
import static com.arise.weland.dto.DeviceStat.getInstance;

public class Main {

    private static final Mole log = Mole.getInstance(Main.class);




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

    public static void main(String[] args) throws IOException {
        System.out.println("hi");

        int u = 0;
        for(File f: File.listRoots()){
            String desc = FileSystemView.getFileSystemView().getSystemTypeDescription(f);
            if(desc.toLowerCase().indexOf("usb ") > -1) {
                String k = "usb.drive." + u;
                String v = f.getAbsolutePath();
                System.setProperty(k, v);
                log.info("Set " + k + " = " + v);
                u++;

            }
        }


        JHttpClient.disableSSL();

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
                .addCommand(MOUSE_PING);

        String cmds = AppSettings.getProperty(Keys.LOCAL_COMANDS_FILE);
        if (StringUtil.hasText(cmds)){
            cmdReg.loadJsonResource(cmds);
        }

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

        desktopContentHandler = new DesktopContentHandler(contentInfoProvider);

        final WelandForm[] welandForm = {null};
        Cronus cronus = null;

        if (!AppSettings.isFalse(Keys.CRONUS_ENABLED)){
            cronus = new Cronus(cmdReg, AppSettings.getProperty(Keys.CRONUS_CONFIG_FILE, "resources#weland/config/cronus.json"));
        }

        if (isTrue(Keys.RADIO_ENABLED)){
            rplayer = new RadioPlayer();
            rplayer.loadShowsResourcePath(AppSettings.getProperty(Keys.RADIO_SHOWS_PATH));

            if(isTrue(Keys.FORCE_CLOSE_ON_STARTUP)){
                RadioPlayer.getMediaPlayer().stop(new Handler<MediaPlayer>() {
                    @Override
                    public void handle(MediaPlayer mediaPlayer) {
                        log.info("Force close media player");
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

            rplayer.onStreamChanged(new Handler<RadioPlayer.Show>() {
                @Override
                public void handle(RadioPlayer.Show show) {
                    if (welandForm[0] != null){
                        welandForm[0].showNextIcon();
                    }
                    log.info("Stream changed");

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


        final WelandServerHandler welandServerHandler = new WelandServerHandler(cmdReg)
                .setContentProvider(contentInfoProvider)
                .setContentHandler(desktopContentHandler);

        final WelandRequestBuilder requestBuilder = new WelandRequestBuilder();


        if(isTrue(Keys.UI_ENABLED)) {
            final Cronus finalCronus = cronus;
            ThreadUtil.fireAndForget(new Runnable() {
                @Override
                public void run() {


                    welandForm[0] = new WelandForm(cmdReg);
                    welandForm[0].pack();
                    welandForm[0].setVisible(true);
                    desktopContentHandler.setForm(welandForm[0]);
                    if (rplayer != null){
                        welandForm[0].setRadioPlayer(rplayer);
                    }

                    if(finalCronus != null) {
                        finalCronus.registerTask(welandForm[0]);
                    }
                    else {
                        repeatedTask(welandForm[0], 1000);
                    }

                    if(isTrue(Keys.UI_CLOSE_ON_EXIT)){
                        welandForm[0].setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    }
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


    }

    private void stopAll() {
        ioServer.stop();
    }




}

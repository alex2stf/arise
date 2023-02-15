package com.arise.droid;

import static com.arise.core.AppSettings.Keys.RADIO_ENABLED;
import static com.arise.core.AppSettings.Keys.RADIO_SHOWS_PATH;
import static com.arise.core.AppSettings.getProperty;
import static com.arise.core.AppSettings.isTrue;
import static com.arise.core.tools.ThreadUtil.startThread;
import static com.arise.droid.AppUtil.ON_START;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.servers.draft_6455.WSDraft6455;
import com.arise.astox.net.servers.io.IOServer;
import com.arise.canter.CommandRegistry;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.models.Handler;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.Util;
import com.arise.droid.impl.AndroidContentHandler;
import com.arise.droid.impl.AndroidMediaPlayer;
import com.arise.droid.impl.SensorReader;
import com.arise.droid.tools.NotificationOps;
import com.arise.droid.tools.RAPDUtils;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.impl.RadioPlayer;
import com.arise.weland.impl.WelandRequestBuilder;
import com.arise.weland.utils.WelandServerHandler;

import java.util.Date;
import java.util.UUID;


public class ServerService extends Service {

    static final String CHANNEL_ID = "LAYNEE_CHANNEL";
    static final int NOTIFICATION_ID = (int) System.currentTimeMillis();
    private static final Mole log = Mole.getInstance(ServerService.class);

    WelandServerHandler serverHandler;
    WelandRequestBuilder requestBuilder;
    AbstractServer server;

    AndroidContentHandler androidContentHandler;



    public void prepareServer() {
        try {
            ContentType.loadDefinitions();
            log.info("Successfully loaded content-type definitions");
        } catch (Exception e) {
            log.error("Failed to load content-type definitions", e);
        }

        androidContentHandler = new AndroidContentHandler(this);
        AndroidMediaPlayer.getInstance().setContentInfoProvider(
                AppUtil.contentInfoProvider
        );

        requestBuilder = new WelandRequestBuilder();
        final CommandRegistry cmdReg = DependencyManager.getCommandRegistry();
        serverHandler = new WelandServerHandler(cmdReg)
                .setContentProvider(AppUtil.contentInfoProvider)
                .setContentHandler(androidContentHandler);
        setupRadioIfRequired();
    }



    private void setupRadioIfRequired(){
        if (isTrue(RADIO_ENABLED)){
            AppUtil.rPlayer = new RadioPlayer();
            RadioPlayer rPlayer = AppUtil.rPlayer;





            rPlayer.onPlay(new Handler<RadioPlayer>() {
                @Override
                public void handle(RadioPlayer rpl) {
                    DeviceStat.getInstance().setProp("rplayer.play", "true");
                }
            });
            rPlayer.onStop(new Handler<RadioPlayer>() {
                @Override
                public void handle(RadioPlayer rpl) {
                    DeviceStat.getInstance().setProp("rplayer.play", "false");
                }
            });
            startThread(new Runnable() {
                @Override
                public void run() {
//                    rPlayer.loadTestData();
                    rPlayer.loadShowsResourcePath(getProperty(RADIO_SHOWS_PATH));
                    rPlayer.play();
                }
            }, "Radio-Thread");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            log("onStartCommand from system");
        } else {
            log("onStartCommand from app");
        }
        Util.registerContext(getApplicationContext());
        Util.registerContext(getApplication());
        return Service.START_STICKY;
    }



    private void startServer() {
        log.info("Server service starting");

        server = new IOServer()
                .setPort(8221)
                .setName("DR_" + SYSUtils.getDeviceName())
                .setUuid(UUID.randomUUID().toString())
                .setRequestBuilder(requestBuilder)
                .addDuplexDraft(new WSDraft6455())
                .setHost("localhost")
                .setStateObserver(serverHandler)
                .setRequestHandler(serverHandler);


        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Failed to start server", e);
                }
            }
        }, "ServerService#start-" + UUID.randomUUID().toString());

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ON_START);
        broadcastIntent.putExtra("http-connect", server.getConnectionPath());
        sendBroadcast(broadcastIntent);

        notify(this);
    }





    public void notify(Context context) {

        NotificationOps notificationOps = new NotificationOps()
//                .setSmallIcon(R.drawable.ic_logo_no_back)
                .setTitle("Laynee service runnig")
                .setText("started at " + new Date())
                .setChannelId(CHANNEL_ID)
                .setChannelDescription("Laynee channel for server support")
                .setId(NOTIFICATION_ID)
                .setFlags(Notification.FLAG_FOREGROUND_SERVICE)
                ;

        Notification notification = RAPDUtils.createNotification(context, notificationOps);

        startForeground(NOTIFICATION_ID, notification);
    }


    IntentFilter batteryFilter = null;
    BroadcastReceiver batteryReceiver = null;

    SensorManager sensorManager = null;

    SensorEventListener sensorEventListener = null;

    /**
     * this is called only once
     */
    private static volatile boolean _cmd_create_recv = false;

    @Override
    public void onCreate() {
        if (_cmd_create_recv){
            return;
        }

        if (AppUtil.contentInfoProvider.noFilesScanned()) {
            AppUtil.contentInfoProvider.get();
        }

        try {
            if (batteryFilter == null){
                batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

                if (batteryReceiver == null){
                    batteryReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent i) {
                            int s = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                            int l = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                            DeviceStat.getInstance().setBatteryLevel(l).setBatteryScale(s);
                        }
                    };
                }
                getApplicationContext().registerReceiver(batteryReceiver, batteryFilter);
            }

            if (sensorManager == null){
                sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            }

            if (sensorEventListener == null){
                sensorEventListener = new SensorReader(sensorManager);
            }

            SensorReader.applyEventListener(sensorManager, sensorEventListener);
        } catch (Exception e){
            log.e(e);
        }

        prepareServer();
        startServer();
        log("ON CREATE");

        _cmd_create_recv = true;


        super.onCreate();
    }







    @Override
    public void onDestroy() {
        log("onDestroy");

        if (server != null) {
            server.stop();
        }
        if (batteryFilter != null){
            try {
                getApplicationContext().unregisterReceiver(batteryReceiver);
            }catch (Exception e){

            }
        }

        if (sensorManager != null){
            sensorManager.unregisterListener(sensorEventListener);
        }


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);

        _cmd_create_recv = false;
        super.onDestroy();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {

        super.onTaskRemoved(rootIntent);
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getSystemService(ns);
        nMgr.cancelAll();
    }

    private void log(String text) {
        log.info("\n\t      " + text + "\n\n\n\n");
    }

}

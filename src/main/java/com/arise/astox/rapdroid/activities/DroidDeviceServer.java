package com.arise.astox.rapdroid.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.Menu;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arise.astox.net.http.HttpRequestBuilder;
import com.arise.astox.net.http.HttpResponse;
import com.arise.astox.net.servers.draft_6455.WSDraft6455;
import com.arise.astox.net.servers.io.IOServer;
import com.arise.astox.net.serviceHelpers.DefaultServerHandler;
import com.arise.astox.net.serviceHelpers.HTTPServerHandler;
import com.arise.astox.net.serviceHelpers.JPEGOfferResponse;
import com.arise.astox.net.serviceHelpers.MJPEGResponse;
import com.arise.astox.net.serviceHelpers.impl.DeviceStatusBuilder;
import com.arise.astox.rapdroid.net.CamStreamResponse;
import com.arise.astox.rapdroid.net.WavRecorderResponse;
import com.arise.astox.rapdroid.progress.IPChecker;
import com.arise.core.models.DeviceStat;
import com.arise.core.models.DialogStyle;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.NetworkUtil;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;

import java.util.HashSet;
import java.util.Set;

import astox.com.FamilyCamAct;

import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.VIBRATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.arise.core.tools.NetworkUtil.findFriendsForIp;
import static com.arise.core.tools.NetworkUtil.scanIPV4;
import static com.arise.core.tools.ThreadUtil.startThread;


/**
 * The following activity will make your application act as a server
 */
public abstract class DroidDeviceServer extends com.astox.rapdroid.RAPDroidActivity {

    private static final int HTTP_PORT = 8221;
    private static final Mole log = Mole.getInstance(FamilyCamAct.class);
    LinearLayout page1;
    DefaultServerHandler serverHandler;
    CamStreamResponse camStreamResponse;
    TextureView mPreview;
    WavRecorderResponse wavRecorderResponse;
    DeviceStat deviceStat;
    DeviceStatusBuilder deviceStatusBuilder;
    MJPEGResponse mjpegResponse = new MJPEGResponse();
    JPEGOfferResponse jpegOfferResponse = new JPEGOfferResponse();
    Set<String> friendIps = new HashSet<>();
    RAIDWebView webView;
    private String DEFAULT_DEVICE_ALIAS = "Red Survivor";
    private String DEVICE_ALIAS_KEY = "device-alias";
    private String BROADCAST_KEY = "isBroadcasting";
    private String deviceAlias;
    private boolean webViewAdded = false;

    Button cameraStreamButton;

    @Override
    protected String[] permissions() {
        return new String[]{CAMERA, ACCESS_WIFI_STATE, VIBRATE, INTERNET, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE};
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }



    private String getDeviceAlias(){
        if (deviceAlias == null){
            deviceAlias = getPreference(DEVICE_ALIAS_KEY, null);
            if (deviceAlias == null){
                deviceAlias = DEFAULT_DEVICE_ALIAS;
                savePreference(DEVICE_ALIAS_KEY, DEFAULT_DEVICE_ALIAS);
            }
        }
        return deviceAlias;
    }


    protected abstract boolean enableCameraStream();



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        hideTitle(); //this should be called before adding content
        super.onCreate(savedInstanceState);
    }

    protected void afterPermissionsGranted(Bundle savedInstanceState) {
        enterPortraitMode();
        keepScreenOn();

        //setup main layout
        final DroidDeviceServer context = this;
        page1 = new LinearLayout(context);
        page1.setOrientation(LinearLayout.VERTICAL);
        addContentView(page1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        page1.setGravity(Gravity.CENTER);

        serverHandler = new DefaultServerHandler();
        deviceStatusBuilder = new DeviceStatusBuilder();


        //camera stream setup
        if (enableCameraStream()) {

            camStreamResponse = new CamStreamResponse(mjpegResponse, jpegOfferResponse);
            wavRecorderResponse = new WavRecorderResponse();


            serverHandler.addRoot("/cam", new HTTPServerHandler.Wrap(
                    HttpResponse.html(StreamUtil.toString(FileUtil.findStream("src/main/resources#common/cam.html")))
            ));
            serverHandler.addRoot("/device-audio-live-stream.wav", new HTTPServerHandler.Wrap(wavRecorderResponse));
            serverHandler.addRoot("/device-cam-mjpeg-stream", new HTTPServerHandler.Wrap(mjpegResponse));
            serverHandler.addRoot("/jpeg-stream-test", new HTTPServerHandler.Wrap(jpegOfferResponse));

            deviceStatusBuilder.onStatusChangeRequest(new DeviceStatusBuilder.StatusChangeRequestEvent() {
                @Override
                public void onChangeRequest(DeviceStatusBuilder.Feature feature, String data) {
                    if ("true".equals(data)){
                        camStreamResponse.turnFlashLightOn();
                    }
                    else {
                        camStreamResponse.turnFlashLightOff();
                    }
                }
            });

            cameraStreamButton = new Button(context);
            cameraStreamButton.setText("Porneste camera wifi");
            cameraStreamButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startStreaming();
                }
            });
            page1.addView(cameraStreamButton);
        }



        deviceStat = deviceStatusBuilder.getDeviceStat();
        deviceStat.setAlias(getDeviceAlias());


        serverHandler.addRoot("/handshake", deviceStatusBuilder);
        serverHandler.addRoot("/device-stat", deviceStatusBuilder);



        trackBatteryChangeStatus(new BatteryChangeListener() {
            @Override
            public void onChange(int scale, int level, int plugged) {
                deviceStat.setBatteryScale(scale);
                deviceStat.setBatteryLevel(level);
            }
        });


        webView = new RAIDWebView(this);
        webView.setCloseButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.setVisibility(View.INVISIBLE);
                webView.loadUrl("about:blank");
            }
        });







        Button btn2 = new Button(context);
        btn2.setText("Scaneaza reteaua WiFi");
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scaneazaReteauaWifi();
            }
        });
        page1.addView(btn2);









        Button btn4 = new Button(context);
        btn4.setText("Scaneaza reteaua bluetooth");
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scaneazaReteauaBluetooth();
            }
        });
        page1.addView(btn4);

        Button btn5 = new Button(context);
        btn5.setText("Porneste camera bluetooth");
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pornesteCameraBluetooth();
            }
        });
        page1.addView(btn5);

        TextView textView = new TextView(context);
        textView.setText("Code name: " + getDeviceAlias());
        page1.addView(textView);

        deviceStat.scanIPV4(new NetworkUtil.IPIterator() {
            @Override
            public void onFound(String ip) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = new TextView(context);
                        textView.setText("connect at: " + ip + ":8080(ipwebcam) " + HTTP_PORT + "(this)");
                        page1.addView(textView);
                    }
                });
            }
        });


        startHTTPServer();


        //gaseste friendIps
        scanIPV4(new NetworkUtil.IPIterator() {
            @Override
            public void onComplete(String[] ips) {
                for (String ip: ips){
                    for (String possibleIp: findFriendsForIp(ip, 0)){
                        IPChecker.checkMyNet(possibleIp, HTTP_PORT, new IPChecker.Handler() {
                            @Override
                            public void onResponse(String ip, DeviceStat jsonObject) {
                                if (!friendIps.contains(ip)){
                                    friendIps.add(ip);
                                    deviceStat.setProp("friendIps", StringUtil.toCSV(friendIps));
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    BluetoothDevice currentDevice;

    public static final String SECURED_UUID_STR = "fa87c0d0-afac-11de-8a39-0800200c9a66";
    private static final String SERVER_NAME = "BluetoothChatSecure";

    public void pornesteCameraBluetooth(){
        this.withBluetoothDiscoverable(new BluetoothDiscovered() {
            @Override
            public void onAdapterDiscovered(BluetoothAdapter adapter) {
                log.info("XXX adapter discovered");
                startStreaming();
                ThreadUtil.startThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new com.astox.rapdroid.BluetoothServerSecure(adapter)
                                    .setRequestHandler(serverHandler)
                                    .setStateObserver(serverHandler)
                                    .addRequestBuilder(new HttpRequestBuilder())
                                    .setUuid(SECURED_UUID_STR)
                                    .setName(SERVER_NAME)
                                    .start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        log.info("XXX STARTED bluetooth");
                    }
                });
            }
        });

    }

    public void scaneazaReteauaBluetooth(){
        page1.removeAllViews();
        final DroidDeviceServer self = this;
        this.withBluetoothDiscoverable(new BluetoothDiscovered() {
            @Override
            public void onAdapterDiscovered(BluetoothAdapter adapter) {
                Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (final BluetoothDevice device : pairedDevices) {
                        Button scanBtn = new Button(self);
                        scanBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                currentDevice = device;
                            }
                        });
                        scanBtn.setText(device.getName() + " " + device.getAddress());
                        page1.addView(scanBtn);
                    }
                }

            }
        });
    }


    public void startHTTPServer(){
        //start httpage1p server
        startThread(new Runnable() {
            @Override
            public void run() {
                try {
                    new IOServer()
                            .setPort(HTTP_PORT)
                            .addRequestBuilder(new HttpRequestBuilder())
                            .addDuplexDraft(new WSDraft6455())
                            .setStateObserver(serverHandler)
                            .setRequestHandler(serverHandler)
                            .start();
                    toastLong("Server started on " + HTTP_PORT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Server-Main");

//        startStreaming();
    }

    public void startStreaming(){
        if (page1 != null) {
            page1.removeAllViews();
        }

        mPreview = new TextureView(this);
        mPreview.setLayoutParams(Layouts.LINEAR_MATCH_MATCH);
        mPreview.setRotation(90);

        addContentView(mPreview, Layouts.LINEAR_MATCH_MATCH);

        Button prevBtn = new Button(this);
        prevBtn.setText("preview");
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.setLayoutParams(Layouts.LINEAR_MATCH_MATCH);
                webView.setBackgroundColor(Color.RED);
                prevBtn.setVisibility(View.INVISIBLE);
                toggleWebView(webView, "http://localhost:" + HTTP_PORT + "/cam");
            }
        });

        addContentView(prevBtn, Layouts.LINEAR_WRAP_WRAP);

        camStreamResponse.setPreview(mPreview);
        camStreamResponse.startStream();


        wavRecorderResponse.startRecord();

        deviceStat.setProp(BROADCAST_KEY, true);

        deviceStat.scanIPV4(new NetworkUtil.IPIterator() {
            @Override
            public void onFound(String ip) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        prevBtn.setText("PREVIEW: " + ip + ":" + HTTP_PORT + "/cam");
                    }
                });
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camStreamResponse != null) {
            camStreamResponse.stop();
        }
        if (wavRecorderResponse != null){
            wavRecorderResponse.stopRecording();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkIP(String addr){
        final com.astox.rapdroid.RAPDroidActivity context = this;
        IPChecker.checkRandomHttp(addr, 8080, new IPChecker.RandHandler() {
            @Override
            public void onResponse(String ip, int statusCode, String content) {
                addButton("IP WEBCAM: " + ip, context, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String root = "http://"+ip+ ":8080/";
                        webView.playOnSoundThread(root + "audio.wav");
                        toggleWebView(webView, root + "jsfs.html");
                        enterLandscapeMode();
                        toastLong("URL: " + root);
                    }
                });
            }
        });

        IPChecker.checkMyNet(addr, HTTP_PORT, new IPChecker.Handler() {
            @Override
            public void onResponse(String ip, DeviceStat stat) {
                log.info(stat);
                if (stat.getBoolean(BROADCAST_KEY, false)){
                    addButton(stat.getAlias() + ": " + ip, context, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            friendIps.add(ip);
                            String root = "http://" + ip + ":" + HTTP_PORT + "/cam";
                            toggleWebView(webView, root);
                            enterLandscapeMode();
                            toastLong("About to load: " + root);
                        }
                    });
                    notificaStatusBaterie(stat, "broadcasting");
                }
                else {
                    notificaStatusBaterie(stat, "info");
                }
            }
        });
    }

    private void toggleWebView(RAIDWebView webView, String url){
        webView.loadUrl(url);
        if (webViewAdded){
            webView.setVisibility(View.VISIBLE);
        } else {
            addContentView(webView, Layouts.LINEAR_MATCH_MATCH);
            webViewAdded = true;
        }
    }

    private void notificaStatusBaterie(DeviceStat stat, String info){
        int percentage = stat.getBatteryPercentage();

        if (percentage < 30){
            notify(stat.getAlias() + " " + info, percentage + "%", "", true, true);
        }
        else if (percentage > 30 && percentage < 50){
            notify(stat.getAlias()+ " " + info, percentage + "%", "", false, false);
        }
        else if (percentage < 80){
            toastLong(stat.getAlias()+ " " + info, String.valueOf(stat.getBatteryPercentage()) + "% battery level");
        }
        else {
            log.info("IGNORE OK device " + stat.getAlias() + " at " + stat.getBatteryPercentage());
        }
    }


    public void addButton(String text, Activity activity, View.OnClickListener onClickListener){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button button = new Button(activity);
                button.setText(text);
                button.setOnClickListener(onClickListener);
                page1.addView(button);
            }
        });
    }


    private void scaneazaReteauaWifi(){
        //192.168.1.6
//        VisualQueue visualQueue = new VisualQueue();
//        visualQueue.setMainWindow(this);

        page1.removeAllViews();

        Button manBtn = new Button(this);
        manBtn.setText("Introdu URL manual");
        manBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog(DialogStyle.OK_INPUT_TEXT, "Titlu", "Introdu url", new Callback() {
                    @Override
                    public boolean executed(String text) {

                        return false;
                    }
                }, null);
            }
        });

        page1.addView(manBtn);


        scanIPV4(new NetworkUtil.IPIterator() {
            @Override
            public void onFound(String ip) {
                for (String s: findFriendsForIp(ip, 0)){
                    checkIP(s);
                }
            }
        });
    }





    @Override
    public void onDestroy() {
        super.onDestroy();
        wavRecorderResponse.stopRecording();
        try {
            camStreamResponse.stop();
        } catch (Exception e){

        }
    }
}

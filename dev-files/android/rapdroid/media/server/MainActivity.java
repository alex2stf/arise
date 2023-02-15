package com.arise.rapdroid.media.server;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.arise.core.AppSettings;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.AppCache;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.Util;
import com.arise.rapdroid.media.server.fragments.BrowserFragment;
import com.arise.rapdroid.media.server.fragments.CameraFragment;
import com.arise.rapdroid.media.server.fragments.LogFragment;
import com.arise.rapdroid.media.server.fragments.MediaCenterFragment;
import com.arise.rapdroid.media.server.fragments.MediaPlaybackFragment;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.Message;
import com.arise.rapdroid.RAPDroidActivity;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.VIBRATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.arise.rapdroid.media.server.AppUtil.TAB_POSITION;

public class MainActivity extends RAPDroidActivity {

    public static boolean IS_RUNNING = false;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    final BroadcastReceiver onMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Message msg = Message.fromMap((Map<String, Object>) Groot.decodeBytes(message));
//            chatFragment.onMessageReceiver(msg);
        }
    };
    MediaPlaybackFragment mediaPlaybackFragment;
    final BroadcastReceiver onStopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaPlaybackFragment.stop();
            browserFragment.onStop();
        }
    };
    final BroadcastReceiver onPlaylistPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaPlaybackFragment.stopAndPlayPlaylist();
        }
    };

    final BroadcastReceiver onStartServerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AppUtil.log.info("server started");
            if (mediaCenterFragment != null){
                mediaCenterFragment.reloadApp();
            }
        }
    };


    volatile boolean cam_running = false;

    final BroadcastReceiver onDeviceUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AppUtil.log.info("received device update");
            int camId = -1;
            try {
                camId = Integer.valueOf( intent.getStringExtra("camId"));
            }catch (Exception e){
                camId = -1;
            }

            boolean startCam = "true".equalsIgnoreCase(intent.getStringExtra("camEnabled"));
            boolean stopCam = "false".equalsIgnoreCase(intent.getStringExtra("camEnabled"));
            String lightMode = intent.getStringExtra("lightMode");

            String snapshot = intent.getStringExtra("takeSnapshot");

            if (StringUtil.hasText(snapshot)){
                startCam = true;
            }


            if (stopCam){
                cameraFragment.onStop();
                cam_running = false;
            } else if (startCam ){
                cam_running = true;
            }

            if (cam_running){
                cameraFragment.checkUpdateState(camId, lightMode);

                if (StringUtil.hasText(snapshot)){
                    cameraFragment.takeSnapshot();
                }
            }

            
            String musicVolume = intent.getStringExtra("musicVolume");
            if (StringUtil.hasText(musicVolume)){
                Integer val = null;
                try {
                    val = Integer.valueOf(musicVolume);
                }catch (Exception e){
                    e.printStackTrace();
                    val = null;
                }

                if (val != null){
                    Integer finalVal = val;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, finalVal, 0);
                        }
                    });
                }
            }




        }
    };




    final BroadcastReceiver onOpenFileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaPlaybackFragment.stop();

            String path = ContentInfo.decodePath(
                    intent.getStringExtra("path")
            );



            if (isUrl(path)){
                browserFragment.onStop();
                browserFragment.loadUrl(path);
                showBrowserFragment();
            }
            else  {
                File file = ContentInfo.fileFromPath(path);
                if (!file.exists()){
                    return;
                }
                ContentInfo contentInfo = AppUtil.DECODER.decode(file);

                if (contentInfo != null){
                    contentInfo.setPosition(0);
                    mediaPlaybackFragment.play(contentInfo);
                    focusMediaPlaybackFragment();
                }
            }


        }
    };
    private int restColors[][] = new int[][]{
            {Color.WHITE , Color.parseColor("#5e5796"), Color.parseColor("#817bb3"), Color.parseColor("#a5a1c9"), Color.parseColor("#c6c3de"), Color.parseColor("#e1dfed")}, //tab1
            {Color.parseColor("#8f8f8f"), Color.WHITE , Color.parseColor("#8f8f8f"), Color.parseColor("#adadad"), Color.parseColor("#c4c4c4"), Color.parseColor("#dbdbdb")}, //tab2
            {Color.parseColor("#65a6a3"), Color.parseColor("#2a7370"), Color.WHITE, Color.parseColor("#2a7370"), Color.parseColor("#65a6a3"), Color.parseColor("#abccca")}, //tab3
            {Color.parseColor("#7d729c"), Color.parseColor("#a295c4"), Color.parseColor("#cdc5e3"), Color.WHITE, Color.parseColor("#cdc5e3"), Color.parseColor("#a295c4")}, //tab4
            {Color.parseColor("#4d4d4d"), Color.parseColor("#3b3b3b"), Color.parseColor("#292929"), Color.parseColor("#1f1f1f"), Color.WHITE, Color.parseColor("#1f1f1f")}, //tab5
            {Color.parseColor("#c7b793"), Color.parseColor("#d4c6a7"), Color.parseColor("#e3d8bf"), Color.parseColor("#ede6d5"), Color.parseColor("#faf4e6"), Color.WHITE} //tab6
    };


    MediaCenterFragment mediaCenterFragment;
    LogFragment logFragment;
    BrowserFragment browserFragment;
    AppViewPager viewPager;
    CameraFragment cameraFragment;
    int tabPosition = 0;
    Object[][] pages;
    int previousPosition = 0;
    private volatile boolean tabInit = false;

    static AudioManager audioManager = null;


    public static boolean isUrl(String s){
        URL url;
        try {
            url = new URL(s);
        } catch (MalformedURLException e) {
            return false;
        }
        return url != null;
    }

    public static int getMusicVolume(){
        if (audioManager != null){
            return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        return 0;
    }

    public static int getMusicMaxVoume(){
        if (audioManager != null){
            return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        return  0;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IS_RUNNING = true;
        super.onCreate(savedInstanceState);
        Util.registerContext(this);
        ContentType.AUDIO_MPEG_3.setResId(R.drawable.ic_treble_clef);
        this.hideTitle();
        SharedPreferences worker = getSharedPreferences("prefs", MODE_PRIVATE);
        AppCache.setWorker(worker);
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }

        tabPosition = AppCache.getInt(TAB_POSITION, 0);


        if ( AppCache.getBoolean(AppUtil.FORCE_LANDSCAPE)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            //enter full screen
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }


        //logging adapter
        logFragment = new LogFragment();

        Mole.addAppender(logFragment);

        browserFragment = new BrowserFragment();

        mediaPlaybackFragment = new MediaPlaybackFragment();

        mediaCenterFragment = new MediaCenterFragment();

        viewPager = new AppViewPager(this){};
        viewPager.setId(viewPager.hashCode());

        cameraFragment = new CameraFragment();


        pages = new Object[][]{
                {mediaCenterFragment, "", Icons.tab1Background, R.drawable.ic_tab_playlists, R.drawable.ic_tab_playlists_disabled},
                {browserFragment, "", Icons.tab2Background, R.drawable.ic_tab_web, R.drawable.ic_tab_web_disabled },
                {cameraFragment, "", Icons.tab3Background, R.drawable.ic_tab_chat, R.drawable.ic_tab_chat_disabled },
                {mediaPlaybackFragment, "", Color.BLACK, R.drawable.ic_tab_media, R.drawable.ic_tab_media_disabled },
                {logFragment, "!", Icons.tab7Background}
        };

        AppPageAdapter appPageAdapter;
        appPageAdapter = new AppPageAdapter(getSupportFragmentManager(), this);
        for (int i = 0; i < pages.length; i++){
            androidx.fragment.app.Fragment fragment = (androidx.fragment.app.Fragment) pages[i][0];
            String title = (String) pages[i][1];
            appPageAdapter.add(fragment, title);
        }
        viewPager.setOffscreenPageLimit(appPageAdapter.getCount());
        viewPager.setAdapter(appPageAdapter);
        viewPager.setTouchEnabled(false);



        __register_receivers();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        TabLayout tabLayout = new TabLayout(this);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setTabTextColors(Color.GRAY, Color.GRAY);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                System.out.println(" PAGE SELECTED: " + position);
                tryUpdateTabColors(tabLayout, position, pages);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                System.out.println(tab);
                tryUpdateTabColors(tabLayout, tab.getPosition(), pages);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                previousPosition = tab.getPosition();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                System.out.println("tab onTabReselected" + tab);
                tryUpdateTabColors(tabLayout, tab.getPosition(), pages);
            }
        });

        tryUpdateTabColors(tabLayout, tabPosition, pages);


        root.addView(tabLayout, com.arise.rapdroid.components.ui.Layouts.matchParentWrapContent());
        root.addView(viewPager, com.arise.rapdroid.components.ui.Layouts.Linear.matchParentMatchParent());


        setContentView(root, com.arise.rapdroid.components.ui.Layouts.Linear.matchParentMatchParent());



        if (arePermissionsGranted()){
            this.afterPermissionsGranted(savedInstanceState);
        } else {
            ActivityCompat.requestPermissions(this, permissions(), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }

        if (AppSettings.isTrue(AppSettings.Keys.KEEP_SCREEN_ON)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    private volatile boolean receivers_registered = false;


    private void __unregister_receivers(){
        safeUnregisterReceiver(onStartServerReceiver);
        safeUnregisterReceiver(onMessageReceiver);
        safeUnregisterReceiver(onOpenFileReceiver);
        safeUnregisterReceiver(onStopReceiver);
        safeUnregisterReceiver(onPlaylistPlayReceiver);
        safeUnregisterReceiver(onDeviceUpdateReceiver);
        receivers_registered = false;
    }

    private void __register_receivers() {
        if (receivers_registered){
            return;
        }
        this.registerReceiver(onStartServerReceiver, new IntentFilter("onStart"));
        this.registerReceiver(onMessageReceiver, new IntentFilter("onMessage"));
        this.registerReceiver(onOpenFileReceiver, new IntentFilter("weland.openFile"));
        this.registerReceiver(onStopReceiver, new IntentFilter("weland.closeFile"));
        this.registerReceiver(onPlaylistPlayReceiver, new IntentFilter("weland.onPlaylistPlay"));
        this.registerReceiver(onDeviceUpdateReceiver, new IntentFilter("weland.onDeviceUpdate"));
        receivers_registered = true;
    }

    @Override
    protected void afterPermissionsGranted(@Nullable Bundle savedInstanceState) {

        startServerService();

        new SamsungDevice(this).discover();

        autoGoToTab(tabPosition);

    }

    private void tryUpdateTabColors(TabLayout tabLayout, int position, Object[][] sources){
//        previousPosition = tabPosition;
        tabPosition = position;
        for (int i = 0; i < sources.length; i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (i == position){
                trySetTabColor(tab, (int) sources[i][2]);
                if (sources[i].length > 3) {
                    tab.setIcon((int) sources[i][3]);
                }
            }
            else {
                if (sources[i].length > 4) {
                   try {
                       tab.setIcon((int) sources[i][4]);
                   }catch (Throwable t){
                       System.out.println("RESOURCE NOT FOUND EXCEPTION");
                       t.printStackTrace();
                   }
                }
                trySetTabColor(tab, restColors[position][i]);

            }
        }

    }

    private void trySetTabColor(TabLayout.Tab tab, int xx){
        try {
            Field field = tab.getClass().getField("view");
            field.setAccessible(true);
            View view = (View) field.get(tab);
            view.setBackgroundColor(xx);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void startServerService(){
        if (!isServerRunning()){
            Intent intent = new Intent(this, ServerService.class);
            ContextCompat.startForegroundService(this, intent);
        }
    }

    public boolean isServerRunning(){
        return isMyServiceRunning(ServerService.class);
    }

    @Override
    protected String[] permissions() {
        return new String[]{
                INTERNET,
                BLUETOOTH,
                BLUETOOTH_ADMIN,
                READ_EXTERNAL_STORAGE,
                CHANGE_WIFI_MULTICAST_STATE,
                CAMERA,
                ACCESS_WIFI_STATE,
                VIBRATE,
                WRITE_EXTERNAL_STORAGE,
                RECORD_AUDIO};
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }




    @Override
    protected void onPause() {
        if (mediaPlaybackFragment != null) {
            mediaPlaybackFragment.onPause();
        }
        if (browserFragment != null) {
            browserFragment.onPause();
        }

        if (mediaCenterFragment != null){
            mediaCenterFragment.onPause();
        }

        if (cameraFragment != null){
            cameraFragment.onStop();
        }
        AppCache.putInt(TAB_POSITION, tabPosition);
        IS_RUNNING = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        IS_RUNNING = true;
        __register_receivers();
        if (cameraFragment != null){
            cameraFragment.onResume();
        }

        if (mediaCenterFragment != null){
            mediaCenterFragment.onResume();
        }

        if (browserFragment != null){
            browserFragment.onResume();
        }
        super.onResume();
    }


    @Override
    protected void onStop() {
        AppCache.putInt(TAB_POSITION, tabPosition);
        if (mediaPlaybackFragment != null) {
            mediaPlaybackFragment.onStop();
        }
        if (browserFragment != null) {
            browserFragment.onStop();
        }
        if (mediaCenterFragment != null){
            mediaCenterFragment.onStop();
        }
        if (cameraFragment != null){
            cameraFragment.onStop();
        }
        __unregister_receivers();
        IS_RUNNING = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        AppCache.putInt(TAB_POSITION, tabPosition);
        mediaPlaybackFragment.saveState();

        if (browserFragment != null) {
            browserFragment.onDestroy();
        }

        if (mediaCenterFragment != null){
            mediaCenterFragment.onDestroy();
        }

        if (cameraFragment != null){
            cameraFragment.onDestroy();
        }
        __unregister_receivers();
        IS_RUNNING = false;
        super.onDestroy();
    }

    private void safeUnregisterReceiver(BroadcastReceiver receiver){
        try {
            unregisterReceiver(receiver);
        }catch (Throwable t){

        }
    }

    public void focusMediaPlaybackFragment() {
        autoGoToTab(2);
    }

    private void showBrowserFragment() {
        autoGoToTab(1);
    }


    public synchronized void autoGoToTab(int index){
        if (tabInit){
            return;
        }
        tabInit = true;
        previousPosition = tabPosition;

        viewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                tabPosition = index;
                AppCache.putInt(TAB_POSITION, tabPosition);
                viewPager.setCurrentItem(index, true);
                tabInit = false;
            }
        }, 100);
    }


    @Override
    public void onBackPressed() {
        if (tabInit){
            return;
        }
        System.out.println("BACK PRESSED");
        switch (tabPosition){
            case 1: //browser fragment
                if (browserFragment != null){
                    browserFragment.goBack();
                }
                return;
        }

        autoGoToTab(previousPosition);
    }



    public void showMediaCenterFragment() {
        autoGoToTab(0);
    }

    public void showSendUrlOptions(String url) {
        showMediaCenterFragment();
        mediaCenterFragment.showSendUrlOptions(url);
    }

    @Override
    public void finish() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAndRemoveTask();
        }
        else {
            super.finish();
        }
    }
}

package com.arise.droid;

import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.VIBRATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.CAMERA;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import static com.arise.core.AppSettings.Keys.RADIO_ENABLED;
import static com.arise.core.AppSettings.Keys.RADIO_SHOWS_PATH;
import static com.arise.core.AppSettings.getProperty;
import static com.arise.core.AppSettings.isTrue;
import static com.arise.droid.AppUtil.APP_STATE;
import static com.arise.droid.AppUtil.ON_START;
import static com.arise.droid.AppUtil.OPEN_PATH;
import static com.arise.droid.AppUtil.PATH;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arise.core.AppSettings;
import com.arise.core.models.Handler;
import com.arise.core.tools.Mole;
import com.arise.core.tools.Util;
import com.arise.droid.fragments.AppFragment;
import com.arise.droid.fragments.BrowserFragment;
import com.arise.droid.fragments.MediaPlaybackFragment;
import com.arise.droid.tools.AppPageAdapter;
import com.arise.droid.tools.AppViewPager;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.impl.RadioPlayer;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    public static volatile boolean IS_RUNNING = true;

    private static final Mole log = Mole.getInstance(MainActivity.class);

    private static Context ctx;


    public static Context getStaticAppContext(){
        return ctx;
    }
    AppFragment appFragment;
    MediaPlaybackFragment mediaPlaybackFragment;
    BrowserFragment browserFragment;

    protected String[] permissions() {
        return new String[]{
                INTERNET,
                BLUETOOTH,
                BLUETOOTH_ADMIN,
                READ_EXTERNAL_STORAGE,
                CHANGE_WIFI_MULTICAST_STATE,
                CAMERA,
                NETWORK_STATS_SERVICE,
                ACCESS_WIFI_STATE,
                VIBRATE,
                WRITE_EXTERNAL_STORAGE,
                RECORD_AUDIO};
    }

    protected boolean arePermissionsGranted() {
        for (String permission : permissions()){
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startBackgroundService(){
        if (!isMyServiceRunning(ServerService.class)) {
            startService(new Intent(MainActivity.this, ServerService.class));
            log.info("Service started");
        }
        else {
            log.info("Service already started");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IS_RUNNING = true;
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Util.registerContext(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }



        if (arePermissionsGranted()){
            this.withPermissionsGranted();
        }
        else {
            System.out.println(Build.VERSION.SDK_INT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, permissions(), REQUEST_ID_MULTIPLE_PERMISSIONS);
            } else {
                this.withPermissionsGranted();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        withPermissionsGranted();
    }

    private volatile boolean _app_started = false;

    private void withPermissionsGranted() {
        if (_app_started){
            return;
        }
        AppPageAdapter appPageAdapter;
        appPageAdapter = new AppPageAdapter(getSupportFragmentManager(), this);
        appFragment = new AppFragment();
        mediaPlaybackFragment = new MediaPlaybackFragment();
        browserFragment = new BrowserFragment();
        appPageAdapter.add(appFragment, "App");
        appPageAdapter.add(mediaPlaybackFragment, "Media");
        appPageAdapter.add(browserFragment, "Browse");



        AppViewPager viewPager = new AppViewPager(this){};
        viewPager.setId(viewPager.hashCode());

        viewPager.setOffscreenPageLimit(appPageAdapter.getCount());
        viewPager.setAdapter(appPageAdapter);
        viewPager.setTouchEnabled(false);

        TabLayout tabLayout = new TabLayout(this);
        tabLayout.setupWithViewPager(viewPager);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        root.addView(tabLayout, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        root.addView(viewPager, new LayoutParams(MATCH_PARENT, MATCH_PARENT));


        setContentView(root,new LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        __register_receivers();

        this.startBackgroundService();
        _app_started = true;
    }

    /**
     * receivers and registration:
     */
    private volatile boolean _rec_reg = false;

    final BroadcastReceiver onStartServer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AppUtil.log.info("server started");
            if (appFragment != null){
                appFragment.reloadApp();
            }
        }
    };


    final BroadcastReceiver onOpenPath = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String path = ContentInfo.decodePath(
                    intent.getStringExtra(PATH)
            );
            System.out.println("open" + path);
        }
    };

    private void __register_receivers(){
        if (_rec_reg){
            return;
        }
        this.registerReceiver(onStartServer, new IntentFilter(ON_START));
        this.registerReceiver(onOpenPath, new IntentFilter(OPEN_PATH));
        _rec_reg = true;
    }

    private void __unregister_receivers(){
        _rec_reg = false;
        this._safe_unregister(onStartServer);
        this._safe_unregister(onOpenPath);
    }

    @Override
    protected void onStop() {
        IS_RUNNING = false;
        __unregister_receivers();
        super.onStop();
    }

    private void _safe_unregister(BroadcastReceiver receiver){
        try {
            unregisterReceiver(receiver);
        }catch (Throwable t){

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.info("App onDestroy");
    }
}

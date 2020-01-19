package com.arise.rapdroid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.Util;
import com.arise.core.models.DialogStyle;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * basic helper activity
 */
public abstract class RAPDroidActivity extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static final int ENABLE_BLUETOOTH = 200;
    private static final int DISCOVER_BLUETOOTH = 300;
    private static final int PICKFILE_RESULT_CODE = 2;

    private static final Mole log = Mole.getInstance(RAPDroidActivity.class);

    private static final Map<DialogStyle, Integer[]> resMap = new HashMap<>();
    private static final long[] STAR_WARS_PATTERN = new long[]{0, 500, 110, 500, 110, 450, 110, 200, 110, 170, 40, 450, 110, 200, 110, 170, 40, 500};


    static {
        resMap.put(DialogStyle.OK, new Integer[]{android.R.style.Theme_Material_Dialog, android.R.drawable.ic_dialog_dialer, android.R.string.ok, null});
        resMap.put(DialogStyle.OK_INFO, new Integer[]{android.R.style.Theme_Material_Dialog_Alert, android.R.drawable.ic_dialog_info, android.R.string.ok, null});
        resMap.put(DialogStyle.OK_WARNING, new Integer[]{android.R.style.Theme_DeviceDefault_Dialog, android.R.drawable.ic_dialog_alert, android.R.string.ok, null});
        resMap.put(DialogStyle.OK_ERROR, new Integer[]{android.R.style.Theme_DeviceDefault_Dialog_Alert, android.R.drawable.ic_dialog_alert, android.R.string.ok, null});
        resMap.put(DialogStyle.OK_INPUT_TEXT, new Integer[]{android.R.style.Theme_DeviceDefault_Dialog_Alert, android.R.drawable.ic_dialog_alert, android.R.string.ok, null, InputType.TYPE_CLASS_TEXT});
        resMap.put(DialogStyle.OK_INPUT_PASSWORD, new Integer[]{android.R.style.Theme_DeviceDefault_Dialog_Alert, android.R.drawable.ic_dialog_alert, android.R.string.ok, null, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD});

        resMap.put(DialogStyle.OK_CANCEL, new Integer[]{android.R.style.Theme_Material_Dialog, android.R.drawable.ic_dialog_dialer, android.R.string.ok, android.R.string.cancel});
        resMap.put(DialogStyle.OK_CANCEL_INFO, new Integer[]{android.R.style.Theme_Material_Dialog_Alert, android.R.drawable.ic_dialog_info, android.R.string.ok, android.R.string.cancel});
        resMap.put(DialogStyle.OK_CANCEL_WARNING, new Integer[]{android.R.style.ThemeOverlay_Material_Dialog, android.R.drawable.ic_dialog_alert, android.R.string.ok, android.R.string.cancel});
        resMap.put(DialogStyle.OK_CANCEL_ERROR, new Integer[]{android.R.style.ThemeOverlay_Material_Dialog_Alert, android.R.drawable.ic_dialog_alert, android.R.string.ok, android.R.string.cancel});
        resMap.put(DialogStyle.OK_CANCEL_INPUT_TEXT, new Integer[]{android.R.style.ThemeOverlay_Material_Dialog_Alert, android.R.drawable.ic_dialog_alert, android.R.string.ok, android.R.string.cancel, InputType.TYPE_CLASS_TEXT});
        resMap.put(DialogStyle.OK_CANCEL_INPUT_PASSWORD, new Integer[]{android.R.style.ThemeOverlay_Material_Dialog_Alert, android.R.drawable.ic_dialog_alert, android.R.string.ok, android.R.string.cancel, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD});

        resMap.put(DialogStyle.YES_NO, new Integer[]{android.R.style.Theme_DeviceDefault_Light_Dialog, android.R.drawable.ic_dialog_dialer, android.R.string.yes, android.R.string.no});
        resMap.put(DialogStyle.YES_NO_INFO, new Integer[]{android.R.style.Theme_DeviceDefault_Light_Dialog_Alert, android.R.drawable.ic_dialog_info, android.R.string.yes, android.R.string.no});
        resMap.put(DialogStyle.YES_NO_WARNING, new Integer[]{android.R.style.Theme_Holo_Dialog, android.R.drawable.ic_dialog_alert, android.R.string.yes, android.R.string.no});
        resMap.put(DialogStyle.YES_NO_ERROR, new Integer[]{android.R.style.Theme_Material_Light_Dialog_Alert, android.R.drawable.ic_dialog_alert, android.R.string.yes, android.R.string.no});
    }

    @Nullable
    private Bundle savedInstanceState;
    private NotificationManager notificationManager;


    private NotificationChannel notificationChannel;
    private BatteryChangeReceiver batteryChangeReceiver;
    private List<String> permissionsToRequest = new ArrayList<>();
    private BluetoothEnabled bluetoothEnabled;
    private BluetoothDiscovered bluetoothDiscovered;
    private BluetoothAdapter bluetoothAdapter = null;
    private FileChooseHandler fileChooseHandler = null;

    public static void removeView(View view) {
        if (view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }


    public void chooseFile(FileChooseHandler fileChooseHandler) {
        fileChooseHandler = fileChooseHandler;
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
    }


    public AlertDialog dialog(DialogStyle style, String title, String text, final Callback okAction, final Callback negativeAction) {

        int themeResId = android.R.style.Theme_Material_Dialog_Alert;
        int iconResId = android.R.drawable.ic_dialog_alert;
        Integer okbtn = null, cancelBtn = null, inputMode = null;
        if (resMap.containsKey(style)) {
            Integer[] args = resMap.get(style);
            themeResId = args[0];
            iconResId = args[1];
            okbtn = args[2];
            cancelBtn = args[3];

            if (args.length > 4) {
                inputMode = args[4];
            }

        }


        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ctx(), themeResId);
        } else {
            builder = new AlertDialog.Builder(ctx());
        }
        builder.setTitle(title)
                .setMessage(text);

        final EditText input;
        if (inputMode != null) {
            input = new EditText(ctx());
            input.setInputType(inputMode);
            builder.setView(input);
        } else {
            input = null;
        }


        if (okbtn != null && okAction != null) {
            builder.setPositiveButton(okbtn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    okAction.executed((String.valueOf(input != null ? input.getText() : "")));
                }
            });
        }


        if (cancelBtn != null && negativeAction != null) {
            builder.setNegativeButton(cancelBtn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (negativeAction != null) {
                        negativeAction.executed((String.valueOf(input != null ? input.getText() : "")));
                    }
                }
            });
        }


        return builder.setIcon(iconResId).show();
    }

    public void dialogNotify(String title, String text, long lifetime) {
        final AlertDialog dialog = this.dialog(DialogStyle.OK, title, text, new Callback() {
            @Override
            public boolean executed(String data) {
                return false;
            }
        }, null);

        Util.ThreadFactory.runLater(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    try {
                        dialog.hide();
                    } catch (Exception e) {
                    }

                    try {
                        dialog.dismiss();
                    } catch (Exception e) {
                    }
                }
            }
        }, lifetime);
    }

    public void enterFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void toastLong(final Object... args) {
        final Context self = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(self, StringUtil.join(args, " "), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void toastShort(final Object... args) {
        final Context self = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(self, StringUtil.join(args, " "), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void hideTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void enterPortraitMode() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void enterLandscapeMode() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    protected void savePreference(String key, String value) {
        getPreferences(Context.MODE_PRIVATE).edit().putString(key, value).commit();
    }

    protected void savePreference(String key, int value) {
        getPreferences(Context.MODE_PRIVATE).edit().putInt(key, value).commit();
    }

    protected String getPreference(String key, String defaultValue) {
        return getPreferences(Context.MODE_PRIVATE).getString(key, defaultValue);
    }

    protected int getPreference(String key, int defaultValue) {
        return getPreferences(Context.MODE_PRIVATE).getInt(key, defaultValue);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        Util.registerContext(this);

        if (areCameraPermissionGranted()){
            this.afterPermissionsGranted(savedInstanceState);
        } else {
            ActivityCompat.requestPermissions(this, permissions(), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
    }

    /**
     * check if all permissions defined inside {@link RAPDroidActivity#permissions()} are enabled
     * @return
     */
    boolean areCameraPermissionGranted() {
        for (String permission : permissions()){
            if (!(ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)){
                return false;
            }
        }
        return true;
    }

    /**
     * makes the bluetooth device discoverable if it's not discovered and then calls calls {@link RAPDroidActivity#withBluetoothEnabled(BluetoothEnabled)}
     * @param bluetoothDiscovered
     */
    protected void withBluetoothDiscoverable(final BluetoothDiscovered bluetoothDiscovered) {
        this.bluetoothDiscovered = bluetoothDiscovered;
        withBluetoothEnabled(new BluetoothEnabled() {
            @SuppressLint("MissingPermission")
            @Override
            public void onAdapterEnabled(BluetoothAdapter adapter) {
                if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(discoverableIntent, DISCOVER_BLUETOOTH);
                } else {
                    bluetoothDiscovered.onAdapterDiscovered(adapter);
                }
            }
        });
    }


    public void getBluetoothBondedDevices(BluetoothBondedHandler bluetoothBondedHandler){
        withBluetoothDiscoverable(new BluetoothDiscovered() {
            @Override
            public void onAdapterDiscovered(BluetoothAdapter adapter) {
                Set<BluetoothDevice> mPairedDevices = adapter.getBondedDevices();
                bluetoothBondedHandler.onFound(adapter, mPairedDevices);
            }
        });
    }



    /**
     * enables the bluetooth device if it's not already enabled
     * @param bluetoothEnabled
     */
    @SuppressLint("MissingPermission")
    protected void withBluetoothEnabled(BluetoothEnabled bluetoothEnabled) {
        this.bluetoothEnabled = bluetoothEnabled;

        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        //enable bluetooth on this device
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, ENABLE_BLUETOOTH);
        } else {
            bluetoothEnabled.onAdapterEnabled(bluetoothAdapter);
        }
    }

    @Override
    protected final void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("BLUE", requestCode + " " + resultCode + " " + data);
        switch (requestCode){
            case ENABLE_BLUETOOTH:
                bluetoothEnabled.onAdapterEnabled(bluetoothAdapter);
                break;
            case DISCOVER_BLUETOOTH:
                bluetoothDiscovered.onAdapterDiscovered(bluetoothAdapter);
                break;
            case PICKFILE_RESULT_CODE:
                if (resultCode == -1) {
                    if (fileChooseHandler != null){
                        fileChooseHandler.onFileSelected(new File(data.getData().getPath()));
                    }
                }
                break;
        }

    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        int res = 0;

        if (REQUEST_ID_MULTIPLE_PERMISSIONS != requestCode) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }


        boolean areAllPermissionsGranted = true;
        for (int result : grantResults){
            if (result != PackageManager.PERMISSION_GRANTED){
                areAllPermissionsGranted = false;
                break;
            }
        }
        if (areAllPermissionsGranted){
            this.afterPermissionsGranted(savedInstanceState);
        } else {
            // User denied one or more of the permissions, without these we cannot record
            // Show a toast to inform the user.
            Toast.makeText(getApplicationContext(),
                    "PERMISIUNI TREBUIE",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    protected abstract String[] permissions();

    protected void afterPermissionsGranted(@Nullable Bundle savedInstanceState){
        log.info("Permissions granted!!!");
    };

    @Override
    protected void onNewIntent(Intent intent) {
        //intent.getExtras().get("NUME")
        //TODO treat notification callbacks
        super.onNewIntent(intent);
    }

    public NotificationChannel getNotificationChannel() {
        if (notificationChannel == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel(notificationChannelId(), notificationChannelName(), NotificationManager.IMPORTANCE_DEFAULT);
                // Configure the notification channel.
                notificationChannel.setDescription(notificationChannelDescription());
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
            }
        }
        return notificationChannel;
    }

    public String notificationChannelId() {
        return "my_channel_id_01";
    }

    public String notificationChannelDescription() {
        return "Channel description";
    }

    public String notificationChannelName() {
        return "My channel name";
    }

    public NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) ctx().getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(notificationChannel);
            }

        }
        return notificationManager;
    }

    public void notify(String title, String text, String action, boolean playSound, boolean vibrate) {
        notify(title, text, action, playSound, vibrate, STAR_WARS_PATTERN);
    }

    public void notify(String title, String text, String action, boolean playSound, boolean vibrate, long[] vibratePattern) {
        int notId = (int) System.currentTimeMillis();
        int iconId = getMainIcon();


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx(), notificationChannelId())
                        .setSmallIcon(iconId)
                        .setContentTitle(title)
                        .setContentText(text);

        //ptr poza
//        .setStyle(new NotificationCompat.BigTextStyle()
//                .bigText(emailObject.getSubjectAndSnippet()))

        if (playSound) {
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(soundUri);
        }


        Intent intent = new Intent(ctx(), ctx().getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("NUME", action);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        ctx(), 0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        mBuilder.setContentIntent(resultPendingIntent);


        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.contentIntent = resultPendingIntent;

        if (vibrate) {
            vibrate(vibratePattern, -1);
        }

        // Builds the notification and issues it.
        getNotificationManager().notify(notId, notification);
    }

    @SuppressLint("MissingPermission")
    protected void vibrate(long[] pattern, int repeat) {
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(pattern, repeat);
    }

    private int getMainIcon() {
        return android.R.drawable.ic_notification_overlay;
    }

    private Context ctx() {
        return this;
    }

    public int getScreenOrientation() {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    public void trackBatteryChangeStatus(BatteryChangeListener batteryChangeListener) {
        if (batteryChangeReceiver == null) {
            batteryChangeReceiver = new BatteryChangeReceiver(batteryChangeListener);
            this.registerReceiver(batteryChangeReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
    }

    private void requestPermission(String permission) {
        //check API version, do nothing if API version < 23!
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion > android.os.Build.VERSION_CODES.LOLLIPOP) {

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    log.info("EXPLANATION REQUIRED");
                } else {
                    log.info("GRANTED!!!");
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this, new String[]{permission}, 1);
                }
            } else {
                log.info("ALREDAY GRANTED");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryChangeReceiver != null) {
            unregisterReceiver(batteryChangeReceiver);
        }
    }

    public interface Callback {
        boolean executed(String data);
    }

    public interface BatteryChangeListener {
        void onChange(int scale, int level, int plugged);
    }

    public interface BluetoothDiscovered {
        void onAdapterDiscovered(BluetoothAdapter adapter);
    }

    public interface BluetoothBondedHandler {
        void  onFound(BluetoothAdapter adapter, Set<BluetoothDevice> bondedDevices);
    }

    public interface BluetoothEnabled {
        void onAdapterEnabled(BluetoothAdapter adapter);
    }

    public interface FileChooseHandler {
        void onFileSelected(File file);
    }





    public static class BatteryChangeReceiver extends BroadcastReceiver {
        private final BatteryChangeListener x;

        public BatteryChangeReceiver(BatteryChangeListener x) {
            this.x = x;
        }

        @Override
        public void onReceive(Context c, Intent i) {
            try {
                int s = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int l = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int p = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0); //2 = true
                x.onChange(s, l, p);
            } catch (Exception e) {

            }
        }
    }


}

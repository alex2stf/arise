package com.arise.rapdroid.net;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.arise.core.tools.Mole;

public class NetworkWrapper {


    private final Activity activity;
    private Mole log = Mole.getInstance(NetworkWrapper.class);

    public NetworkWrapper(Activity activity){
        this.activity = activity;
    }

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    private void connectToWPAWiFi(String ssid, String pass){
        if (isConnectedTo(ssid)){
            log.info("deja conectat la " + ssid);
            return;
        }
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wifiConfiguration = getWiFiConfig(ssid);
        if (wifiConfiguration == null){
            createWPAProfile(ssid, pass);
            wifiConfiguration = getWiFiConfig(ssid);
        }
        wifiManager.disconnect();
        wifiManager.enableNetwork(wifiConfiguration.networkId, true);
        wifiManager.reconnect();
    }


    private boolean isConnectedTo(String ssid) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.getConnectionInfo().getSSID() == ssid){
            return true;
        }
        return false;
    }


    private WifiConfiguration getWiFiConfig(String ssid) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        for(WifiConfiguration configuration: wifiManager.getConfiguredNetworks()){
            if (configuration.SSID != null && configuration.SSID.equals(ssid)){
                return configuration;
            }
        }
        return null;
    }


    private void createWPAProfile(String ssid, String pass){
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = String.format("\"%s\"", ssid);
        conf.preSharedKey = String.format("\"%s\"", pass);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);
    }

    private void wifiConnect(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);



        if (!mWifi.isConnected() || !checkWifiOnAndConnected()) {
            log.info("CONNECT TO NETWORK");
            try {
//                connectToWPAWiFi("DIGI-852C", "racanel2017");
            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }

    private Object getSystemService(String connectivityService) {
        return activity.getSystemService(connectivityService);
    }

    private Context getApplicationContext() {
        return activity.getApplicationContext();
    }
}

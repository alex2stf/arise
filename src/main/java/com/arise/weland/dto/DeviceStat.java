package com.arise.weland.dto;

import com.arise.core.tools.MapUtil;
import com.arise.core.tools.NetworkUtil;
import com.arise.core.tools.ReflectUtil;
import com.arise.core.tools.SYSUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.arise.core.tools.StringUtil.jsonVal;

public class DeviceStat {

    protected Set<String> ipv4Addrs;
    private int batteryScale;
    private int batteryLevel;
    private String deviceName;
    private SYSUtils.OS os;
    private List<Screen> screens = new ArrayList<>();
    private String serverStatus;
    private String displayName;
    private String serverUUID;
    private String conversationId;

    private boolean pbr;
    private boolean dbr;
    private boolean ubr;
    private boolean cbr;
    private boolean apm;



    public static DeviceStat fromMap(Map obj) {
        DeviceStat deviceStat = new DeviceStat(false);
        deviceStat.ipv4Addrs = MapUtil.getSet(obj, "ipv4Addrs");
        deviceStat.batteryLevel = MapUtil.getInt(obj, "batteryLevel", 0);
        deviceStat.batteryScale = MapUtil.getInt(obj,"batteryScale", 0);
        deviceStat.deviceName = MapUtil.getString(obj, "deviceName");
        deviceStat.serverStatus = MapUtil.getString(obj, "serverStatus");
        deviceStat.serverUUID = MapUtil.getString(obj, "serverUUID");
        deviceStat.displayName = MapUtil.getString(obj, "displayName");
        deviceStat.conversationId = MapUtil.getString(obj, "conversationId");
        deviceStat.pbr = MapUtil.getBool(obj, "pbr");
        deviceStat.dbr = MapUtil.getBool(obj, "dbr");
        deviceStat.ubr = MapUtil.getBool(obj, "ubr");
        deviceStat.cbr = MapUtil.getBool(obj, "cbr");
        deviceStat.apm = MapUtil.getBool(obj, "apm");

        List screensList = MapUtil.getList(obj, "screens");
        if (screensList != null){
            for (Object o: screensList){
                if (o instanceof Map){
                    Map scr = (Map) o;
                    int width = MapUtil.getInt(scr, "width", -1);
                    int height = MapUtil.getInt(scr, "height", -1);
                    deviceStat.screens.add(new Screen(width, height));
                }
            }
        }

        Map os = MapUtil.getMap(obj, "os");
        if (os != null){
            String name = MapUtil.getString(os, "name");
            String version = MapUtil.getString(os, "version");
            String arch = MapUtil.getString(os, "arch");
            deviceStat.os = new SYSUtils.OS(name, version, arch);
        }

        return deviceStat;
    }


    public boolean isPbr() {
        return pbr;
    }

    public DeviceStat setPbr(boolean pbr) {
        this.pbr = pbr;
        return this;
    }

    public boolean isDbr() {
        return dbr;
    }

    public DeviceStat setDbr(boolean dbr) {
        this.dbr = dbr;
        return this;
    }

    public boolean isUbr() {
        return ubr;
    }

    public DeviceStat setUbr(boolean ubr) {
        this.ubr = ubr;
        return this;
    }

    public boolean isCbr() {
        return cbr;
    }

    public DeviceStat setCbr(boolean cbr) {
        this.cbr = cbr;
        return this;
    }

    public boolean isApm() {
        return apm;
    }

    public DeviceStat setApm(boolean apm) {
        this.apm = apm;
        return this;
    }

    public String getConversationId() {
        return conversationId;
    }

    public DeviceStat setServerUUID(String serverUUID) {
        this.serverUUID = serverUUID;
        return this;
    }

    public String getServerUUID() {
        return serverUUID;
    }


    public String getServerStatus() {
        return serverStatus;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : String.valueOf(deviceName);
    }

    public DeviceStat setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public List<Screen> getScreens() {
        return screens;
    }

    public DeviceStat(){
        this(true);
    }

    private DeviceStat(boolean scan){
        conversationId = Message.sanitize(SYSUtils.getDeviceId());
        if (!scan){
            return;
        }
        this.os = SYSUtils.getOS();
        deviceName = SYSUtils.getDeviceName();


        Object graphicsEnvironment =
                ReflectUtil.getStaticMethod("java.awt.GraphicsEnvironment", "getLocalGraphicsEnvironment").call();

        if (graphicsEnvironment != null){
            try {
                Object [] devices = (Object[]) ReflectUtil.getMethod(graphicsEnvironment, "getScreenDevices").call();
                for (Object device: devices){
                    Object displayMode = ReflectUtil.getMethod(device, "getDisplayMode").call();
                    if (displayMode != null){
                        Integer width = ReflectUtil.getMethod(displayMode, "getWidth").callForInteger();
                        Integer height = ReflectUtil.getMethod(displayMode, "getHeight").callForInteger();
                        screens.add(new Screen(width, height));
                    }
                }
            } catch (Exception e){

            }


        }
    }



    public SYSUtils.OS getOs() {
        return os;
    }

    public DeviceStat scanIPV4(){
        if (ipv4Addrs != null){
            return this;
        }
        else {
            ipv4Addrs = new HashSet<>();
            NetworkUtil.scanIPV4(new NetworkUtil.IPIterator() {
                @Override
                public void onFound(String ip) {
                    ipv4Addrs.add(ip);
                }
            });
        }
        return this;

    }




    public String getDeviceName() {
        return deviceName != null ? deviceName : "UNSET";
    }


    public int getBatteryScale() {
        return batteryScale;
    }

    public DeviceStat setBatteryScale(int batteryScale) {
        this.batteryScale = batteryScale;
        return this;
    }

    public int getBatteryPercentage(){
        return (int) ((getBatteryLevel()/ (float) getBatteryScale()) * 100);
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public DeviceStat setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
        return this;
    }

    public Set<String> getIpv4Addrs() {
        return ipv4Addrs;
    }

    public void setServerStatus(String status) {
        this.serverStatus = status;
    }


    public static class Screen {
        private final Integer width;
        private final Integer height;


        Screen(Integer width, Integer height) {
            this.width = width;
            this.height = height;
        }

        public Integer getWidth() {
            return width;
        }

        public Integer getHeight() {
            return height;
        }

        @Override
        public String toString() {
            return "{" +
                    "\"width\":\"" + width +
                    "\",\"height\":\"" + height +
                    "\"}";
        }
    }


    public String toJson() {
        return "{" +
                "\"batteryScale\":" + batteryScale +
                ",\"batteryLevel\":" + batteryLevel +
                ",\"deviceName\":" + jsonVal(deviceName) +
                ",\"os\":" + os +
                ",\"screens\":" + jsonVal(screens) +
                ",\"serverStatus\":" + jsonVal(serverStatus) +
                ",\"displayName\":" + jsonVal(displayName) +
                ",\"serverUUID\":" + jsonVal(serverUUID) +
                ",\"conversationId\":" + jsonVal(conversationId) +
                ",\"pbr\":" + jsonVal(pbr) +
                ",\"dbr\":" + jsonVal(dbr) +
                ",\"ubr\":" + jsonVal(ubr) +
                ",\"cbr\":" + jsonVal(cbr) +
                ",\"apm\":" + jsonVal(apm) +
                '}';
    }
}

package com.arise.weland.dto;

import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.NetworkUtil;
import com.arise.core.tools.ReflectUtil;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.arise.core.tools.CollectionUtil.*;
import static com.arise.core.tools.StringUtil.jsonVal;
import static com.arise.weland.dto.ContentInfo.addVal;
import static com.arise.weland.dto.ContentInfo.decodeString;

public class DeviceStat {

    protected Set<String> ipv4Addrs = new HashSet<>();
    private int batteryScale;
    private int batteryLevel;
    private String deviceName;
    private SYSUtils.OS os;
    private List<Screen> screens = new ArrayList<>();
    private String serverStatus;
    private String displayName;
    private String serverUUID;
    private String conversationId;



    public String toJson() {
        StringBuilder sb = new StringBuilder().append("{");
        addVal(sb, "B1", batteryScale);
        addVal(sb, "B2", batteryLevel);
        addVal(sb, "P", serverStatus);
        addVal(sb, "D", displayName);
        addVal(sb, "U", serverUUID);
        addVal(sb, "C", conversationId);
        if (!isEmpty(screens)){
            sb.append("\"S\":").append(jsonVal(screens)).append(",");
        }

        if (!isEmpty(ipv4Addrs)){
            sb.append("\"I4\":").append(jsonVal(ipv4Addrs)).append(",");
        }
        addVal(sb, "x", os.getName());
        addVal(sb, "v", os.getVersion());
        addVal(sb, "a", os.getArch());

        sb.append("\"N\":").append(jsonVal(ContentInfo.encodePath(deviceName)));
        sb.append("}");
        return sb.toString();
    }

    public static DeviceStat fromMap(Map obj) {
        DeviceStat deviceStat = new DeviceStat(false);
        deviceStat.batteryScale = MapUtil.getInt(obj,"B1", 0);
        deviceStat.batteryLevel = MapUtil.getInt(obj, "B2", 0);
        deviceStat.deviceName =  decodeString(obj, "N");
        deviceStat.serverStatus =  decodeString(obj, "P");
        deviceStat.displayName = decodeString(obj, "D");
        deviceStat.serverUUID = decodeString(obj, "U");
        deviceStat.conversationId = decodeString(obj, "C");


        List ipv4Addrs = MapUtil.getList(obj, "I4");
        if (!isEmpty(ipv4Addrs)){
            for (Object s: ipv4Addrs){
                if (s != null) {
                    deviceStat.ipv4Addrs.add(String.valueOf(s));
                }
            }
        }


        List screensList = MapUtil.getList(obj, "S");
        if (screensList != null){
            for (Object o: screensList){
                if (o instanceof Map){
                    Map scr = (Map) o;
                    int width = MapUtil.getInt(scr, "W", -1);
                    int height = MapUtil.getInt(scr, "H", -1);
                    deviceStat.screens.add(new Screen(width, height));
                }
            }
        }

        String name = decodeString(obj, "x");
        String version = decodeString(obj, "v");
        String arch = decodeString(obj, "a");
        deviceStat.os = new SYSUtils.OS(name, version, arch);

        return deviceStat;
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
                    "\"W\":\"" + width +
                    "\",\"H\":\"" + height +
                    "\"}";
        }
    }



}

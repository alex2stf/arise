package com.arise.weland.dto;

import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.NetworkUtil;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.StringUtil.jsonVal;
import static com.arise.weland.dto.ContentInfo.addVal;

public class DeviceStat {

    protected Set<String> ipv4Addrs = new HashSet<>();
    private int batteryScale;
    private int batteryLevel;
    private String deviceName;
    private SYSUtils.OS os;
    private String serverStatus;
    private String displayName;
    private String serverUUID;


    private Map<String, Object> pps = new ConcurrentHashMap<>();


    public static final DeviceStat INSTANCE = new DeviceStat().scanIPV4();


    public static DeviceStat getInstance() {
        return INSTANCE;
    }


    private DeviceStat(){
        this.os = SYSUtils.getOS();
        deviceName = SYSUtils.getDeviceName().toUpperCase();
    }



    private String t2j(Tuple2 t){
        return "{\"k\":"+jsonVal(t.first())+", \"v\":"+jsonVal(t.second())+"}";
    }

    private String t2jl(List<Tuple2> l){
        return "[" + StringUtil.join(l, ",", new StringUtil.JoinIterator<Tuple2>() {
            @Override
            public String toString(Tuple2 v) {
                return t2j(v);
            }
        }) + "]";
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder().append("{");
        addVal(sb, "B1", batteryScale);
        addVal(sb, "B2", batteryLevel);
        addVal(sb, "P", serverStatus);
        addVal(sb, "D", displayName);
        addVal(sb, "U", serverUUID);

        if (!isEmpty(ipv4Addrs)){
            sb.append("\"I4\":").append(jsonVal(ipv4Addrs)).append(",");
        }

        if (!isEmpty(pps)){
            sb.append("\"pP\": {");
            int cxx = 0;
            for (Map.Entry<String, Object> e: pps.entrySet()){
                if (cxx > 0){
                    sb.append(",");
                }
                if (e.getValue() instanceof String) {
                    sb.append(jsonVal(e.getKey())).append(":").append(jsonVal(e.getValue()));
                }
                else if(e.getValue() instanceof Tuple2){
                    sb.append(jsonVal(e.getKey())).append(":").append(
                            t2j((Tuple2) e.getValue())
                    );
                }
                else if (e.getValue() instanceof List){
                    sb.append(jsonVal(e.getKey())).append(":").append(
                            t2jl((List<Tuple2>) e.getValue())
                    );
                }
                cxx++;
            }
            sb.append("},");
        }

        addVal(sb, "x", os.getName());
        addVal(sb, "v", os.getVersion());
        addVal(sb, "a", os.getArch());

        sb.append("\"N\":").append(jsonVal(ContentInfo.encodePath(deviceName)));

        Calendar calendar = Calendar.getInstance();
        sb.append(",\"tz\":\"")
                .append(calendar.get(Calendar.YEAR))
                .append("-")
                .append(calendar.get(Calendar.MONTH))
                .append("-")
                .append(calendar.get(Calendar.DAY_OF_MONTH))
                .append("-")
                .append(calendar.get(Calendar.HOUR_OF_DAY))
                .append("-")
                .append(calendar.get(Calendar.MINUTE))
                .append("-")
                .append(calendar.get(Calendar.SECOND))
                .append("-")
                .append(calendar.get(Calendar.MILLISECOND))
        .append("\"");

        sb.append("}");
        return sb.toString();
    }



    public String getServerUUID() {
        return serverUUID;
    }

    public DeviceStat setServerUUID(String serverUUID) {
        this.serverUUID = serverUUID;
        return this;
    }

    public String getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(String status) {
        this.serverStatus = status;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : String.valueOf(deviceName);
    }

    public DeviceStat setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }



    public SYSUtils.OS getOs() {
        return os;
    }

    public DeviceStat scanIPV4(){
        ipv4Addrs = new HashSet<>();
        NetworkUtil.scanIPV4(new NetworkUtil.IPIterator() {
            @Override
            public void onFound(String ip) {
                ipv4Addrs.add(ip);
            }
        });
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

    public DeviceStat setProp(String key, String val) {
        pps.put(key, val);
        return this;
    }

    public DeviceStat setProp(String key, Tuple2<String, String> val) {
        pps.put(key, val);
        return this;
    }



    public DeviceStat setProp(String key, List<Tuple2<String, String>> val) {
        pps.put(key, val);
        return this;
    }

    @Deprecated
    public HttpResponse toHttp() {
        return HttpResponse.json(toJson()).allowAnyOrigin();
    }

    @Deprecated
    public HttpResponse toHttp(HttpRequest request) {
        return HttpResponse.json(toJson()).allowAnyOrigin();
    }

    public boolean hasProp(String k) {
        return pps != null && pps.containsKey(k);
    }

    public void setSysProp(String key, String name, String description, Object value) {
        setProp("sys["+key+"].v", value + "");
        setProp("sys["+key+"].n", name);
        setProp("sys["+key+"].i", description);
    }


    @Deprecated
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

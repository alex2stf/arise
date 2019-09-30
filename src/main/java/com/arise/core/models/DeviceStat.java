package com.arise.core.models;

import com.arise.core.tools.NetworkUtil;
import com.arise.core.tools.SYSUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeviceStat {

    protected Set<String> ipv4Addrs;
    private int batteryScale;
    private int batteryLevel;
    private String alias;
    private SYSUtils.OS os;
    private Map<String, String> props = new HashMap<>();

    public DeviceStat(){

    }

    public SYSUtils.OS getOs() {
        return os;
    }

    public void setOs(SYSUtils.OS os) {
        this.os = os;
    }

    public void scanIPV4(){
        scanIPV4(null);
    }

    public void scanIPV4(final NetworkUtil.IPIterator ipIterator){
        if (ipv4Addrs != null){
            for (String s: ipv4Addrs){
                ipIterator.onFound(s);
            }
            String[] x = new String[ipv4Addrs.size()];
            if (ipIterator != null) {
                ipIterator.onComplete(x);
            }
        }
        else {
            ipv4Addrs = new HashSet<>();
            NetworkUtil.scanIPV4(new NetworkUtil.IPIterator() {
                @Override
                public void onFound(String ip) {
                    ipv4Addrs.add(ip);
                    if (ipIterator != null) {
                        ipIterator.onFound(ip);
                    }
                }

                @Override
                public void onComplete(String[] ips) {
                    if (ipIterator != null) {
                        ipIterator.onComplete(ips);
                    }
                }
            });
        }
    }



    public String getAlias() {
        return alias != null ? alias : "UNSET";
    }

    public void setAlias(String alias) {
        this.alias = alias;
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

    public void setIpv4Addrs(Set<String> ipv4Addrs) {
        this.ipv4Addrs = ipv4Addrs;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public DeviceStat setProp(String key, String value){
        props.put(key, value);
        return this;
    }

    public void setProp(String key, boolean value){
        props.put(key, String.valueOf(value));
    }

    public boolean getBoolean(String key, boolean defaultVal){
        if (props.containsKey(key) && "true".equalsIgnoreCase(props.get(key))){
            return true;
        }
        return false;
    }

}

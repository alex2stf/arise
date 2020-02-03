package com.arise.weland.dto;

import java.io.Serializable;

public class RemoteConnection  {
    private final Object payload;
    private DeviceStat deviceStat;

    public RemoteConnection(Object payload, DeviceStat pingResponse) {
        this.payload = payload;
        this.deviceStat = pingResponse;
    }

    public Object getPayload() {
        return payload;
    }

    public DeviceStat getDeviceStat() {
        return deviceStat;
    }

    public void updatePingResponse(DeviceStat data) {
        deviceStat = data;
    }

    public String getName(){
        return deviceStat.getDisplayName();
    }
}

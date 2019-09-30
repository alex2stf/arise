package com.arise.astox.net.serviceHelpers.impl;

import com.arise.core.models.DeviceStat;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.SYSUtils;
import com.arise.astox.net.http.HttpRequest;
import com.arise.astox.net.http.HttpResponse;
import com.arise.astox.net.models.PayloadSerializer;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.serviceHelpers.RESTResponse;
import com.arise.astox.net.serviceHelpers.HTTPServerHandler;
import com.arise.core.tools.ContentType;

public class DeviceStatusBuilder extends HTTPServerHandler.ResponseBuilder {
    private final DeviceStat deviceStat = new DeviceStat();
    private StatusChangeRequestEvent event;

    private String lightStat = "";


    @Override
    public HttpResponse build(ServerRequest req) {
        HttpRequest request = (HttpRequest) req;
        deviceStat.setOs(SYSUtils.getOS());
        if ("GET".equals(request.method())) {
            return new RESTResponse(deviceStat, ContentType.APPLICATION_JSON, new PayloadSerializer<DeviceStat>() {
                @Override
                public byte[] serialize(DeviceStat obj) {
                  return Groot.toJson(obj).getBytes();
                }
            });
        }
        if ("POST".equals(request.method())){
            String newStat = request.getQueryParam("flashLightOn");
            if (newStat != null && !newStat.equals(lightStat)) {
                dispatchEvent(Feature.FLASHLIGHT, newStat);
                lightStat = newStat;
            }

        }
        return HttpResponse.oK();
    }

    private void dispatchEvent(Feature feature, String arg){
        if (event != null){
            try {
                event.onChangeRequest(feature, arg);
            }catch (Exception e){

            }

        }
    }

    public DeviceStat getDeviceStat() {
        return deviceStat;
    }

    public void onStatusChangeRequest(StatusChangeRequestEvent event) {
        this.event = event;
    }

    public enum Feature {
        FLASHLIGHT;
    }

    public interface StatusChangeRequestEvent {
        void onChangeRequest(Feature feature, String data);
    }
}

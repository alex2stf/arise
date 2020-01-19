package com.arise.corona;


import com.arise.astox.net.models.AbstractClient;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.http.HttpResponseBuilder;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.core.tools.MapObj;
import com.arise.core.tools.Mole;
import com.arise.corona.dto.ContentInfo;
import com.arise.corona.dto.DeviceStat;
import com.arise.corona.dto.Message;

public class Client  {

    private static final Mole log = Mole.getInstance(Client.class);


    protected static AbstractClient currentClient;
    static HttpResponseBuilder builder = new HttpResponseBuilder();

    public Client(){

    }

    public Client setClient(AbstractClient c){
        currentClient = c;
        return this;
    }





    public void ping(CompleteHandler<DeviceStat> onSuccess, CompleteHandler onError) {
        HttpRequest request = new HttpRequest().setMethod("GET").setUri("/health");

        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse response) {
                decodePing(response, onSuccess, onError);
            }
        });
    }

    private void decodePing(HttpResponse response, CompleteHandler<DeviceStat> onSuccess, CompleteHandler onError){
        MapObj obj = null;
        try {
            if (response.bodyBytes() == null){
                onError.onComplete(null);
                return;
            }
            obj = (MapObj) Groot.decodeBytes(response.bodyBytes(), 0, response.bodyBytes().length, Groot.Syntax.STANDARD);
        }catch (Exception e){
            onError.onComplete(e);
            return;
        }
        if (obj != null){
            DeviceStat deviceStat = DeviceStat.fromMap(obj);
            onSuccess.onComplete(deviceStat);
        }
        else {
            onError.onComplete(response);
        }
    }

    public void listFiles(String s, CompleteHandler<ContentInfo> onSuccess, CompleteHandler onError) {
        if (currentClient == null){
            log.warn("No client defined");
            onError.onComplete(null);
            return;
        }
        HttpRequest request = new HttpRequest()
                .setMethod("GET").setUri("/list/dir?path=" + s);
        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse response) {
                MapObj obj = (MapObj) Groot.decodeBytes(response.bodyBytes(), 0, response.getContentLength(), Groot.Syntax.STANDARD);
                if (obj != null){
                    ContentInfo dto = ContentInfo.fromMap(obj);
                    onSuccess.onComplete(dto);
                }
                else {
                    onError.onComplete(response);
                }
            }
        });
    }

    public void openFile(String s) {
        HttpRequest request = new HttpRequest()
                .setMethod("GET").setUri("/files/open?path=" + s);
        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse data) {
                log.info("open file response: " + data);
            }
        });
    }

    public void openInBrowser(String s) {
        if (currentClient == null){
            log.info("currentClient not defined");
            return;
        }
        HttpRequest request = new HttpRequest()
                .setMethod("GET").setUri("/commands/exec/browserOpen?url=" + s + "&closeExisting=true");
        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse data) {
                log.info("open url response: " + data);
            }
        });
    }


    public void updateMousePosition(int mouseX, int mouseY){
        if (currentClient == null){
            log.info("currentClient not defined");
            return;
        }

        HttpRequest request = new HttpRequest()
                .setMethod("GET").setUri("/device/controls/set?mouseX=" + mouseX + "&mouseY=" + mouseY);
        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse data) {
                log.info("updateMousePosition response: " + data);
            }
        });
    }


    public void getDeviceStat(CompleteHandler<DeviceStat> deviceStatCompleteHandler, CompleteHandler onError){
        if (currentClient == null){
            log.info("currentClient not defined");
            return;
        }

        HttpRequest request = new HttpRequest()
                .setMethod("GET").setUri("/device/stat");
        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse response) {
                log.info("getDeviceStat response: " + response);
                MapObj obj = (MapObj) Groot.decodeBytes(response.bodyBytes(), 0, response.getContentLength(), Groot.Syntax.STANDARD);
                DeviceStat deviceStat = DeviceStat.fromMap(obj);
                deviceStatCompleteHandler.onComplete(deviceStat);
            }
        });
    }


    public void sendMessage(Message message, CompleteHandler<DeviceStat> onSuccess, CompleteHandler onError) {
        HttpRequest request = new HttpRequest()
                .setMethod("POST").setUri("/message")
                .setBytes(message.toJson().getBytes());
        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse data) {
                decodePing(data, onSuccess, onError);
            }
        });
    }
}

package com.arise.weland;


import com.arise.astox.net.models.AbstractClient;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.MapObj;
import com.arise.core.tools.Mole;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.dto.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class WelandAPI {

    private static final Mole log = Mole.getInstance(WelandAPI.class);


    protected static AbstractClient currentClient;

    public WelandAPI(){

    }

    public WelandAPI setClient(AbstractClient c){
        currentClient = c;
        return this;
    }



    public String clientId(){
        return currentClient.getId();
    }



    public void ping(final CompleteHandler<DeviceStat> onSuccess, final CompleteHandler onError) {
        HttpRequest request = new HttpRequest().setMethod("GET").setUri("/health");
        currentClient.setErrorHandler(onError);
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

    public void listFiles(String s, final CompleteHandler<ContentInfo> onSuccess, final CompleteHandler onError) {
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
                .setMethod("GET").setUri("/files/open?path=" + URLEncoder.encode(s));
        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse data) {
                log.info("open file response: " + data);
            }
        });
    }

    public void playNative(String s) {
        HttpRequest request = new HttpRequest()
                .setMethod("GET").setUri("/files/play/native?path=" + URLEncoder.encode(s));
        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse data) {
                log.info("open file response: " + data);
            }
        });
    }


    public void playEmbedded(String s) {
        HttpRequest request = new HttpRequest()
                .setMethod("GET").setUri("/files/play/embedded?path=" + URLEncoder.encode(s));
        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse data) {
                log.info("open file response: " + data);
            }
        });
    }



    public void close(String s) {
        HttpRequest request = new HttpRequest()
                .setMethod("GET").setUri("/files/close?path=" + URLEncoder.encode(s));
        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse data) {
                log.info("close file response: " + data);
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


    public void getDeviceStat(final CompleteHandler<DeviceStat> deviceStatCompleteHandler, CompleteHandler onError){
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


    public void sendMessage(Message message, final CompleteHandler<DeviceStat> onSuccess, final CompleteHandler onError) {
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
    public void mediaList(String playlistId, Integer index, final CompleteHandler<ContentPage> completeHandler, final CompleteHandler onError) {
        HttpRequest request = new HttpRequest()
                .setMethod("GET").setUri("/media/list/" + playlistId + "?index=" + index);
        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse data) {
              if (data.bodyBytes() == null){
                onError.onComplete("RETRY");
                return;
              }
              try {
                Map obj = (Map) Groot.decodeBytes(data.bodyBytes());
                ContentPage contentPage = ContentPage.fromMap(obj);
                completeHandler.onComplete(contentPage);
              } catch (Exception ex){
                log.error("DECODE FAILED FOR " + data);
                onError.onComplete(ex);
              }
            }
        });
    }


    public void findThumbnail(String id, final CompleteHandler<byte[]> success, CompleteHandler onError){
        try {
            id = URLEncoder.encode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {

        }
        HttpRequest request = new HttpRequest()
                .setMethod("GET").setUri("/thumbnail?id=" + id);
        currentClient.sendAndReceive(request, new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse data) {
                success.onComplete(data.bodyBytes());
            }
        });
    }



    public void shuffle(String playlistId, CompleteHandler onComplete) {
        HttpRequest request = new HttpRequest()
                .setMethod("GET").setUri("/media/shuffle/" + playlistId);
        currentClient.sendAndReceive(request, onComplete);
    }



//    public void moveMouse(int x, int y) {
//        System.out.println("mouse move " + x + " " + y);
//        HttpRequest request = new HttpRequest()
//                .setMethod("GET").setUri("/ctrl?mx=" + x + "&my=" + y);
//        currentClient.sendAndReceive(request, new CompleteHandler() {
//            @Override
//            public void onComplete(Object data) {
//                System.out.println(data);
//            }
//        });
//    }



}

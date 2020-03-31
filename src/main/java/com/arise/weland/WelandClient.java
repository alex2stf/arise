package com.arise.weland;

//import android.bluetooth.BluetoothDevice;


import com.arise.astox.net.clients.JHttpClient;
import com.arise.astox.net.models.AbstractClient;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.core.tools.ReflectUtil;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.dto.ContentPage;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.Message;
import com.arise.weland.dto.RemoteConnection;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WelandClient {
    static String bluetoothUUID = "fa87c0d0-afac-11de-8a39-0800200c9a66";



//    static WelandAPI client = new WelandAPI();

    static Map<String, AbstractClient> workersCache = new ConcurrentHashMap<>();
    static Map<String, WelandAPI> clientsCache = new ConcurrentHashMap<>();

    public static String getWorkerId(Object worker){
        if (worker instanceof Serializable){
            return worker.toString();
        }
        String res = "";
        String name = ReflectUtil.getMethod(worker, "getName").callForString();
        if (StringUtil.hasText(name)){
            res += name;
        }
        String address = ReflectUtil.getMethod(worker, "getAddress").callForString();
        if (StringUtil.hasText(address)){
            res += address;
        }
        return res;
    }

    static CompleteHandler<Throwable> ERROR_HANDLER = new CompleteHandler<Throwable>() {
        @Override
        public void onComplete(Throwable data) {
            data.printStackTrace();
        }
    };

    public static synchronized AbstractClient getWorker(Object worker){
        if (worker instanceof RemoteConnection){
            return getWorker(((RemoteConnection) worker).getPayload());
        }
        String id = getWorkerId(worker);
        if (workersCache.containsKey(id) && workersCache.get(id) != null){
            return workersCache.get(id);
        }
        AbstractClient abstractClient = null;
        if (ReflectUtil.isInstanceOf(worker, "android.bluetooth.BluetoothDevice")){
            abstractClient = (AbstractClient) ReflectUtil.newInstance("com.arise.rapdroid.BluetoothHttpClient", worker);
            abstractClient.setUuid(bluetoothUUID);
            ReflectUtil.getMethod(abstractClient, "setErrorHandler").call(ERROR_HANDLER);
            workersCache.put(id, abstractClient);
        }
        else if (worker instanceof URI){
            abstractClient = new JHttpClient().setUri((URI) worker).setErrorHandler(ERROR_HANDLER);
            workersCache.put(id, abstractClient);
        }
        else {
            throw new RuntimeException("Cannot track worker " + worker);
        }
        return abstractClient;
    }

    public static synchronized WelandAPI getSynchronizedClient(Object worker){
        JHttpClient client = new JHttpClient(){
            @Override
            public void connect(HttpRequest request, CompleteHandler<HttpURLConnection> completionHandler) {
                connectSync(request, completionHandler);
            }
        };
        client.setUri((URI) worker);
        return new WelandAPI().setClient(client);
    }

    static synchronized WelandAPI getClient(Object worker){
        AbstractClient client = getWorker(worker);
        String id = client.getId();
        if (clientsCache.containsKey(id)){
            clientsCache.get(id).setClient(client);
            return clientsCache.get(id);
        }
        WelandAPI welandApi = new WelandAPI().setClient(client);
        clientsCache.put(id, welandApi);
        return welandApi;
    }

    public static void pingBluetooth(Object device, CompleteHandler<DeviceStat> handler, CompleteHandler<Throwable> errorHandler) {
        getClient(device).ping(handler, errorHandler);
    }

    public static void pingHttp(URI uri, CompleteHandler<DeviceStat> handler, CompleteHandler<Throwable> errorHandler){
        getClient(uri).ping(handler, errorHandler);
    }

    public static void pingHttp(String ip, int port, CompleteHandler<DeviceStat> handler, CompleteHandler<Throwable> errorHandler){
        try {
            URI uri = new URI("http://" + ip + "/" + port);
            getClient(uri).ping(handler, errorHandler);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }





    public static void mediaList(Object worker, String playlistId, Integer index, CompleteHandler<ContentPage> completeHandler, CompleteHandler onError){
        getClient(worker).setErrorHandler(onError).mediaList(playlistId, index, completeHandler, onError);
    }



    public static void openInRemoteBrowser(Object worker, String url) {
        getClient(worker).openInBrowser(url);
    }



    public static void sendMessage(RemoteConnection remoteConnection,  final Message message,
                                   final CompleteHandler<MessageResponse> onSucces, CompleteHandler onError) {
        final MessageResponse messageResponse = new MessageResponse();
        messageResponse.message = message;

        getClient(remoteConnection.getPayload()).sendMessage(message, new CompleteHandler<DeviceStat>() {
            @Override
            public void onComplete(DeviceStat data) {
                messageResponse.deviceStat = data;
                onSucces.onComplete(messageResponse);
            }
        }, onError);
    }
    public static void sendTextMessage(RemoteConnection remoteConnection, String text, CompleteHandler<MessageResponse> onSucces, CompleteHandler onError) {
        Object worker = remoteConnection.getPayload();
        WelandAPI welandApi = getClient(worker);
        String receiverId = welandApi.clientId();
        String senderId = SYSUtils.getDeviceId();
        String messageId;

        String conversationId = remoteConnection.getDeviceStat().getConversationId();

        messageId = senderId + receiverId + conversationId + new Date();

        Message message = new Message();
        message.setId(messageId)
                .setConversationId(conversationId)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .setText(text);
        sendMessage(remoteConnection, message, onSucces, onError);
    }

//    public static void openFile(ContentInfo info, Object worker, CompleteHandler onSuccess) {
//        getClient(worker).openFile(info.getPath(), onSuccess);
//    }

    public static void openFile(String path, Object worker, CompleteHandler onSuccess) {
        getClient(worker).openFile(path, onSuccess);
    }

    public static void findThumbnail( Object worker, String thumbnailId, CompleteHandler<byte[]> completeHandler) {
        getClient(worker).findThumbnail(thumbnailId, completeHandler, new CompleteHandler() {
            @Override
            public void onComplete(Object data) {
                System.out.println("error");
            }
        });
    }

    public static void stop(ContentInfo info, Object worker) {
        getClient(worker).close(info.getPath());
    }

    public static void playNative(String s, Object worker, CompleteHandler onSuccess) {
        getClient(worker).playNative(s, onSuccess);
    }

    public static void playEmbedded(String s, Object worker, CompleteHandler onSuccess) {
        getClient(worker).playEmbedded(s, onSuccess);
    }

    public static void addToQueue(ContentInfo info, String mode, Object worker) {
        getClient(worker).addToQueue(info, mode);
    }


    public static class MessageResponse {
       public Message message;
       public DeviceStat deviceStat;
    }
}

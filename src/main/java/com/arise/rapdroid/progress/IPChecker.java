package com.arise.rapdroid.progress;

import com.arise.core.tools.Mole;
import com.arise.core.tools.Util;
import com.arise.weland.dto.DeviceStat;


public class IPChecker {
    private static final Mole log = Mole.getInstance(IPChecker.class);

    public static void checkMyNet(final String address, final int port, final Handler handler){
        Util.ThreadFactory.asyncTask(new Runnable() {
            @Override
            public void run() {
//                JHttpClient httpClient = new JHttpClient();
//                String url = "http://" + address + ":"+port+"/device-stat";
//                httpClient.setUrl(url);
//                httpClient.setMethod("GET");
//                httpClient.execute(null, new JHttpClient.RequestHandler() {
//                    @Override
//                    public void handle(int responseCode, Map<String, List<String>> headers, InputStream stream, InputStream errorStream) {
//                        String response = StreamUtil.toString(stream);
//                        try {
//                            DeviceStat jsonObject = CoreSerializer.fromJson(response);
//                            handler.onResponse(address, jsonObject);
//                        }catch (Exception e){
//                            log.error(e);
//                        }
//                    }
//
//                    @Override
//                    public void err(Throwable error) {
////                        log.info("skip " + address + ":" + port + " because " + (error != null ? error.getMessage() : String.valueOf(error)));
//                    }
//                });
            }
        });
    }

    public static void checkRandomHttp(final String address, final int port, final RandHandler randHandler){
        Util.ThreadFactory.asyncTask(new Runnable() {
            @Override
            public void run() {
//                JHttpClient httpClient = new JHttpClient();
//                String url = "http://" + address + ":"+port+"/";
//                httpClient.setUrl(url);
//                httpClient.setMethod("GET");
//                httpClient.execute(null, new JHttpClient.RequestHandler() {
//                    @Override
//                    public void handle(int responseCode, Map<String, List<String>> headers, InputStream stream, InputStream errorStream) {
//                        log.info(responseCode);
//                        if (200 == responseCode){
//                            randHandler.onResponse(address, responseCode, StreamUtil.toString(stream));
//                        }
//                    }
//
//                    @Override
//                    public void err(Throwable error) {
//                        log.info("skip " + address + ":" + port + " because " + (error != null ? error.getMessage() : String.valueOf(error)));
//                    }
//                });
            }
        });
    }

    public interface Handler {
        void onResponse(String ip, DeviceStat jsonObject);
    }

    public interface RandHandler{
        void onResponse(String ip, int statusCode, String content);
    }
}

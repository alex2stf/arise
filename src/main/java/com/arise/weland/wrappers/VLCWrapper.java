package com.arise.weland.wrappers;

import com.arise.astox.net.clients.JHttpClient;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.models.Handler;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.Base64;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.NetworkUtil;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.dto.DeviceStat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Deprecated
public class VLCWrapper {

    private static final String VLC_HTTP_PASSWORD = "arise";
    private static final String VLC_HTTP_PORT = "9090";
    public static String VLC_HTTP_HOST = NetworkUtil.getCurrentIPV4AddressSync();




    private static final Mole log = Mole.getInstance(VLCWrapper.class);

    public static File open(String[] args) {
        String executablePath = args[0];
        final File executable = new File(executablePath);
        if (!executable.exists()){
            return null;
        }
        boolean seemsVlc = executable.getName().equalsIgnoreCase("vlc")
                || executable.getName().equalsIgnoreCase("vlc.exe")
                || executable.getName().equalsIgnoreCase("VLCPortable.exe")
                ;
        if (!seemsVlc){
            return null;
        }

        final File source = new File(args[1]);


        Status vlcStatus = getStatus();
        if (vlcStatus.valid){
            vlcStatus = clearPlaylist();
            if (vlcStatus.valid) {

                String path = source.toURI().getPath();
                path = path.replaceAll("\\s+", "%20");
                vlcStatus = httpPlay(path);
                if (vlcStatus.valid){
                    getStatusRequest(MapUtil.newMap("command", "fullscreen").get());
                    return executable;
                }
            }
        }

//        String vlcHost = NetworkUtil.getCurrentIPV4AddressSync();
        DeviceStat.getInstance().setProp("vlc-host", VLC_HTTP_HOST + ":" + VLC_HTTP_PORT + "/mobile.html");
        ThreadUtil.startDaemon(new Runnable() {
            @Override
            public void run() {
                startVlcInstance(executable, source);
            }
        }, "VLCWrapper#startInstance" + UUID.randomUUID().toString());
        return executable;
    }

    static void startVlcInstance(File executable, File source){
        String actualArgs[] = new String[]{
                executable.getAbsolutePath()
                , source.getAbsolutePath()
                , "--fullscreen"
                , "-I"
                , "http"
                , "--http-host=" + VLC_HTTP_HOST
                , "--http-port=" + VLC_HTTP_PORT
                , "--http-password=" + VLC_HTTP_PASSWORD
        };

        System.out.println("VLC instance init " + (StringUtil.join(actualArgs, " ")));


        SYSUtils.exec(actualArgs);
    }



    static Status clearPlaylist(){
        Map<String, String> map = new HashMap<>();
        map.put("command", "pl_empty");
        return getStatusRequest(map);
    }

    static Status httpPlay(String path){
        Map<String, String> map = new HashMap<>();
        map.put("command", "in_play");
        map.put("input", "file://" + path);
        return getStatusRequest(map);
    }

    static Status getStatus(){
        return getStatusRequest(Collections.<String, String>emptyMap());
    }


    static Status getStatusRequest(Map<String, String> args){
        JHttpClient jHttpClient = new JHttpClient();
        jHttpClient.setPort(Integer.valueOf(VLC_HTTP_PORT));
        jHttpClient.setHost(VLC_HTTP_HOST);

        String auth = ":" + VLC_HTTP_PASSWORD;
        HttpRequest request = new HttpRequest()
                .setUri("/requests/status.json")
                .setMethod("GET")
                .addHeader("Authorization", "Basic " + Base64.encodeBytes(auth.getBytes(StandardCharsets.UTF_8)));

        for (Map.Entry<String, String> entry: args.entrySet()) {
            request.addQueryParam(entry.getKey(), entry.getValue());
        }


        System.out.println("sending 0" + request.toString());



        final Status status = new Status();

        jHttpClient.sendAndReceiveSync(request, new Handler<HttpResponse>() {
            @Override
            public void handle(HttpResponse data) {

                if (data.status() == 200){
                    status.valid = true;
                }

                Map response = (Map) Groot.decodeBytes(
                        data.bodyBytes()
//                        new String(data.bodyBytes()).replaceAll("\\s+", " ").getBytes()
                );
                status.state = MapUtil.getString(response, "state");
                status.time = MapUtil.getString(response, "time");
                status.volume = MapUtil.getString(response, "volume");
                status.length = MapUtil.getString(response, "length");
            }
        });

        return status;
    }

    public static boolean isHttpOpened() {
        return getStatus() != null;
    }

    public static Status pauseHttp() {
        return getStatusRequest(
                MapUtil.newMap("command", "pl_pause").get()
        );
    }

    public static Status stopHttp() {
        clearPlaylist();
        Map<String, String> map = new HashMap<>();
        map.put("command", "in_play");
        map.put("input", "vlc://quit");
        return getStatusRequest(map);
    }


    public static class Status {
        boolean valid;
        String length;
        String state;
        String time;
        String volume;

        @Override
        public String toString() {
            return "Status{" +
                    "valid=" + valid +
                    ", length='" + length + '\'' +
                    ", state='" + state + '\'' +
                    ", time='" + time + '\'' +
                    ", volume='" + volume + '\'' +
                    '}';
        }
    }
}

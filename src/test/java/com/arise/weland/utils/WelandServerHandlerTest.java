package com.arise.weland.utils;


import com.arise.astox.net.models.http.HttpResponse;
import com.arise.cargo.management.DependencyManager;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.Message;
import com.arise.weland.impl.RadioPlayer;
import com.arise.weland.model.ContentHandler;
import com.arise.weland.model.MediaPlayer;

import java.util.List;
import java.util.Map;

public class WelandServerHandlerTest {

    public void test(){

    }

    public static void main(String[] args) {
        WelandServerHandler welandServerHandler = new WelandServerHandler();

        welandServerHandler.setContentHandler(new ContentHandler() {
            @Override
            public HttpResponse openPath(String path) {
                return HttpResponse.plainText("PLAYED_" + path);
            }

            @Override
            protected HttpResponse pause(String path) {
                return HttpResponse.plainText("PAUSED_" + path);
            }

            @Override
            public HttpResponse stop(String x) {
                return HttpResponse.plainText("STOPPED_" + x);
            }

            @Override
            public void onMessageReceived(Message message) {

            }

            @Override
            public void onPlaylistPlay(String name) {

            }

            @Override
            public MediaPlayer mPlayer() {
                return null;
            }

            @Override
            public RadioPlayer rPlayer() {
                return null;
            }

            @Override
            public DeviceStat onDeviceUpdate(Map<String, List<String>> params) {
                return null;
            }

            @Override
            public DeviceStat getDeviceStat() {
                return DeviceStat.getInstance();
            }

            @Override
            public void onCloseRequested() {

            }

            @Override
            public void takeSnapshot(String x) {

            }
        });


    }
}
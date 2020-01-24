package com.arise.corona;

import com.arise.astox.net.clients.HttpClient;
import com.arise.astox.net.clients.JHttpClient;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.corona.dto.ContentInfo;
import com.arise.corona.dto.DeviceStat;
import com.arise.corona.dto.Message;
import org.junit.Test;

public class ClientTest {

    @Test
    public void testDeviceStat(){
        new DeviceStat();
    }


    public static void main(String[] args) {


        Message message = new Message().setType(Message.Type.TEXT);
        message.setId("--http://128.3.445:-8083/A/x\\x\\---");
//        System.out.println(message.toJson());

        DeviceStat deviceStat = new DeviceStat();
//        System.out.println(deviceStat);

        ContentInfo mediaInfo = new ContentInfo()
                .setAlbumName("albm");


        System.out.println(mediaInfo);
        System.exit(-1);
        HttpClient httpClient = new HttpClient().setHost("localhost").setPort(8221);
        JHttpClient jHttpClient = new JHttpClient().setHost("localhost").setPort(8221);

        Client client = new Client();
        client.setClient(httpClient);

        client.getDeviceStat(new CompleteHandler<DeviceStat>() {
            @Override
            public void onComplete(DeviceStat data) {
                System.out.println(data);
            }
        }, new CompleteHandler() {
            @Override
            public void onComplete(Object data) {
                System.out.println(data);
            }
        });

        client.ping(new CompleteHandler<DeviceStat>() {
            @Override
            public void onComplete(DeviceStat data) {
                System.out.println(data);
            }
        }, new CompleteHandler() {
            @Override
            public void onComplete(Object data) {
                System.out.println("ERROR " + data);
            }
        });

        try {
            Thread.sleep(20000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




    }
}

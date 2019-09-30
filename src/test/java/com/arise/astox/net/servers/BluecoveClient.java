package com.arise.astox.net.servers;

import com.arise.astox.net.http.HttpEntity;
import com.arise.astox.net.http.HttpEntity.Protocol;
import com.arise.astox.net.http.HttpRequest;
import com.arise.astox.net.http.HttpResponse;
import com.arise.astox.net.http.HttpResponseBuilder;
import com.arise.astox.net.models.AbstractClient;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.util.Map;

public class BluecoveClient extends AbstractClient<ServerRequest, ServerResponse, StreamConnection> {

    public static final String SECURED_UUID_STR = "fa87c0d0-afac-11de-8a39-0800200c9a66";
    private static final String SERVER_NAME = "BluetoothChatSecure";

    public BluecoveClient(){

    }

    @Override
    protected StreamConnection getConnection() throws Exception {
        return (StreamConnection) Connector.open("btspp://8C83E10BB219:5;authenticate=false;encrypt=false;master=false");
    }

    @Override
    protected OutputStream getOutputStream(StreamConnection streamConnection) {
        try {
            return streamConnection.openOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected InputStream getInputStream(StreamConnection streamConnection) {
        try {
            return streamConnection.openInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    // globals
    private HashMap<String,RemoteDevice> devicesMap = null;

    //btspp://8C83E10BB219:5;authenticate=false;encrypt=false;master=false

    // used to block execution until startInquiry() finishes


    public static void main(String[] args) throws IOException {
        AbstractClient bluecoveClient = new BluecoveClient().setBuilder(new HttpResponseBuilder());
        List<String> pathParams = new ArrayList<>();
        pathParams.add("ping");

        Map<String, String> headers = new HashMap<>();
        headers.put("test", "key");
        HttpRequest httpRequest = new HttpRequest("GET", pathParams, HttpEntity.emptyQueryParams(), headers, Protocol.HTTP_1_0.name());

        bluecoveClient.send(httpRequest, new CompletionHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse response) {
                System.out.println("Respone:\n" + response);
            }
        });


    }


























    public static void main2(String[] args) throws BluetoothStateException, InterruptedException {

        final Object inquiryCompletedEvent = new Object();

        LocalDevice local = LocalDevice.getLocalDevice();
        System.out.println("This device's name: " +
            local.getFriendlyName());
        DiscoveryAgent agent = local.getDiscoveryAgent();

        final Vector<RemoteDevice> discoveredDevices = new Vector<>();

        DiscoveryListener listener = new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
                String name;
                try {
                    name = remoteDevice.getFriendlyName(false);
//                    System.out.println("Discovered " + remoteDevice.getFriendlyName(false));
                } catch (IOException e) {
                    name = remoteDevice.getBluetoothAddress();
//                    System.out.println("Discovered " + remoteDevice.getBluetoothAddress());
                }
                if (name.indexOf("Galaxy") > -1){
                    System.out.println("ADD " + name);
                    discoveredDevices.add(remoteDevice);
                } else {
                    System.out.println("IGNORE " + name);
                }



            }

            @Override
            public void servicesDiscovered(int j, ServiceRecord[] serviceRecords) {
                for (int i = 0; i < serviceRecords.length; i++) {
                    System.out.println("Service " + i);
                    String url = serviceRecords[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    if (url == null) {
                        continue;
                    }
                    System.out.println("Connection url " + url);
//                    DataElement serviceName = services[i].getAttributeValue(0x0100);
//                    if (serviceName != null) {
//                        System.out.println("service " + serviceName.getValue() + " found " + url);
//                    } else {
//                        System.out.println("service found " + url);
//                    }
//
//                    if(serviceName.getValue().equals("OBEX Object Push")){
//                        sendMessageToDevice(url);
//                    }
                }
            }

            @Override
            public void serviceSearchCompleted(int i, int i1) {

            }

            @Override
            public void inquiryCompleted(int i) {
                synchronized (inquiryCompletedEvent){
                    inquiryCompletedEvent.notify();
                }
            }
        };


        synchronized (inquiryCompletedEvent){
            agent.startInquiry(DiscoveryAgent.GIAC, listener);

//            agent.searchServices(attrIDs, searchUuidSet, btDevice, listener);
            inquiryCompletedEvent.wait();
        }

        System.out.println("inquiry completed");

//        UUID HANDS_FREE = new UUID(0x111E);

        UUID uuid = new UUID(java.util.UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66").toString().replaceAll("-", ""), false);
        int URL_ATTRIBUTE = 0X0100;

        UUID[] searchUuidSet = new UUID[]{uuid};
        int[] attrIDs = new int[]{URL_ATTRIBUTE};

        for (RemoteDevice device: discoveredDevices){
            synchronized (inquiryCompletedEvent){
                try {
                    System.out.println("Search in " + device.getFriendlyName(false));
                } catch (IOException e) {
                    System.out.println("Search in " + device.getBluetoothAddress());
                }
                agent.searchServices(attrIDs, searchUuidSet, device, listener);
                inquiryCompletedEvent.wait();
            }

        }



//        RemoteDevice[] remoteDevices = agent.retrieveDevices(DiscoveryAgent.PREKNOWN);
//        System.out.println(remoteDevices);
//        findRemote(agent, DiscoveryAgent.CACHED);
//        findRemote(agent, DiscoveryAgent.PREKNOWN);
    }

    public static void findRemote(DiscoveryAgent agent, int option){
        System.out.println("Checking for previous devices...");
        RemoteDevice[] remoteDevices = agent.retrieveDevices(option);
        for (RemoteDevice device: remoteDevices){
            try {
                System.out.println(device.getFriendlyName(true));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}

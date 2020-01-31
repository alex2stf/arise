package com.arise.weland.impl;


import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.ReadCompleteHandler;
import com.arise.astox.net.models.ServerMessage;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.StreamedServer;
import com.arise.astox.net.models.http.HttpReader;
import com.arise.astox.net.models.http.HttpRequestReader;
import com.arise.astox.net.models.http.HttpResponseReader;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.tools.Util;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BluecoveServer extends StreamedServer<StreamConnectionNotifier, StreamConnection> {

//    private StreamConnection streamConnection;

    public BluecoveServer() {
        super(StreamConnection.class);
    }

    private static final String UUID_STR = "fa87c0d0-afac-11de-8a39-0800200c9a66";
    private static final UUID UUID_OBJ = new UUID(java.util.UUID.fromString(UUID_STR).toString().replaceAll("-", ""), false);

    @Override
    protected String getConnectionProtocol() {
        return "btspp";
    }

    @Override
    public String getConnectionPath() {
        return getConnectionProtocol() + "://" + (getHost() != null ? getHost() : "localhost") + ":" + getUuid() + ";name=" + getName();
    }



    @Override
    protected StreamConnectionNotifier buildConnectionProvider() {
        java.util.UUID MY_UUID_SECURE =
            java.util.UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
        System.out.println(MY_UUID_SECURE.getMostSignificantBits());
        // display local device address and name
        LocalDevice localDevice = null;
        try {
            localDevice = LocalDevice.getLocalDevice();
//            localDevice.setDiscoverable(DiscoveryAgent.GIAC);
        } catch (Exception e) {
            fireError(e);
        }
        System.out.println("Address: " + localDevice.getBluetoothAddress());
        System.out.println("Name: " + localDevice.getFriendlyName());

        // open server url
        try {
            StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector.open(getConnectionPath());
            return streamConnNotifier;
        } catch (IOException e) {
            fireError(e);
        }
        return null;
    }

//    StreamConnection currentStreamConnection;
    @Override
    protected StreamConnection acceptConnection(StreamConnectionNotifier streamConnectionNotifier) throws Exception {
        if (streamConnectionNotifier == null){
           throw new LogicalException("StreamConnectionNotifier is null");
        }
        return streamConnectionNotifier.acceptAndOpen();
    }


    @Override
    protected void handle(StreamConnection connection) {

       AbstractServer self = this;
        final boolean[] allow = {true};

        OutputStream outputStream;
        InputStream inputStream;
        try {
            outputStream = connection.openOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            inputStream = connection.openInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpRequestReader httpRequestReader = new HttpRequestReader() {
            @Override
            public void handleRest(HttpReader reader) {
                request.setBytes(bodyBytes.toByteArray());
                System.out.println("RECEIVED " + request);
                ServerResponse response = requestHandler.getResponse(self, request);
                try {
                    outputStream.write(response.bytes());
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Util.close(outputStream);
                    Util.close(inputStream);
                    Util.close(connection);
                    allow[0] = false;
                }
                flush();
            }

            @Override
            public void onError(IOException e) {
                allow[0] = false;
                e.printStackTrace();
                Util.close(outputStream);
                Util.close(inputStream);
                Util.close(connection);
            }
        };


        while (allow[0]){
            httpRequestReader.readInputStream(inputStream);
        }


    }




    @Override
    protected InputStream getInputStream(StreamConnection streamConnection) {
        try {
            return streamConnection.openInputStream();
        } catch (IOException e) {
            return null;
        }
    }


    @Override
    protected OutputStream getOutputStream(StreamConnection streamConnection) {
        try {
            return  streamConnection.openOutputStream();
        } catch (IOException e) {
            return null;
        }
    }


    @Override
    public void start() throws Exception {
        super.start();
    }

    @Override
    public void registerMessage(ServerMessage message) {

    }

    public void close(){

    }


    @Override
    public String getUuid() {
        return UUID_OBJ.toString();
    }
}

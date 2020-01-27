package com.arise.weland.impl;


import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.ReadCompleteHandler;
import com.arise.astox.net.models.ServerMessage;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.StreamedServer;
import com.arise.core.exceptions.LogicalException;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        InputStream localInputStream = null;
        try {
            localInputStream = connection.openInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        AbstractServer self = this;
        InputStream finalLocalInputStream = localInputStream;
        readPayload(localInputStream, new ReadCompleteHandler<ServerRequest>() {
            @Override
            public void onReadComplete(ServerRequest serverRequest) {
                System.out.println("RECEIVED " + serverRequest);
                OutputStream outputStream = null;
                try {
                    outputStream = connection.openOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ServerResponse response = requestHandler.getResponse(self, serverRequest);
                if (response == null){
                    response = requestHandler.getDefaultResponse(self);
                }
                try {
                    System.out.println("RESPONSE " + response);
                    outputStream.write(response.bytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("CLOSING CONNECTION");
//                    close();
                    try {
                        connection.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                finally {
                    try {
                        outputStream.close();
                        finalLocalInputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


//    OutputStream outputStream;
//    InputStream inputStream;

    @Override
    protected InputStream getInputStream(StreamConnection streamConnection) {
//        if (inputStream == null){
//            try {
//                inputStream = streamConnection.openInputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return inputStream;
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
//        if (outputStream == null){
//            try {
//                outputStream = streamConnection.openOutputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return outputStream;
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

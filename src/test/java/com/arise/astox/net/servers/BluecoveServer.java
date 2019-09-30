package com.arise.astox.net.servers;


import com.arise.astox.net.http.HttpRequestBuilder;
import com.arise.astox.net.http.HttpResponse;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.DuplexDraft.Connection;
import com.arise.astox.net.models.DuplexDraft.Frame;
import com.arise.astox.net.models.ServerMessage;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.StreamedServer;
import com.arise.core.exceptions.LogicalException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BluecoveServer extends StreamedServer<StreamConnectionNotifier, StreamConnection> {

    public BluecoveServer() {
        super(StreamConnection.class);
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
        } catch (BluetoothStateException e) {
            fireError(e);
        }
        System.out.println("Address: " + localDevice.getBluetoothAddress());
        System.out.println("Name: " + localDevice.getFriendlyName());

        UUID uuid = new UUID(java.util.UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66").toString().replaceAll("-", ""), false);
        String connectionString = "btspp://localhost:" + uuid + ";name=Sample SPP Server";

        System.out.println(connectionString);

        // open server url
        try {
            StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionString);
            return streamConnNotifier;
        } catch (IOException e) {
            fireError(e);
        }
        return null;
    }

    @Override
    protected StreamConnection acceptConnection(StreamConnectionNotifier streamConnectionNotifier) throws Exception {
        if (streamConnectionNotifier == null){
           throw new LogicalException("StreamConnectionNotifier is null");
        }
        return streamConnectionNotifier.acceptAndOpen();
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
    public void registerMessage(ServerMessage message) {

    }


    public static void main(String[] args) throws Exception {
        AbstractServer server = new BluecoveServer()
            .setStateObserver(new ServerTestHandler(null))
            .addRequestBuilder(new HttpRequestBuilder())
            .setRequestHandler(new RequestHandler() {
                @Override
                public ServerResponse getResponse(AbstractServer serviceServer, ServerRequest request) {
                    System.out.println("RESPOND TO " + request);
                    return HttpResponse.plainText("OK");
                }

                @Override
                public boolean validate(ServerRequest request) {
                    return true;
                }

                @Override
                public void onDuplexConnect(AbstractServer ioHttp, ServerRequest request, Connection connection) {

                }

                @Override
                public void onFrame(Frame frame, Connection connection) {

                }

                @Override
                public ServerResponse getDefaultResponse(AbstractServer server) {
                    return HttpResponse.plainText("OK");
                }

                @Override
                public void onDuplexClose(Connection c) {

                }
            });
        server.start();
    }


}

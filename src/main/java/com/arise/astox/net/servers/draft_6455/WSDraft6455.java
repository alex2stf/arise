package com.arise.astox.net.servers.draft_6455;

import com.arise.astox.net.models.HttpProtocol;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.DuplexDraft;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.core.tools.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Date;


public class WSDraft6455 extends DuplexDraft<ServerRequest, ServerResponse> {
    public static final String WS_ACC_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    @Override
    public Connection createConnection(AbstractServer server, Object transporter, Object wrapper, Object key) {
        return new WSConnection(server, transporter, wrapper, key);
    }

    @Override
    public boolean isValidHandshakeRequest(ServerRequest request) {
        if (request != null && request instanceof HttpRequest){
            HttpRequest httpRequest = (HttpRequest) request;
            return httpRequest.containsHeader("Connection") && httpRequest.getHeaderParam("Connection").indexOf("Upgrade") > -1; //firefox trimite ceva, Upgrade
        }
        return false;
    }

    @Override
    public ServerResponse getHandshakeResponse(ServerRequest req) {
        HttpRequest request = (HttpRequest) req;
        try {
            String key = request.getHeaderParam("Sec-WebSocket-Key");
            String concatenatedKey = key + WS_ACC_KEY;
            byte[] sha1 =  MessageDigest.getInstance("SHA-1").digest(concatenatedKey.getBytes("UTF-8"));
            HttpResponse response = new HttpResponse();
            response.setProtocol(HttpProtocol.V1_0)
                    .setStatusCode(101)
                    .setStatusText("Switching Protocols")
                    .addHeader("Connection", "Upgrade")
                    .addHeader("Server", "Astox")
                    .addHeader("Date", new Date().toString())
                    .addHeader("Upgrade", "websocket")
                    .addHeader("Sec-WebSocket-Accept", Base64.encodeBytes(sha1));
            return response;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }



    @Override
    public DuplexInputStream buildInputStream(InputStream inputStream) {
        return new WebSocketInputStream(inputStream);
    }

    @Override
    public void parseBytes(byte[] readedBytes, Connection connection, ParseEvent parseEvent) {
        WebSocketInputStream webSocketInputStream = new WebSocketInputStream(new ByteArrayInputStream(readedBytes));
        try {
            WebSocketFrame frame;
            while (  (frame = webSocketInputStream.readFrame()) != null){
                if (frame.getOpcode() == WebSocketOpcode.CONTINUATION){
                    continue;
                }
                parseEvent.onFrameFound(frame, connection);
            }
        } catch (NoMoreFrameException e) {
            return;
        } catch (WebSocketException e) {
            parseEvent.onError(e);
        } catch (IOException e) {
            parseEvent.onError(e);
        }
    }
}

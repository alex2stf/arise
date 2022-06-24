package com.arise.astox.net.models;

import com.arise.astox.net.models.http.HttpResponseBuilder;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.Socket;

public abstract class AbstractStreamedSocketClient<I extends ServerRequest, O extends ServerResponse> extends Client<I, O, Socket> {


    protected static final HttpResponseBuilder httpResponseBuilder = new HttpResponseBuilder();

    @Override
    public Socket getConnection(I request) throws Exception {
        Socket socket;
        if (sslContext != null){
            socket = sslContext.getSocketFactory().createSocket(getHost(), getPort());
            ((SSLSocket) socket).startHandshake();
        }
        else if (getPort() != null){
            socket = new Socket(getHost(), getPort());
        }
        else {
            socket = new Socket(getHost(), 443);
        }
        return socket;
    }


    @Override
    protected void write(Socket socket, I request) {
        try {
            socket.getOutputStream().write(request.getBytes());
        } catch (IOException e) {
            //TODO treat exception
            e.printStackTrace();
        }
    }




}

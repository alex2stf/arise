package com.arise.astox.net.models;

import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.http.HttpResponseBuilder;
import com.arise.core.tools.Util;
import com.arise.core.tools.models.CompleteHandler;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class AbstractStreamedSocketClient<I extends ServerRequest, O extends ServerResponse> extends AbstractClient<I, O, Socket> {


    protected static final HttpResponseBuilder httpResponseBuilder = new HttpResponseBuilder();

    @Override
    protected Socket getConnection(I request) throws Exception {
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
            e.printStackTrace();
        }
    }




}

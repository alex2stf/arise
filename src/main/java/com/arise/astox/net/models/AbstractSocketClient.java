package com.arise.astox.net.models;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

public class AbstractSocketClient<I extends ServerRequest, O extends ServerResponse> extends AbstractClient<I, O, Socket> {



    @Override
    protected Socket getConnection() throws Exception {
        Socket socket;
        if (sslContext != null){
            socket = sslContext.getSocketFactory().createSocket(getHost(), getPort());
            ((SSLSocket) socket).startHandshake();
        } else {
            socket = new Socket(getHost(), getPort());
        }
        return socket;
    }

    @Override
    protected OutputStream getOutputStream(Socket socket) {
        try {
            return socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected InputStream getInputStream(Socket socket) {
        try {
            return socket.getInputStream();
        } catch (IOException e) {
            onError(e);
        }
        return null;
    }
}

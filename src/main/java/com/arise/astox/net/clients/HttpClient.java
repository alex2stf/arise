package com.arise.astox.net.clients;


import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.http.HttpResponseBuilder;
import com.arise.astox.net.models.AbstractPeer;
import com.arise.astox.net.models.AbstractStreamedSocketClient;
import com.arise.core.tools.Util;
import com.arise.core.tools.models.CompleteHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.ssl.SSLContext;

public class HttpClient extends AbstractStreamedSocketClient<HttpRequest, HttpResponse> {


    @Override
    public HttpClient setHost(String host) {
        return (HttpClient) super.setHost(host);
    }

    @Override
    public HttpClient setPort(int port) {
        return (HttpClient) super.setPort(port);
    }

    @Override
    public HttpClient setSslContext(SSLContext sslContext) {
        return (HttpClient) super.setSslContext(sslContext);
    }






    @Override
    protected void read(Socket socket, CompleteHandler<HttpResponse> responseHandler) {

        try {
            InputStream stream = socket.getInputStream();
            httpResponseBuilder.readInputStream(stream, new CompleteHandler<HttpResponse>() {
                @Override
                public void onComplete(HttpResponse data) {
                    responseHandler.onComplete(data);
                    Util.close(stream);
                    Util.close(socket);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}

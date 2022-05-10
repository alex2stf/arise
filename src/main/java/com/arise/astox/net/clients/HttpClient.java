package com.arise.astox.net.clients;


import com.arise.astox.net.models.AbstractStreamedSocketClient;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.models.Handler;
import com.arise.core.tools.Util;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

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
    protected void read(final Socket socket, final Handler<HttpResponse> responseHandler) {

        try {
            final InputStream stream = socket.getInputStream();
            httpResponseBuilder.readInputStream(stream, new Handler<HttpResponse>() {
                @Override
                public void handle(HttpResponse data) {
                    responseHandler.handle(data);
                    Util.close(stream);
                    Util.close(socket);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}

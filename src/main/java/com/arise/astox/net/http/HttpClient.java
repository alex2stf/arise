package com.arise.astox.net.http;


import com.arise.astox.net.models.AbstractPeer;
import com.arise.astox.net.models.AbstractSocketClient;

import javax.net.ssl.SSLContext;

public class HttpClient extends AbstractSocketClient<HttpRequest, HttpResponse> {

    private static final HttpResponseBuilder httpResponseBuilder = new HttpResponseBuilder();

    public HttpClient(){
        setBuilder(httpResponseBuilder);
    }

    public HttpClient(String host, int port){
        this.setHost(host)
            .setPort(port)
            .setBuilder(httpResponseBuilder);
    }

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


    public static HttpClient httpClientWithSslTrustAll(String host, int port) throws Exception {
        return new HttpClient(host, port).setSslContext(AbstractPeer.sslContextTLSV12AllowAll());
    }



}

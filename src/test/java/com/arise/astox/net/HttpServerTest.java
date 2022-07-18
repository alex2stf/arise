package com.arise.astox.net;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.http.HttpRequestBuilder;
import com.arise.astox.net.servers.HTTPServerHandler;
import com.arise.astox.net.servers.ServerTestHandler;
import com.arise.astox.net.servers.io.IOServer;

public class HttpServerTest {

    public static final int TEST_PORT = 8034;

    public static void doTest(AbstractServer server, HTTPServerHandler handler) throws Exception {
        server.setPort(TEST_PORT)
                .setName("test-server")
                .setRequestHandler(handler)
                .setRequestBuilder(new HttpRequestBuilder())
                .setStateObserver(handler)
                .start();
    }

    public static void main(String[] args) throws Exception {


        doTest(new IOServer(), new ServerTestHandler());

    }
}

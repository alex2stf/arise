package com.arise.astox.net.servers;

import com.arise.astox.net.http.HttpRequestBuilder;
import com.arise.astox.net.http.HttpResponseBuilder;
import com.arise.astox.net.models.AbstractClient.CompletionHandler;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.AbstractServer.RequestHandler;
import com.arise.astox.net.models.AbstractServer.WriteCompleteEvent;
import com.arise.astox.net.models.ConnectionSolver;
import com.arise.astox.net.models.DuplexDraft.Connection;
import com.arise.astox.net.models.DuplexDraft.Frame;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.servers.io.IOServer;

public class BluecoveLib {



    public static class HttpProxyServer extends IOServer {

    }


    public static class ProxyHandler extends ServerResponse {

        @Override
        public boolean isSelfManageable() {
            return true;
        }

        @Override
        public void onTransporterAccepted(ServerRequest request, Object... transporters) {

            BluecoveClient client = new BluecoveClient();
            client.setBuilder(new HttpResponseBuilder());
            final ConnectionSolver solver = new ConnectionSolver(transporters);



            System.out.println(request);
            client.send(request, new CompletionHandler<ServerResponse>() {
                @Override
                public void onComplete(ServerResponse response) {
                    solver.server().write(response.bytes(), solver, new WriteCompleteEvent() {
                        @Override
                        public void onComplete() {
                            System.out.println("DONE");
                            solver.closeStreamables();
                        }
                    });
                }
            });



        }
    }


    public static void main(String[] args) throws Exception {
        AbstractServer server = new HttpProxyServer()
            .setPort(5005)
            .addRequestBuilder(new HttpRequestBuilder())
            .setName("BlueCoveHttpProxy")
            .setStateObserver(AbstractServer.DEBUG_OBSERVER);

        final ProxyHandler proxyHandler = new ProxyHandler();


        server.setRequestHandler(new RequestHandler() {
            @Override
            public ServerResponse getResponse(AbstractServer serviceServer, ServerRequest request) {
                return proxyHandler;
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
                return proxyHandler;
            }

            @Override
            public void onDuplexClose(Connection c) {

            }
        });
        server.start();
    }
}

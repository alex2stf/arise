package com.arise.astox.net.serviceHelpers;

import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.DuplexDraft;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.core.tools.Mole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPServerHandler implements AbstractServer.StateObserver, AbstractServer.RequestHandler {
    private static final Mole log = Mole.getInstance(HTTPServerHandler.class);

    protected String rootInfo;
    private Map<String, ResponseBuilder> responseMap = new HashMap<>();
    private Map<String, ServerResponse> staticResponses = new HashMap<>();
    protected List<DuplexDraft.Connection> duplexConnections = new ArrayList<>();


    @Override
    public void postInit(AbstractServer server) {
        rootInfo = server.getConnectionPath();
    }

    @Override
    public void onError(AbstractServer serviceServer, Throwable err) {
//        System.out.println("");
    }

    public HTTPServerHandler addRoot(String route, ResponseBuilder responseBuilder) {
        responseMap.put(route, responseBuilder);
        return this;
    }

    public HTTPServerHandler addRoot(String route, ServerResponse response) {
        staticResponses.put(route, response);
        return this;
    }

    @Override
    public ServerResponse getResponse(AbstractServer server, ServerRequest request) {
        if (request instanceof HttpRequest){
            return getHTTPResponse((HttpRequest) request, server);
        }
        return null;
    }

    public HttpResponse getHTTPResponse(HttpRequest request, AbstractServer server){
        for (Map.Entry<String, ResponseBuilder> entry : responseMap.entrySet()) {
            if (entry.getKey().equals(request.path())) {
                return entry.getValue().build(request);
            }
        }
        return HttpResponse.oK();
    }

    @Override
    public boolean validate(ServerRequest request) {
        return false;
    }

    @Override
    public void onDuplexConnect(AbstractServer ioHttp, ServerRequest request, DuplexDraft.Connection connection) {
        duplexConnections.add(connection);
        broadcast("Hello client!!!");
    }

    @Override
    public void onFrame(DuplexDraft.Frame frame, DuplexDraft.Connection connection) {

    }

    @Override
    public ServerResponse getDefaultResponse(AbstractServer server) {
        return null;
    }

    @Override
    public void onDuplexClose(DuplexDraft.Connection c) {
        log.info("remove duplex " + c);
    }


    public void broadcast(String message) {
        for (DuplexDraft.Connection c : duplexConnections) {
            try {
                c.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract static class ResponseBuilder {
        public abstract HttpResponse build(ServerRequest request);
    }

    public static class Wrap extends ResponseBuilder {

        private final HttpResponse response;

        public Wrap(HttpResponse response) {
            this.response = response;
        }

        @Override
        public HttpResponse build(ServerRequest request) {
            return response;
        }
    }
}

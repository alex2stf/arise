package com.arise.astox.net.servers;

import com.arise.astox.net.serviceHelpers.DefaultServerHandler;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;

import com.arise.core.tools.StreamUtil;
import com.arise.astox.net.http.HttpClient;
import com.arise.astox.net.http.HttpEntity.Protocol;
import com.arise.astox.net.http.HttpRequest;
import com.arise.astox.net.http.HttpResponse;
import com.arise.astox.net.models.AbstractClient.CompletionHandler;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.DuplexDraft;
import com.arise.astox.net.models.ServerRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;

public class ServerTestHandler extends DefaultServerHandler {
    private static final Mole log = new Mole(ServerTestHandler.class);
    private static final Map<String, String> msgs = new HashMap<>();

    static {
        msgs.put("msg-1", "msg1-response");
        msgs.put("msg-2", "msg2-response");
        msgs.put("msg-3", "msg3-response");
        msgs.put("msg-4", "msg4-response");
        msgs.put("msg-5", "msg5-response");
        msgs.put("73812&*(^#*&(!##*($#@(*$^@#*(&$@#%$&^@#GOBDD)#(E*#)QUDJO(D@*^TG$", "892430472349709n ^#^(#& \n\n\n\n\n\n\nkkk");
        msgs.put("fdsfrs  dadfef", "892430472349709n ^#^(#& \n\n\n\n\n\n\nkkk");
    }

    private final SSLContext sslContext;

    public ServerTestHandler(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    public void onError(AbstractServer serviceServer, Throwable err) {
        if (err instanceof LogicalException){
            System.exit(-1);
        }
        super.onError(serviceServer, err);
    }

    @Override
    public void postInit(AbstractServer server) {
        super.postInit(server);
        HttpClient httpClient = null;
        try {
            httpClient = new HttpClient("localhost", 9221);
                //HttpClient.httpClientWithSslTrustAll("localhost", 9222);
        } catch (Exception e) {
            e.printStackTrace();
        }


        List<String> pathParams = new ArrayList<>();
        pathParams.add("ping");

        HttpRequest httpRequest = new HttpRequest("GET", pathParams, new HashMap<String, List<String>>(), new HashMap<String, String>(), Protocol.HTTP_1_1.text());


        httpClient.send(httpRequest, new CompletionHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse response) {
                System.out.println(response);
            }
        });

//        JHttpClient.disableSSL();
//
//        JHttpClient JHttpClient = new JHttpClient();
//        JHttpClient.setUrl(rootInfo + "ping");
//        JHttpClient.setMethod("POST");
//        JHttpClient.addHeader("Custom-Request-Header", "custom-header-value");
//        JHttpClient.execute(null, new JHttpClient.RequestHandler() {
//            @Override
//            public void handle(int responseCode, Map<String, List<String>> headers, InputStream stream, InputStream errorStream) {
//                boolean responseExists = false;
//                for (Map.Entry<String, List<String>> entry: headers.entrySet()){
//                    if ("Custom-Request-Header-Response".equals(entry.getKey())
//                            && "custom-header-value-Response".equals(entry.getValue().get(0))){
//                        responseExists = true;
//                        break;
//                    }
//                }
//                Assert.isTrue(responseExists);
//            }
//
//            @Override
//            public void err(Throwable error) {
//
//            }
//        });






    }

    @Override
    public void onDuplexConnect(AbstractServer ioHttp, ServerRequest request,  DuplexDraft.Connection connection) {
        log.info("Duplex connection: " + connection);
    }

    @Override
    public HttpResponse getHTTPResponse(HttpRequest request, AbstractServer server) {
        HttpResponse response = super.getHTTPResponse(request, server);
        if (response != null){
            return response;
        }
        
        String text = StreamUtil.toString(FileUtil.findStream("src/main/resources#common/websock.html"));
//        Template tmpl = Mustache.compiler().compile(text);
//        Map<String, String> args = new HashMap<String, String>();
//        args.put("port", String.valueOf(server.getPort()));
//        args.put("ws_host", server.getSslContext() != null ? "wss" : "ws");
//        args.put("request_debug", request.toString());
//        args.put("test_messages", new Gson().toJson(msgs));
//
        return HttpResponse.html(text);
    }

    @Override
    public void onFrame(DuplexDraft.Frame frame, DuplexDraft.Connection connection) {

        String message = frame.getPayloadText();
        log.info("RECEIVED MESSAGE [" + message + "] from connection " + connection.toString());
        if (msgs.containsKey(message)){
            log.info("sending response [" + msgs.get(message) + "]");
            connection.send(msgs.get(message));
        }
        else if (message != null){
            connection.send(" RECEIVED UNWANTED " + message);
        }
        else {
            log.info(" RECEIVED " + frame);
        }
    }



    @Override
    public void onDuplexClose(DuplexDraft.Connection c) {

    }


}

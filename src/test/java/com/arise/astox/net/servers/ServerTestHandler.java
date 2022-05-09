package com.arise.astox.net.servers;

import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.DuplexDraft;
import com.arise.astox.net.models.ServerRequest;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.weland.utils.WelandServerHandler;

import javax.net.ssl.SSLContext;
import java.util.HashMap;
import java.util.Map;

public class ServerTestHandler extends WelandServerHandler {
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
        super(null);
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
        System.out.println(server + "post init");
//        super.postInit(server);
//        HttpClient httpClient = null;
//        try {
//            httpClient = new HttpClient("localhost", 9221);
//                //HttpClient.httpClientWithSslTrustAll("localhost", 9222);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        List<String> pathParams = new ArrayList<>();
//        pathParams.add("ping");
//
//        HttpRequest httpRequest = new HttpRequest("GET", pathParams, new HashMap<String, List<String>>(), new HashMap<String, String>(), Protocol.HTTP_1_1.text());
//


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
        Whisker whisker = new Whisker();

        Map<String, String> args = new HashMap<String, String>();
        args.put("port", String.valueOf(server.getPort()));
        args.put("test_messages", Groot.toJson(msgs));
        args.put("ws_host", server.getSslContext() != null ? "wss" : "ws");
        args.put("request_debug", request.toString());

        return HttpResponse.html(whisker.compile(text, args));
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

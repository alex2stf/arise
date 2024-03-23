package com.arise.astox.net.servers;

import com.arise.astox.net.HttpServerTest;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.DuplexDraft;
import com.arise.astox.net.models.ServerRequest;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.tools.Assert;
import com.arise.core.tools.Mole;
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

    public ServerTestHandler() {
        this.sslContext = null;
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
        Assert.assertNotNull(server);
        Assert.assertIntEquals(HttpServerTest.TEST_PORT, server.getPort());
   }

    @Override
    public void onDuplexConnect(AbstractServer ioHttp, ServerRequest request,  DuplexDraft.Connection connection) {
        log.info("Duplex connection: " + connection);
    }

//    @Override
//    public HttpResponse getHTTPResponse(HttpRequest req, AbstractServer server) {
//        HttpResponse response = super.getHTTPResponse(req, server);
//        if (response != null){
//            return response;
//        }
//
//        String text = StreamUtil.toString(FileUtil.findStream("src/main/resources#common/websock.html"));
//        Whisker whisker = new Whisker();
//
//        Map<String, String> args = new HashMap<String, String>();
//        args.put("port", String.valueOf(server.getPort()));
//        args.put("test_messages", Groot.toJson(msgs));
//        args.put("ws_host", server.getSslContext() != null ? "wss" : "ws");
//        args.put("request_debug", req.toString());
//
//        return HttpResponse.html(whisker.compile(text, args));
//    }

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

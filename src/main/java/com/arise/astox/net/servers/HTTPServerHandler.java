package com.arise.astox.net.servers;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;

public class HTTPServerHandler implements AbstractServer.StateObserver, AbstractServer.RequestHandler {

    protected String rootInfo;


    @Override
    public void postInit(AbstractServer server) {
        rootInfo = server.getConnectionPath();
    }

    @Override
    public void onError(AbstractServer serviceServer, Throwable err) {
//        System.out.println("");
    }


    @Override
    public ServerResponse getResponse(AbstractServer serviceServer, ServerRequest request) {
        return null;
    }

    @Override
    public boolean validate(ServerRequest request) {
        return false;
    }





    @Override
    public ServerResponse getExceptionResponse(AbstractServer s, Throwable t) {
        if (t != null) {
            return HttpResponse.plainText(StringUtil.dump(t));
        }
        return HttpResponse.plainText("null response");
    }





}

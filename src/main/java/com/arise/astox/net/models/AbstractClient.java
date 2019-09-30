package com.arise.astox.net.models;

import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractClient<I extends ServerRequest, O extends ServerResponse, CONNECTION> extends AbstractPeer {
    protected ServerResponseBuilder builder;
    protected volatile boolean connected;

    public ServerResponseBuilder getBuilder() {
        return builder;
    }

    public AbstractClient setBuilder(ServerResponseBuilder serverResponseBuilder) {
        this.builder = serverResponseBuilder;
        return this;
    }

    protected abstract CONNECTION getConnection() throws Exception;

    protected CONNECTION gConn;


    public void connect(final CompletionHandler<CONNECTION> completionHandler){
        ThreadUtil.startThread(new Runnable() {
            @Override
            public void run() {
                if (!connected){
                    try {
                        gConn = getConnection();
                    } catch (Exception e) {
                       onError(e);
                    }
                }
                completionHandler.onComplete(gConn);

            }
        });
    }



    //TODO make queue

    /**
     *
     * @param request
     * @param completionHandler
     */
    public void send(final I request, final CompletionHandler<O> completionHandler){
        this.connect(new CompletionHandler<CONNECTION>() {
            @Override
            public void onComplete(CONNECTION localConection) {
                OutputStream outputStream = getOutputStream(localConection);
                try {
                    outputStream.write(request.getBytes());
                } catch (IOException e) {
                    onError(e);
                }
                InputStream inputStream = getInputStream(localConection);
                if (inputStream != null) {
                    ServerResponse response = builder.buildFromInputStream(inputStream);
                    completionHandler.onComplete((O) response);
                    Util.close(localConection);
                }
            }
        });
    }


    public void send(final I request){
        this.send(request, new CompletionHandler<O>() {
            @Override
            public void onComplete(O response) {
                System.out.println("SENT DONE " + response);
            }
        });
    }


    protected abstract OutputStream getOutputStream(CONNECTION connection);
    protected abstract InputStream getInputStream(CONNECTION connection);



    protected void onError(Throwable t){
        t.printStackTrace();
    }

    public interface CompletionHandler<O> {
        void onComplete(O response);
    }
}

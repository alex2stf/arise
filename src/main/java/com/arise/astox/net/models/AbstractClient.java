package com.arise.astox.net.models;

import com.arise.astox.net.clients.JHttpClient;
import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.models.CompleteHandler;


public abstract class AbstractClient<I extends ServerRequest, O extends ServerResponse, CONNECTION> extends AbstractPeer {


    protected abstract CONNECTION getConnection(final I request) throws Exception;

    public void connect(final I request, final CompleteHandler<CONNECTION> connectHandler){
        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                try {
                    final CONNECTION connection = getConnection(request);
                    connectHandler.onComplete(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void send(final I request, final CompleteHandler<CONNECTION> handler){
        connect(request, new CompleteHandler<CONNECTION>() {
            @Override
            public void onComplete(CONNECTION connection) {
                write(connection, request);
                handler.onComplete(connection);
            }
        });
    }

    protected abstract void write(CONNECTION connection,  I request);
    protected abstract void read(CONNECTION connection, CompleteHandler<O> responseHandler);





    public void sendAndReceive(final I request, final CompleteHandler<O> responseHandler){
        this.send(request, new CompleteHandler<CONNECTION>() {
            @Override
            public void onComplete(CONNECTION data) {
                read(data, responseHandler);
            }
        });
    }


    protected void onError(Throwable t){
       if (errorHandler != null){
           errorHandler.onComplete(t);
       }
    }



    private CompleteHandler<Throwable> errorHandler = null;


    public AbstractClient setErrorHandler(CompleteHandler<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public void close(){

    };


   public String getId(){
       return getConnectionPath();
   }


}

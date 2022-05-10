package com.arise.astox.net.models;

import com.arise.core.models.Handler;

import java.util.UUID;

import static com.arise.core.tools.ThreadUtil.fireAndForget;


public abstract class AbstractClient<I extends ServerRequest, O extends ServerResponse, CONNECTION> extends AbstractPeer {


    public abstract CONNECTION getConnection(final I request) throws Exception;

    public void connect(final I request, final Handler<CONNECTION> connectHandler){
        fireAndForget(new Runnable() {
            @Override
            public void run() {
                try {
                    final CONNECTION connection = getConnection(request);
                    connectHandler.handle(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "AbstractClient#Connect-" + UUID.randomUUID().toString());
    }


    public void send(final I request, final Handler<CONNECTION> handler){
        connect(request, new Handler<CONNECTION>() {
            @Override
            public void handle(CONNECTION connection) {
                write(connection, request);
                handler.handle(connection);
            }
        });
    }

    protected abstract void write(CONNECTION connection,  I request);
    protected abstract void read(CONNECTION connection, Handler<O> responseHandler);





    public void sendAndReceive(final I request, final Handler<O> responseHandler){
        this.send(request, new Handler<CONNECTION>() {
            @Override
            public void handle(CONNECTION data) {
                read(data, responseHandler);
            }
        });
    }


    protected void onError(Throwable t){
       if (errorHandler != null){
           errorHandler.handle(t);
       }
    }



    private Handler<Throwable> errorHandler = null;


    public AbstractClient setErrorHandler(Handler<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public void close(){

    };


   public String getId(){
       return getConnectionPath();
   }


}

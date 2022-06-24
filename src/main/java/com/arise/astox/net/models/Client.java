package com.arise.astox.net.models;

import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;

import java.util.UUID;

import static com.arise.core.tools.ThreadUtil.startThread;


public abstract class Client<I extends ServerRequest, O extends ServerResponse, CONNECTION> extends Peer {


    public abstract CONNECTION getConnection(final I request) throws Exception;

    public void connect(final I request, final Handler<CONNECTION> connectHandler){
        startThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final CONNECTION connection = getConnection(request);
                    connectHandler.handle(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Client#Connect-" + UUID.randomUUID());
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
       if (erh != null){
           erh.handle(Tuple2.<Throwable, Peer>of(t, this));
       }
    }



    private Handler<Tuple2<Throwable, Peer>> erh = null;



    public Client setErrorHandler(Handler<Tuple2<Throwable, Peer>> erh) {
        this.erh = erh;
        return this;
    }

    public void close(){

    };


   public String getId(){
       return getConnectionPath();
   }


}

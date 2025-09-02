package com.arise.astox.net.models;


import com.arise.core.tools.Mole;
import com.arise.core.tools.ThreadUtil;

import javax.net.ssl.SSLContext;

public abstract class AbstractServer<READABLE> extends Peer {


    protected volatile boolean running = false;

    protected StateObserver stateObserver;
    protected RequestHandler requestHandler;



    protected ServerRequestBuilder serverRequestBuilder;

    public AbstractServer(){

    }


    @Override
    public AbstractServer setUuid(String uuid) {
        return (AbstractServer) super.setUuid(uuid);
    }


    @Override
    public AbstractServer setName(String name) {
        return (AbstractServer) super.setName(name);
    }


    public AbstractServer setRequestBuilder(ServerRequestBuilder serverRequestBuilder){
        this.serverRequestBuilder = serverRequestBuilder;
        return this;
    }


    public abstract void write(byte[] bytes, ConnectionSolver connectionSolver, WriteCompleteEvent event);


    public AbstractServer setStateObserver(StateObserver stateObserver) {
        this.stateObserver = stateObserver;
        return this;
    }

    public AbstractServer setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        return this;
    }

    public void start() throws Exception {
        running = true;
    }

    public boolean isSecure() {
        return sslContext != null;
    }

    public void stop(){
        running = false;
    }

    public final void restart() throws Exception {
        synchronized (this){
            stop();
            start();
        }
    }


    @Override
    public AbstractServer setHost(String host) {
        return (AbstractServer) super.setHost(host);
    }

    @Override
    public AbstractServer setPort(int port) {
        return (AbstractServer) super.setPort(port);
    }


    @Override
    public AbstractServer setSslContext(SSLContext sslContext) {
        return (AbstractServer) super.setSslContext(sslContext);
    }

    /**
     * should be used by duplex communications
     * @param message
     */
    public abstract void registerMessage(ServerMessage message);

    protected void firePostInit(){
        final AbstractServer s = this;
        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                if (stateObserver != null) {
                    stateObserver.postInit(s);
                }
            }
        }, "Post-Init");
    }

    protected void fireError(Throwable t){
        stateObserver.onError(this, t);
    }




//    @Deprecated
//    protected void readPayload(READABLE stream, ReadCompleteHandler<ServerRequest> completeHandler){
//        solveInterceptor(serverRequestBuilder, stream, completeHandler);
//    };





//    protected abstract void solveInterceptor(ServerRequestBuilder builder, READABLE data, ReadCompleteHandler<ServerRequest> completeHandler);
//    protected abstract void solveSingleRequestAfterValidation(ServerRequest request, ServerRequestBuilder builder, READABLE data) throws Exception;











    public interface StateObserver {
        void postInit(AbstractServer serviceServer);
        void onError(AbstractServer serviceServer, Throwable err);
    }



    public static final StateObserver DEBUG_OBSERVER = new StateObserver() {
        private final Mole log = Mole.getInstance("StateObserver$DEBUG");

        @Override
        public void postInit(AbstractServer serviceServer) {
            log.info("Started server " + serviceServer);
        }

        @Override
        public void onError(AbstractServer serviceServer, Throwable err) {
            err.printStackTrace();
        }
    };

    public interface RequestHandler {
        ServerResponse getResponse(final AbstractServer serviceServer, ServerRequest request);
        boolean validate(ServerRequest request);
        ServerResponse getExceptionResponse(AbstractServer s, Throwable t);
    }

    public abstract static class WriteCompleteEvent {
        public abstract void onComplete();
        public void onError(Throwable t){
            t.printStackTrace(); //TODO better
        }
    }
}

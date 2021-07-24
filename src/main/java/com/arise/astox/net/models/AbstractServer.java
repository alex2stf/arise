package com.arise.astox.net.models;


import com.arise.core.tools.Mole;
import com.arise.core.tools.ThreadUtil;

import javax.net.ssl.SSLContext;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class AbstractServer<READABLE> extends AbstractPeer {


    protected volatile boolean running = false;

    protected StateObserver stateObserver;
    protected RequestHandler requestHandler;


    protected BlockingQueue<ServerMessage> messagesToWrite = new ArrayBlockingQueue<ServerMessage>(200);

    private Set<DuplexDraft> duplexDrafts = new HashSet<>();
//    private Set<ServerRequestBuilder> requestBuilderSet = new HashSet<>();
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

//    public Set<ServerRequestBuilder> getBuilders() {
//        return Collections.unmodifiableSet(requestBuilderSet);
//    }

    public AbstractServer addDuplexDraft(DuplexDraft draft){
        duplexDrafts.add(draft);
        return this;
    }

    public AbstractServer setRequestBuilder(ServerRequestBuilder serverRequestBuilder){
        this.serverRequestBuilder = serverRequestBuilder;
        return this;
    }


    public abstract void write(byte[] bytes, ConnectionSolver connectionSolver, WriteCompleteEvent event);

    public StateObserver getStateObserver() {
        return stateObserver;
    }

    public AbstractServer setStateObserver(StateObserver stateObserver) {
        this.stateObserver = stateObserver;
        return this;
    }

    public RequestHandler getRequestHandler() {
        return requestHandler;
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




    public DuplexDraft<ServerRequest, ServerResponse> requestToDuplex(ServerRequest request){
        for (DuplexDraft<ServerRequest, ServerResponse> draft: duplexDrafts){
            if (draft.isValidHandshakeRequest(request)){
                return draft;
            }
        }
        return null;
    }

    public ServerResponse getDuplexHandshakeResponse(DuplexDraft<ServerRequest, ServerResponse> draft, ServerRequest request){
        try {
            return draft.getHandshakeResponse(request);
        } catch (Exception e) {
            fireError(e);
            return null;
        }
    }


    protected void onDuplexClose(DuplexDraft.Connection c) {
       requestHandler.onDuplexClose(c);
    }



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
        void onDuplexConnect(AbstractServer ioHttp, ServerRequest request, DuplexDraft.Connection connection);
        void onFrame(DuplexDraft.Frame frame, DuplexDraft.Connection connection);
        ServerResponse getDefaultResponse(AbstractServer server);
        void onDuplexClose(DuplexDraft.Connection c);

//        ServerResponse serverError(Exception ex);
    }

    public abstract static class WriteCompleteEvent {
        public abstract void onComplete();
        public void onError(Throwable t){
            t.printStackTrace(); //TODO better
        }
    }
}

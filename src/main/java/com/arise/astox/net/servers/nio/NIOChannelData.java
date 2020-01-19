package com.arise.astox.net.servers.nio;


import com.arise.astox.net.models.DuplexDraft;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;

import javax.net.ssl.SSLEngine;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.arise.astox.net.servers.nio.NIOChannelData.Type.DUPLEX;

public class NIOChannelData {
    private final SSLEngine sslEngine;


    private Type type;
    private DuplexDraft duplexDraft;
    private DuplexDraft.Connection duplexConnection;



    public DuplexState getDuplexState() {
        return duplexState;
    }

    public void setDuplexState(DuplexState duplexState) {
        this.duplexState = duplexState;
    }

    private DuplexState duplexState = DuplexState.UNSET;
    private ServerRequest request;
    private ServerResponse response;


    public NIOChannelData(Type type, SSLEngine sslEngine){
        this.type = type;
        this.sslEngine = sslEngine;
    }

    public DuplexDraft.Connection getDuplexConnection() {
        return duplexConnection;
    }

    public void setDuplexConnection( DuplexDraft.Connection duplexConnection) {
        this.duplexConnection = duplexConnection;
    }

    public DuplexDraft getDuplexDraft() {
        return duplexDraft;
    }

    public void setDuplexDraft(DuplexDraft duplexDraft) {
        this.duplexDraft = duplexDraft;
    }

    public ServerResponse getResponse() {
        return response;
    }

    public void setResponse(ServerResponse response) {
        this.response = response;
    }

    public SSLEngine getSslEngine() {
        return sslEngine;
    }

    public ServerRequest getRequest() {
        return request;
    }

    public void setRequest(ServerRequest request) {
        this.request = request;
    }


    public Type getType() {
        return type;
    }

    public boolean isDuplexConfigured() {
        return DUPLEX.equals(type) && duplexDraft != null;
    }

    public void setType(Type type) {
        this.type = type;
    }


    public boolean isCloseable() {
        return type.equals(Type.CLOSEABLE) ;
    }

    BlockingQueue<ByteBuffer> bufferBlockingQueue = new LinkedBlockingQueue<>();

    public void pushRead(ByteBuffer concat) {
        try {
            bufferBlockingQueue.put(concat);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public enum  Type {
        CLOSEABLE, DUPLEX
    }

    public enum DuplexState{
        UNSET, CONNECTING, ALIVE
    }

    @Override
    public String toString() {
        return "NIOChannelData{" +
                "type=" + type +
                ", duplexDraft=" + duplexDraft +
                ", duplexConnection=" + duplexConnection +
                ", duplexState=" + duplexState +
                ", isCloseable=" + isCloseable() +
                '}';
    }
}

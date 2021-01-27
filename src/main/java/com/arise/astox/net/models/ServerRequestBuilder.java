package com.arise.astox.net.models;

import com.arise.core.tools.models.CompleteHandler;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class ServerRequestBuilder<T extends ServerRequest> {

    protected Object connection;
    public abstract void readInputStream(InputStream inputStream,
                                         CompleteHandler<T> onComplete,
                                         CompleteHandler<Throwable> onError);

    public abstract void readSocketChannel(SocketChannel socketChannel,
                                           CompleteHandler<T> onComplete,
                                           CompleteHandler<Throwable> onError);

    public abstract void readByteBuffer(ByteBuffer input,
                                        CompleteHandler<T> onComplete,
                                        CompleteHandler<Throwable> onError);

    ServerRequestBuilder<T> withConnection(Object connection) {
        this.connection = connection;
        return this;
    }
}

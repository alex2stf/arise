package com.arise.astox.net.models;

import com.arise.core.models.Handler;

import java.io.InputStream;

public abstract class ServerRequestBuilder<T extends ServerRequest> {

    protected Object connection;
    public abstract void readInputStream(InputStream inputStream,
                                         Handler<T> onComplete,
                                         Handler<Throwable> onError);

    ServerRequestBuilder<T> withConnection(Object connection) {
        this.connection = connection;
        return this;
    }
}

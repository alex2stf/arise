package com.arise.astox.net.models;

import com.arise.core.models.Handler;

import java.io.InputStream;

public abstract class ServerResponseBuilder<T extends ServerResponse> {
    public abstract void readInputStream(InputStream inputStream, Handler<T> onComplete);
}

package com.arise.astox.net.models;

import com.arise.core.tools.models.CompleteHandler;

import java.io.InputStream;

public abstract class ServerResponseBuilder<T extends ServerResponse> {
    public abstract void readInputStream(InputStream inputStream, CompleteHandler<T> onComplete);
}

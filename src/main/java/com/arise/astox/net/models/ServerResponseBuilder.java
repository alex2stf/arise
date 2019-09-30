package com.arise.astox.net.models;

import java.io.InputStream;

public abstract class ServerResponseBuilder {
    public abstract ServerResponse buildFromInputStream(InputStream inputStream);
}

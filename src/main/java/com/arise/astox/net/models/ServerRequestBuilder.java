package com.arise.astox.net.models;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class ServerRequestBuilder {
    public abstract ServerRequest readHeader(InputStream inputStream) throws Exception;
    public abstract void solveRequestAfterValidation(ServerRequest serverRequest, InputStream inputStream) throws Exception;

    public abstract ServerRequest readHeader(SocketChannel socket) throws Exception;
    public abstract ServerRequest fromByteBuffer(ByteBuffer byteBuffer) throws Exception;
    public abstract void solveRequestAfterValidation(ServerRequest serverRequest, SocketChannel channel) throws Exception;

}

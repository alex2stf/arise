package com.arise.astox.net.http;

import static com.arise.astox.net.http.HttpEntity.HEADER_SEPARATOR;

import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerRequestBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class HttpRequestBuilder extends ServerRequestBuilder {


    public ServerRequest readHeader(InputStream inputStream) throws IOException {
        HttpEntity entity = new HttpEntity(true).extractHeadersFromInputStream(inputStream);
        return entity.request();
    }



    public ServerRequest readHeader(SocketChannel socket) throws IOException {
        StringBuilder sb = new StringBuilder();
        ByteBuffer buff = ByteBuffer.allocate(1);
        HttpRequest httpRequest = null;
        while (socket.read(buff) > 0){
            sb.append((char) buff.array()[0]);
            if (sb.toString().endsWith(HEADER_SEPARATOR)){
                httpRequest = new HttpEntity(true).extractHeaders(sb.toString()).request();
                break;
            }
            buff = ByteBuffer.allocate(1);
        }
        return httpRequest;
    }

    public ServerRequest fromByteBuffer(ByteBuffer byteBuffer) throws Exception {

        StringBuilder sb = new StringBuilder();
        HttpRequest serverRequest = null;
        byteBuffer.rewind();
        while (byteBuffer.hasRemaining()){
            sb.append((char) byteBuffer.get());
            if (sb.toString().endsWith(HEADER_SEPARATOR)){
                serverRequest = new HttpEntity(true).extractHeaders(sb.toString()).request();
                break;
            }
        }

        if (byteBuffer.position() < byteBuffer.limit()){
            int position = byteBuffer.position();
            int length = byteBuffer.limit() - position;
            System.out.println( "pozitia " + position + " din " + byteBuffer.limit() + " => size " + length);
            byte[] rest = new byte[length];
            byteBuffer.get(rest);
            serverRequest.setBytes(rest);
            System.out.println(serverRequest + "\nRequest body:" + new String(rest));
        }

        return serverRequest;
    }

    public void solveRequestAfterValidation(ServerRequest request, SocketChannel channel) throws Exception {
        if (request instanceof HttpRequest) {
            HttpRequest serverRequest = (HttpRequest) request;
            if (serverRequest != null && serverRequest.hasContent()) {
                ByteBuffer buff = ByteBuffer.allocate(serverRequest.contentLength());
                channel.read(buff);
                serverRequest.setBytes(buff.array());
            }
        }
        //todo throw error
    }

    @Override
    public void solveRequestAfterValidation(ServerRequest request, InputStream inputStream) throws Exception {
        if (request instanceof HttpRequest) {
            HttpRequest serverRequest = (HttpRequest) request;
            if (serverRequest.hasContent()) {
                byte buf[] = new byte[serverRequest.contentLength()];
                inputStream.read(buf);
                serverRequest.setBytes(buf);
            }
        }
    }



}

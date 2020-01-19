package com.arise.astox.net.models.http;

import com.arise.astox.net.models.ReadCompleteHandler;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerRequestBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;



public class HttpRequestBuilder extends ServerRequestBuilder<HttpRequest> {


    @Override
    public void readInputStream(final InputStream inputStream, final ReadCompleteHandler<HttpRequest> handler) {
        HttpRequestReader reader = new HttpRequestReader() {
            @Override
            public void handleRest(HttpReader x) {
//                    if (this.getRequest().hasContent()) {
//                        byte buf[] = new byte[getRequest().contentLength()];
//                        try {
//                            inputStream.read(buf);
//                            this.getRequest().setBytes(buf);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    getRequest().setBytes(bodyBytes.toByteArray());
                    handler.onReadComplete(this.getRequest());
                    flush();
            }
        };
        reader.readInputStream(inputStream);
    }

    @Override
    public void readSocketChannel(final SocketChannel channel, final ReadCompleteHandler<ServerRequest> handler) {
        HttpRequestReader reader = new HttpRequestReader() {
            @Override
            public void handleRest(HttpReader reader) {
                if (getRequest().hasContent()) {
                    ByteBuffer buff = ByteBuffer.allocate(getRequest().contentLength());
                    try {
                        channel.read(buff);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    getRequest().setBytes(buff.array());
                }
                handler.onReadComplete(getRequest());
                flush();
            }
        };

        try {
            ByteBuffer buff = ByteBuffer.allocate(1);
            while (channel.read(buff) > 0){
                reader.readChar(buff.array()[0]);
                buff = ByteBuffer.allocate(1);
            }
        } catch (Exception e){
            e.printStackTrace();
        }




    }

    @Override
    public void readByteBuffer(final ByteBuffer byteBuffer, final ReadCompleteHandler<ServerRequest> handler) {

        if (byteBuffer.limit() == 0){
            handler.onReadComplete(new HttpRequest());
            return;
        }

        HttpRequestReader reader = new HttpRequestReader() {
            @Override
            public void handleRest(HttpReader reader) {
                if (byteBuffer.position() < byteBuffer.limit()){
                    int position = byteBuffer.position();
                    int length = byteBuffer.limit() - position;
                    byte[] rest = new byte[length];
                    byteBuffer.get(rest);
                    getRequest().setBytes(rest);
                }
                handler.onReadComplete(getRequest());
                flush();
            }
        };


        byteBuffer.rewind();
        reader.setLimit(byteBuffer.limit());
        while (byteBuffer.hasRemaining()){
            reader.readChar(byteBuffer.get());
        }
    }












}

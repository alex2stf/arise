package com.arise.astox.net.models.http;

import com.arise.astox.net.models.ServerRequestBuilder;
import com.arise.core.models.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;



public class HttpRequestBuilder extends ServerRequestBuilder<HttpRequest> {


    @Override
    public void readInputStream(final InputStream inputStream,
                                final Handler<HttpRequest> onSuccess,
                                final Handler<Throwable> onError) {
        HttpRequestReader reader = new HttpRequestReader() {
            @Override
            public void handleRest(HttpReader x) {
                    getRequest().setBytes(bodyBytes.toByteArray());
                    onSuccess.handle(this.getRequest());
                    flush();
            }

            @Override
            public void onError(Throwable e) {
                onError.handle(e);
            }
        };
        reader.readInputStream(inputStream);
    }

    @Override
    public void readSocketChannel(final SocketChannel channel,
                                  final Handler<HttpRequest> onSuccess,
                                  final Handler<Throwable> onError) {
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
                onSuccess.handle(getRequest());
                flush();
            }

            @Override
            public void onError(Throwable e) {
                onError.handle(e);
            }
        };

        try {
            ByteBuffer buff = ByteBuffer.allocate(1);
            while (channel.read(buff) > 0){
                reader.readChar(buff.array()[0]);
                buff = ByteBuffer.allocate(1);
            }
        } catch (Exception e){
            onError.handle(e);
        }




    }

    @Override
    public void readByteBuffer(final ByteBuffer byteBuffer,
                               final Handler<HttpRequest> onSuccess,
                               final Handler<Throwable> onError) {

        if (byteBuffer.limit() == 0){
            onSuccess.handle(new HttpRequest());
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
                onSuccess.handle(getRequest());
                flush();
            }

            @Override
            public void onError(Throwable e) {
                onError.handle(e);
            }
        };


        byteBuffer.rewind();
        reader.setLimit(byteBuffer.limit());
        while (byteBuffer.hasRemaining()){
            reader.readChar(byteBuffer.get());
        }
    }



}

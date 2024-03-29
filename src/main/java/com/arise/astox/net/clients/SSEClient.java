package com.arise.astox.net.clients;

import com.arise.astox.net.models.http.HttpReader;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponseReader;
import com.arise.core.models.Handler;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static java.util.UUID.randomUUID;


public class SSEClient extends HttpClient {

    @Override
    public SSEClient setPort(int port) {
        return (SSEClient) super.setPort(port);
    }

    @Override
    public SSEClient setHost(String host) {
        return (SSEClient) super.setHost(host);
    }

    Thread thread;
    volatile boolean closed = false;
    @Override
    public void connect(final HttpRequest request, final Handler<Socket> connectHandler) {
        closed = false;
        thread = ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = getConnection(request);
                    connectHandler.handle(socket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "sse-connect-" + randomUUID());
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onError(Throwable t) {
        super.onError(t);
    }

    public void subscribe(final HttpRequest request, final Consumer consumer){
        this.connect(request, new Handler<Socket>() {
            @Override
            public void handle(final Socket socket) {
                try {
                    final OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(request.getBytes());

                    final InputStream inputStream = socket.getInputStream();
                    HttpResponseReader responseReader = new HttpResponseReader() {
                        @Override
                        public void handleRest(HttpReader reader) {
//                            reset();
//                            byte [] bytes = bodyBytes.toByteArray();
//                            for (int i = 0; i < bytes.length; i++){
//                                builder.append((char)bytes[i]);
//                            }
//                            try {
//                                bodyBytes.flush();
//                                bodyBytes.close();
//                                bodyBytes = null;
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            if (builder.toString().endsWith("\n\n")){
//                                parseBlock(reset(), consumer);
//                            }
//
//                            try {
//                                byte [] buff = new byte[1];
//                                while (inputStream.read(buff) > 0){
//                                    char c = (char) buff[0];
//                                    builder.append(c);
//                                    if (builder.toString().endsWith("\n\n")){
//                                        parseBlock(reset(), consumer);
//                                    }
//                                    if (closed){
//                                        flush();
//                                        Util.close(inputStream);
//                                        Util.close(outputStream);
//                                        break;
//                                    }
//                                }
//                            } catch (IOException e) {
//                                builder.setLength(0);
//                                builder = null;
//                                Util.close(inputStream);
//                                Util.close(outputStream);
//                                Util.close(socket);
//                            }
                        }
                    };
                    responseReader.readInputStream(inputStream);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void parseBlock(String block, Consumer consumer) {
        if (!StringUtil.hasContent(block)){
            return;
        }
        String lines[] = block.split("\n");
        for (String line: lines){
            pushLine(line);
        }
        dispatchAndClear(consumer);
    }

    private void dispatchAndClear(Consumer consumer){
        consumer.onEventReceived(
                new SSEEvent().setEvent(event).setData(data).setId(id).setRetry(retry).setComment(comment)
        );
        event = null;
        data = null;
        id = null;
        retry = null;
        comment = null;
    }

    @Override
    public void close() {
        closed = true;
    }

    String event;
   String id;
   String comment;
   String data;
   Long retry;

    private void pushLine(String line) {
        int index = line.indexOf(":");
        if (index < 0){
            return;
        }

        String key = line.substring(0, index);
        String value = line.substring(index + 1).trim();

        if ("event".equalsIgnoreCase(key)){
            event = value;
        }
        else if ("retry".equalsIgnoreCase(key)){
            try {
                retry = Long.valueOf(value);
            }catch (Exception e){
                System.out.println("unable to convert  " + value + " to long");
            }
        }
        else if ("id".equalsIgnoreCase(key)){
            id = value;
        }
        else if ("data".equalsIgnoreCase(key)){
            data = value;
        }
        else {
            comment = value;
        }
    }


    public interface Consumer {
        void onEventReceived(SSEEvent event);
    }
}

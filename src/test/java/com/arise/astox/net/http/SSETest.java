package com.arise.astox.net.http;

import com.arise.astox.net.clients.SSEClient;
import com.arise.astox.net.clients.SSEEvent;
import com.arise.astox.net.models.AbstractStreamedSocketClient;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.core.tools.models.CompleteHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SSETest {


    public static void main(String[] args) throws Exception {
        SSEClient sseClient = new SSEClient()
//                .setPort(8033)
                .setPort(8778)
                .setHost("localhost");

        HttpRequest request = new HttpRequest().setMethod("GET").setUri("/flux/nodes")
                .addQueryParam("apiKey", "some_value");


        final int[] counter = {0};

        System.out.println("subsscrieb!!!");

        sseClient.subscribe(request, new SSEClient.Consumer() {
            @Override
            public void onEventReceived(SSEEvent event) {
                System.out.println(counter[0] + ")" + event);
                counter[0]++;
                if (counter[0] > 3){
                    sseClient.close();
                }
            }
        });

//        sseClient.send(request, new CompletionHandler<Socket>() {
//            @Override
//            public void onComplete(Socket data) {
//
//                HttpReader httpReader = new HttpReader(RESPONSE) {
//
//
//                    @Override
//                    public void handleRest(HttpReader reader) {
//                        if (reader.endsWith("\n")){
//                            String line = reader.reset();
//                            if(!line.trim().isEmpty()){
//                                System.out.println("X|" + line);
//                            }
//                        }
//                    }
//
//                    @Override
//                    protected void onHeadersParsed(Map<String, String> headers) {
//                        for (Map.Entry<String, String> entry: headers.entrySet()){
//                            System.out.println(entry.getKey() + " == " + entry.getValue());
//                        }
//                    }
//
//                    @Override
//                    protected void onStatusFound(Integer status) {
//                        System.out.println("status " + status);
//                    }
//
//                    @Override
//                    protected void onProtocolFound(HttpProtocol protocol) {
//                        System.out.println("protocol"+ protocol);
//                    }
//                };
//                httpReader.readInputStream(inputStream);
//
//            }
//        });

    }


}

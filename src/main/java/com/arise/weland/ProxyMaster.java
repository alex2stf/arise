package com.arise.weland;

import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpRequestBuilder;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.servers.io.IOServer;
import com.arise.core.tools.Util;
import com.arise.core.tools.models.CompleteHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ProxyMaster {

    public static void main(String[] args) throws Exception {
        HttpRequestBuilder httpRequestBuilder = new HttpRequestBuilder();


        IOServer ioServer = new IOServer(){

            @Override
            protected void handle(Socket server) {
                try {
                    httpRequestBuilder.readInputStream(server.getInputStream(), new CompleteHandler<HttpRequest>() {
                        @Override
                        public void onComplete(HttpRequest data) {
                            System.out.println("SERVER received" + data);
                            OutputStream out = null;
                            try {
                                out = server.getOutputStream();
                            } catch (IOException e) {
                                System.out.println("Failed to obtain server getOutputStream");
                                Util.close(out);
                                return;
                            }
                            if (data.pathsStartsWith("proxy")){
                                try {
                                    out.write(HttpResponse.htmlResource("src/main/resources#weland/proxy.html").bytes());
                                    Util.close(out);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return;
                            }

                            Socket client = null;
                            InputStream in = null;
                            try {
                                client = new Socket("192.168.1.6", 8221);
                                client.getOutputStream().write(data.getBytes());
                                byte[] buf = new byte[8192];
                                int length;
                                in = client.getInputStream();
//                                out = server.getOutputStream();
                                while ((length = in.read(buf)) > 0) {
                                    out.write(buf, 0, length);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Util.close(client);
                            Util.close(in);
                            Util.close(out);
                        }
                    }, new CompleteHandler<Throwable>() {
                        @Override
                        public void onComplete(Throwable data) {

                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }//end handle
        }; //end instance
        ioServer.setPort(1234);
        ioServer.start();

    }
}

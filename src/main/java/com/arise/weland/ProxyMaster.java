package com.arise.weland;

import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpRequestBuilder;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.servers.io.IOServer;
import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.Util;
import com.arise.core.tools.models.CompleteHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class ProxyMaster {

    static String host = null;
    static String port = null;
    static final Whisker whisker = new Whisker();
    static final Mole log = Mole.getInstance(ProxyMaster.class);
    static int proxyPort = 1234;

    static String PROXY_PORT_PROP = "weland.proxy.port";

    public static void main(String[] args) throws Exception {
        if (System.getProperty(PROXY_PORT_PROP) != null){
            try {
                proxyPort = Integer.valueOf(System.getProperty(PROXY_PORT_PROP));
            }catch (Exception e){

            }
        }

        log.info("Weland proxy started on port: " + proxyPort);


        HttpRequestBuilder httpRequestBuilder = new HttpRequestBuilder();


        IOServer ioServer = new IOServer(){

            @Override
            protected void handle(Socket acceptedSocket) {
                try {
                    httpRequestBuilder.readInputStream(acceptedSocket.getInputStream(), new CompleteHandler<HttpRequest>() {
                        @Override
                        public void onComplete(HttpRequest data) {
                            OutputStream out = null;
                            try {
                                out = acceptedSocket.getOutputStream();
                            } catch (IOException e) {
                                System.out.println("Failed to obtain server getOutputStream");
                                Util.close(out);
                                return;
                            }
                            if (data.pathsStartsWith("proxy")){
                                try {
                                    java.util.Map<String, String> params = new HashMap<>();
                                    params.put("host", data.getQueryParam("host"));
                                    params.put("port", data.getQueryParam("port"));
                                    host = data.getQueryParam("host");
                                    port = data.getQueryParam("port");
                                    log.info("Defined root " + host + ":" + port);
                                    String content = StreamUtil.toString(FileUtil.findStream("src/main/resources#weland/proxy.html"));
                                    out.write(HttpResponse.html(whisker.compile(content, params)).bytes());
                                    Util.close(out);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return;
                            }

                            Socket client = null;
                            InputStream in = null;

                            try {
                                client = new Socket(host, Integer.valueOf(port));
                            } catch (IOException e) {
                                log.error("Failed to connect", e);
                                Util.close(client);
                                Util.close(acceptedSocket);
                                return;
                            }
                            try {
                                client.setKeepAlive(true);
                            } catch (SocketException e) {
                                log.error("cannot keep alive", e);
                            }

                            try {
                                client.setSoTimeout(1000 * 60 * 60 * 5);
                            } catch (SocketException e) {
                                log.error("cannot set read timeout", e);
                            }

                            try {
                                client.getOutputStream().write(data.getBytes());
                            } catch (IOException e) {
                                log.error("Failed to write request", e);
                                Util.close(client);
                                Util.close(acceptedSocket);
                                return;
                            }
                            byte[] buf = new byte[8192];
                            int length = 0;
                            try {
                                in = client.getInputStream();
                            } catch (IOException e) {
                                log.error("Failed to get client inputStream");
                                Util.close(client);
                                Util.close(acceptedSocket);
                                return;
                            }

                            long start = System.currentTimeMillis();

                            while (true) {
                                try {
                                    if (!((length = in.read(buf)) > 0)) break;
                                } catch (IOException e) {
                                    long end = System.currentTimeMillis();
                                    long diff = (end - start);
                                    log.error("Failed to read client for request" + data + "after " + diff + " miliseconds" , e);
                                    Util.close(in);
                                    Util.close(client);
                                    Util.close(acceptedSocket);
                                    return;
                                }
                                try {
                                    out.write(buf, 0, length);
                                }catch (Exception ex){
                                    log.error("Failed to write bytes to client", ex);
                                    Util.close(in);
                                    Util.close(client);
                                    Util.close(acceptedSocket);
                                    return;
                                }
                            }
                            try {
                                out.flush();
                            } catch (IOException e) {
                                log.error("Failed to flush");
                            }

                            Util.close(client);
                            Util.close(in);
                            Util.close(out);
                            Util.close(acceptedSocket);
                        }
                    }, new CompleteHandler<Throwable>() {
                        @Override
                        public void onComplete(Throwable data) {
                            log.error("Server error ", data);
                        }
                    });

                } catch (IOException e) {
                   log.error("Unexpected exception while parsing request ", e);
                }
            }//end handle
        }; //end instance
        ioServer.setPort(proxyPort);
        ioServer.start();

    }
}

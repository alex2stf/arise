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
            log.info("start proxy on port: " + proxyPort);
        }

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

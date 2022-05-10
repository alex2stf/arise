package com.arise.weland.utils;

import com.arise.astox.net.models.HttpProtocol;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.tools.Mole;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

import static com.arise.core.tools.StringUtil.jsonVal;
import static com.arise.core.tools.Util.close;

public class ProxyHttpResponse extends HttpResponse {

    static final Mole log = Mole.getInstance(ProxyHttpResponse.class);



    @Override
    public boolean isSelfManageable() {
        return true;
    }

    private byte[] err(String message, String requestUrl){
        return HttpResponse.json("{\"err\": "+ jsonVal(message) +", \"test\": "+jsonVal(requestUrl)+"}").bytes();
    }



    private void writeError(OutputStream out, String message, String requestUrl){
        try {
            out.write(err(message, requestUrl));
        } catch (IOException e) {
            log.error("Failed to write error");
        }
    }

    @Override
    public void onTransporterAccepted(ServerRequest serverRequest, Object... transporters) {
        InputStream in = null;
        OutputStream out = null;
        Socket acceptedSocket = (Socket) transporters[1];

        HttpRequest serverHttpRequest = (HttpRequest) serverRequest;
        String host = serverHttpRequest.getQueryParam("host");
        String port = serverHttpRequest.getQueryParam("port");
        String protocol = serverHttpRequest.getQueryParam("protocol");
        String path = serverHttpRequest.getQueryParam("path");
        String requestUrl = protocol + "://" + host + ":" + port  + path;


        try {
            out = acceptedSocket.getOutputStream();
        } catch (IOException e) {
            log.error("Failed to get acceptedSocket inputStream", e);
            close(acceptedSocket);
            return;
        }

        Socket client = null;
        try {
            client = new Socket(host, Integer.valueOf(port));
        } catch (IOException e) {

            log.error("Failed to create socket client on " + requestUrl, e);
            writeError(out, "client connection refused", requestUrl);
            close(out);
            close(client);
            close(acceptedSocket);
            return;
        }


        try {
            client.setKeepAlive(true);
        } catch (SocketException e) {
            log.error("cannot keep alive", e);
        }


        try {
            //TODO make it configurable
            client.setSoTimeout(1000 * 2);
        } catch (SocketException e) {
            log.error("cannot set read timeout", e);
        }

        HttpRequest request;
        try {
            request = new HttpRequest().setMethod("GET").setUri(path).setProtocol(HttpProtocol.V1_0);
            client.getOutputStream().write(request.getBytes());
        } catch (IOException e) {
            log.error("Failed to write request", e);
            writeError(out, "client write request failed", requestUrl);
            close(out);
            close(client);
            close(acceptedSocket);
            return;
        }

        byte[] buf = new byte[8192];
        int length = 0;
        try {
            in = client.getInputStream();
        } catch (IOException e) {
            log.error("Failed to get client inputStream", e);
            writeError(out, "client read failed", requestUrl);
            close(out);
            close(client);
            close(acceptedSocket);
            return;
        }



        long start = System.currentTimeMillis();

        while (true) {
            try {
                if (!((length = in.read(buf)) > 0)) {
                    break;
                }
            } catch (IOException e) {
                long end = System.currentTimeMillis();
                long diff = (end - start);
                log.error("Failed to read client for request" + request + "after " + diff + " miliseconds" , e);
                close(out);
                close(in);
                close(client);
                close(acceptedSocket);
                return;
            }
            try {
                out.write(buf, 0, length);
                try {
                    out.flush();
                } catch (IOException e) {
                    log.error("Failed to flush ", e);
                }
            } catch (Exception ex){
                log.error("Failed to write bytes to client", ex);
                close(in);
                close(out);
                close(client);
                close(acceptedSocket);
                return;
            }
        }//exit while

        close(client);
        close(in);
        close(out);
        close(acceptedSocket);


    }//exit method



}

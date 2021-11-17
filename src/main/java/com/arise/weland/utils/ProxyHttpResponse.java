package com.arise.weland.utils;

import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.Util;
import com.arise.weland.ProxyMaster;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.arise.core.tools.StringUtil.jsonVal;

public class ProxyHttpResponse extends HttpResponse {

    static final Mole log = Mole.getInstance(ProxyMaster.class);

    private final String requestUrl;

    public ProxyHttpResponse(String host, String port, String protocol, String path) {
        this.requestUrl = protocol + "://" + host + ":" + port + path;
    }

    @Override
    public boolean isSelfManageable() {
        return true;
    }

    private byte[] err(String message){
        return HttpResponse.json("{\"err\": "+ jsonVal(message) +", \"test\": "+jsonVal(requestUrl)+"}").bytes();
    }

    private void write(OutputStream outputStream, byte [] bytes){
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write outputstram for " + requestUrl);
        }
    }

    @Override
    public void onTransporterAccepted(ServerRequest serverRequest, Object... transporters) {
        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection httpURLConnection = null;
        URL connectionURL = null;
        Socket socket = (Socket) transporters[1];

        try {
            out = socket.getOutputStream();
        } catch (IOException e) {
            Util.close(out);
            throw new RuntimeException("Failed to get outputstram for request " + serverRequest);
        }

        try {
            connectionURL = new URL(requestUrl);
        } catch (MalformedURLException e) {
            write(out, err("malformed url"));
            Util.close(out);
            return;
        }

        try {
            httpURLConnection = (HttpURLConnection) connectionURL.openConnection();
        } catch (IOException e) {
            write(out, err("cannot open request"));
            Util.close(out);
            disconnect(httpURLConnection);
            return;
        }

        try {
            httpURLConnection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            write(out, err("protocol exception"));
            Util.close(out);
            disconnect(httpURLConnection);
            return;
        }
        httpURLConnection.setConnectTimeout(3000);
        httpURLConnection.setReadTimeout(3000);

        try {
            httpURLConnection.connect();
        } catch (IOException e) {
            write(out, err("connect exception"));
            Util.close(out);
            disconnect(httpURLConnection);
            return;
        }

        int status = 200;

        try {
            status = httpURLConnection.getResponseCode();
        } catch (IOException e) {
            ;;
        }

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(status);
        for (Map.Entry<String, List<String>> entry : httpURLConnection.getHeaderFields().entrySet()) {
            httpResponse.addHeader(entry.getKey(), StringUtil.join(entry.getValue(), ","));
        }

        try {
            out.write(httpResponse.headerLine().getBytes());
//            out.write("\r\n".getBytes());
        } catch (IOException e) {
            write(out, err("failed to write header"));
            Util.close(out);
            disconnect(httpURLConnection);
            return;
        }



        try {
            in = httpURLConnection.getInputStream();
        } catch (IOException e) {
            write(out, err("get inputStrem exception"));
            Util.close(out);
            Util.close(in);
            disconnect(httpURLConnection);
            return;
        }
        byte[] buf = new byte[8192];
        int length = 0;


        while (true){
            try {
                if (!((length = in.read(buf)) > 0)) {
                    break;
                }
            } catch (IOException e) {
                log.error("Failed to read for " + requestUrl, e);
                break;
            }
            try {
//                System.out.println("write  " + new String(buf));
                out.write(buf, 0, length);
                try {
                    out.flush();
                } catch (IOException e) {
                    log.error("Failed to flush for " + requestUrl, e);
                }
            } catch (IOException e) {
                log.error("Failed to write for  " + requestUrl, e);
                break;
            }

        }



        Util.close(out);
        Util.close(in);
        disconnect(httpURLConnection);


    }


    private void disconnect(HttpURLConnection httpURLConnection){
        if (httpURLConnection != null){
            httpURLConnection.disconnect();
        }
    }
}

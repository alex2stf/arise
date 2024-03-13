package com.arise.astox.net.clients;

import com.arise.astox.net.models.Client;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.models.Handler;
import com.arise.core.tools.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JHttpClient extends Client<HttpRequest, HttpResponse, HttpURLConnection> {


    private String protocol = "http";

    @Override
    public JHttpClient setHost(String host) {
        return (JHttpClient) super.setHost(host);
    }

    @Override
    public JHttpClient setPort(int port) {
        return (JHttpClient) super.setPort(port);
    }

    private static final Mole log = Mole.getInstance(JHttpClient.class);




    public static void disableSSL(){
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            System.setProperty("https.protocols", "TLSv1.2");

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    public void sendAndReceiveSync(HttpRequest request, final Handler<HttpResponse> httpResponseCompleteHandler){
        connectSync(request, new Handler<HttpURLConnection>() {
            @Override
            public void handle(HttpURLConnection data) {
                read(data, httpResponseCompleteHandler);
            }
        });
    }



    public void connect(final HttpRequest request, final Handler<HttpURLConnection> completionHandler) {
        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
               connectSync(request, completionHandler);
            }
        }, "JHttpClient#onnect-" + UUID.randomUUID().toString());
    }


    public void connectSync(final HttpRequest request, final Handler<HttpURLConnection> completionHandler){
        try {

            HttpURLConnection con = getConnection(request);

            if (request.hasPayload()){
                con.setDoOutput(true);
                con.setRequestProperty("Content-Length", String.valueOf(request.payload().length));
            }

            try {
                con.setRequestMethod(request.method());
            } catch (ProtocolException e) {
                log.error("Protocol exception at " + _u);
                onError(e);
                return;
            }

            if (!CollectionUtil.isEmpty(request.getHeaders())){
                for (Map.Entry<String, String> entry: request.getHeaders().entrySet()){
                    con.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            try {
                con.connect();
                completionHandler.handle(con);
            } catch (Throwable e) {
                log.error("Failed to connect to " + _u);
                onError(e);
            }


        } catch (Exception e) {
            log.error("Generic failed to connect " + _u);
            onError(e);
        }
    }

    private String _u;

    public JHttpClient setAbsoluteUrl(String _u){
        this._u = _u;
        return this;
    }



    public HttpURLConnection getConnection(HttpRequest request) throws Exception {

        URL connectionURL;
        if (StringUtil.hasText(_u)){
            connectionURL = new URL(_u);
        }
        else {
            connectionURL = new URL("http://" + getHost() + ":" + getPort() + request.getUri());
        }
        //TODO url connection nu are nevoie de uri encodat


        HttpURLConnection res = null;
        try {
            res = (HttpURLConnection) connectionURL.openConnection();
        } catch (Throwable t){
            onError(t);
        }
        return res;
    }

    @Override
    protected void write(HttpURLConnection con, HttpRequest request) {
        if (request.hasPayload()){

            try {
                con.getOutputStream().write(request.payload());
                con.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
                onError(e);
            }
        }
    }

    @Override
    public void read(HttpURLConnection con, Handler<HttpResponse> responseHandler) {


        HttpResponse httpResponse = new HttpResponse();

        try {
            httpResponse.setStatusCode(con.getResponseCode());
        } catch (IOException e) {
            e.printStackTrace();
        }

        httpResponse.setContentType(ContentType.search(con.getContentType()))
                        .setContentLength(con.getContentLength());

        for (Map.Entry<String, List<String>> entry: con.getHeaderFields().entrySet()){
            if (entry.getKey() != null){
                httpResponse.addHeader(entry.getKey(), StringUtil.join(entry.getValue(), ","));
            }
        }
        InputStream inputStream;
        try  {
            inputStream = con.getInputStream();
            if (inputStream != null){
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                StreamUtil.transfer(inputStream, outputStream);
                httpResponse.setBytes(outputStream.toByteArray());
                inputStream.close();
            }
        } catch (Exception ex){
            //TODO throw runtime error
            ex.printStackTrace();
        }

        if (responseHandler != null){
            responseHandler.handle(httpResponse);
        }
        con.disconnect();
    }










}

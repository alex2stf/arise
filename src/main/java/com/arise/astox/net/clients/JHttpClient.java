package com.arise.astox.net.clients;

import com.arise.astox.net.models.AbstractClient;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.http.HttpResponseBuilder;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.Util;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class JHttpClient extends AbstractClient<HttpRequest, HttpResponse, HttpURLConnection>  {


    private String protocol = "http";
    @Override
    public JHttpClient setHost(String host) {
        return (JHttpClient) super.setHost(host);
    }

    @Override
    public JHttpClient setPort(int port) {
        return (JHttpClient) super.setPort(port);
    }





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








    public void connect(final HttpRequest request, final CompleteHandler<HttpURLConnection> completionHandler) {
        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
               connectSync(request, completionHandler);
            }
        });
    }


    protected void connectSync(final HttpRequest request, final CompleteHandler<HttpURLConnection> completionHandler){
        try {

            HttpURLConnection con = getConnection(request);

            if (request.hasPayload()){
                con.setDoOutput(true);
                con.setRequestProperty("Content-Length", String.valueOf(request.payload().length));
            }

            try {
                con.setRequestMethod(request.method());
            } catch (ProtocolException e) {
                onError(e);
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
                completionHandler.onComplete(con);
            } catch (Throwable e) {
                e.printStackTrace();
                onError(e);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HttpURLConnection getConnection(HttpRequest request) throws Exception {
        URL connectionURL = new URL("http://" + getHost() + ":" + getPort() + request.getUri());

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
    protected void read(HttpURLConnection con, CompleteHandler<HttpResponse> responseHandler) {


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
            ex.printStackTrace();
        }

        if (responseHandler != null){
            responseHandler.onComplete(httpResponse);
        }
        con.disconnect();
    }



    public JHttpClient setUri(URI uri) {
        readUri(uri.toString());
        return this;
    }
}

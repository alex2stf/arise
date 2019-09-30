package com.arise.astox.clients;

import javax.net.ssl.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JHttpClient {


    protected String method;
    protected Map<String, String> headerParams = new HashMap<>();
    private String url;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void addHeader(String key, String value){
        headerParams.put(key, value);
    }





    public void execute(byte[] body, RequestHandler handler) {
        try {
            URL connectionURL = new URL(getUrl());

            HttpURLConnection con = (HttpURLConnection) connectionURL.openConnection();
            con.setRequestMethod(method);

            for (Map.Entry<String, String> arg : headerParams.entrySet()){
                con.setRequestProperty(arg.getKey(), arg.getValue());
            }

            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            //scrie body
            if (body != null){
                con.setDoOutput(true);
                con.setRequestProperty("Content-Length", String.valueOf(body.length));
                con.getOutputStream().write(body);
            }
            con.connect();

            handler.handle(
                    con.getResponseCode(),
                    con.getHeaderFields(),
                    con.getInputStream(),
                    con.getErrorStream()
            );

            con.disconnect();
        } catch (Exception ex){
            if (handler != null){
                handler.err(ex);
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public interface RequestHandler {
        void handle(int responseCode, Map<String,List<String>> headers, InputStream stream, InputStream errorStream);
        void err(Throwable error);
    }

}

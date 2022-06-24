package com.arise.astox.net.models;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public abstract class Peer {
    protected String name = "service-server";
    protected String uuid;
    protected SSLContext sslContext;
    private Integer port;
    private String host;

    public static SSLContext sslContextTLSV12AllowAll() throws Exception {
        SSLContext sslContext = null;
        sslContext = SSLContext.getInstance("TLSv1.2");
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
        sslContext.init(null, trustAllCerts, new SecureRandom());
        return sslContext;
    }

    public String getUuid() {
        return uuid;
    }

    public Peer setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getHost() {
        return host;
    }

    public Peer setHost(String host) {
        this.host = host;
        return this;
    }

    public Peer setUri(URI uri){
        this.setHost(uri.getHost());
        this.setPort(uri.getPort());
        return this;
    }


    public Peer setUri(String input){
        try {
            return setUri(new URI(input));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public Peer setPort(int port) {
        this.port = port;
        return this;
    }

    public String getName() {
        return name;
    }

    public Peer setName(String name) {
        this.name = name;
        return this;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public Peer setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }


    protected String getConnectionProtocol(){
        return "http";
    }

    public String getConnectionPath() {
        return (getSslContext() != null ? getConnectionProtocol() + "s" : getConnectionProtocol()) + "://" + (getHost() != null ? getHost() : "localhost") + ":" + getPort() + "/";
    }
}

package com.arise.astox.net.models;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public abstract class AbstractPeer {
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

    public AbstractPeer setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getHost() {
        return host;
    }

    public AbstractPeer setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public AbstractPeer setPort(int port) {
        this.port = port;
        return this;
    }

    public String getName() {
        return name;
    }

    public AbstractPeer setName(String name) {
        this.name = name;
        return this;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public AbstractPeer setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }
}

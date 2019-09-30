package com.arise.astox.net.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class HttpEntityTest {

    @Test
    public void extractHeaders() {

        String input = "GET / HTTP/1.1\n"
            + "Host: localhost:8222\n"
            + "Connection: keep-alive\n"
            + "Cache-Control: max-age=0\n"
            + "Upgrade-Insecure-Requests: 1\n"
            + "User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/71.0.3578.98 Chrome/71.0.3578.98 Safari/537.36\n"
            + "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\n"
            + "Accept-Encoding: gzip, deflate, br\n"
            + "Accept-Language: en-US,en;q=0.9\n"
            + "\n";
       HttpRequest request = new HttpEntity(true).extractHeaders(input).request();

        Assert.assertEquals(request.path(), "*");
        Assert.assertEquals(request.method(), "GET");
        Assert.assertEquals(request.protocol(), "HTTP/1.1");
        Assert.assertEquals(request.getHeaderParam("Cache-Control"), "max-age=0");



    }

    @Test
    public void testResponse(){
        String responseInput = "HTTP/1.0 200 OK\n"
            + "Server: Astox-Srv\n"
            + "Content-Length: 10\n"
            + "Date: Thu Feb 14 14:04:21 EET 2019\n"
            + "Content-Type: text/html\n"
            + "\n"
            + "1234567890\n";

        HttpResponse response;


        HttpResponseBuilder httpResponseBuilder = new HttpResponseBuilder();
        response = httpResponseBuilder.buildFromInputStream(new ByteArrayInputStream(responseInput.getBytes()));

        try {
            Assert.assertEquals(new String(response.payloadBytes(), "UTF-8"), "1234567890");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
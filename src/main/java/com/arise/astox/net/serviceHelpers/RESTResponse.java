package com.arise.astox.net.serviceHelpers;

import com.arise.astox.net.http.HttpResponse;
import com.arise.core.tools.ContentType;
import com.arise.astox.net.models.PayloadSerializer;

import java.util.Date;

public class RESTResponse extends HttpResponse {

    private final Object object;
    private final PayloadSerializer serializer;


    public RESTResponse(Object object, ContentType contentType, PayloadSerializer serializer){
        this.object = object;
        this.serializer = serializer;
        this.setStatusText("OK")
                .setProtocol("HTTP/1.0")
                .setHeaders(EMPTY_HEADERS)
                .addHeader("Server", "Astox-Srv")
                .addHeader("Date", new Date());
    }

    @Override
    public boolean isSelfManageable() {
        return false;
    }

    @Override
    public byte[] bytes() {
        return super.bytes();
    }

    @Override
    public byte[] payloadBytes() {
        byte[] bytes = serializer.serialize(object);
        addHeader("Content-Length", bytes.length);
        return bytes;
    }
}

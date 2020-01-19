package com.arise.astox.net.serviceHelpers;

import com.arise.astox.net.models.HttpProtocol;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.PayloadSerializer;
import com.arise.core.tools.ContentType;

import java.util.Date;

public class RESTResponse<T> extends HttpResponse {

    private final Object object;
    private final PayloadSerializer serializer;


    public RESTResponse(T object, ContentType contentType, PayloadSerializer<T> serializer){
        this.object = object;
        this.serializer = serializer;
        this.setStatusText("OK")
                .setProtocol(HttpProtocol.V1_0)
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



}

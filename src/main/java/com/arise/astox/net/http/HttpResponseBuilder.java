package com.arise.astox.net.http;

import com.arise.astox.net.models.ServerResponseBuilder;
import java.io.IOException;
import java.io.InputStream;

public class HttpResponseBuilder extends ServerResponseBuilder {


    public HttpResponse buildFromInputStream(InputStream inputStream) {
        HttpEntity entity = new HttpEntity(false);
        HttpResponse response = null;
        try {
            entity = entity.extractHeadersFromInputStream(inputStream);
            response = entity.response();
            if (response.contentLength() > 0){
                byte[] body = new byte[response.contentLength()];
                inputStream.read(body);
                response.setPayload(body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }


}

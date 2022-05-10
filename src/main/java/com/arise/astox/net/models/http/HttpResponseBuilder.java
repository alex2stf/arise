package com.arise.astox.net.models.http;

import com.arise.astox.net.models.ServerResponseBuilder;
import com.arise.core.models.Handler;
import com.arise.core.tools.Mole;

import java.io.InputStream;

public class HttpResponseBuilder extends ServerResponseBuilder<HttpResponse> {



    @Override
    public void  readInputStream(final InputStream inputStream, final Handler<HttpResponse> completeHandler) {

        HttpResponseReader reader = new HttpResponseReader() {


            @Override
            public void handleRest(HttpReader reader) {

                Mole.getInstance(HttpResponseBuilder.class).trace(getResponse());
                response.setBytes(bodyBytes.toByteArray());
                Mole.getInstance(HttpResponseBuilder.class).warn("READED  " + getResponse().toString());
                completeHandler.handle(response);
                flush();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };
        reader.readInputStream(inputStream);

    }



}

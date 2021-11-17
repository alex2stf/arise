package com.arise.astox.net.models.http;

import com.arise.astox.net.models.ServerResponseBuilder;
import com.arise.core.tools.Mole;
import com.arise.core.tools.models.CompleteHandler;

import java.io.InputStream;

public class HttpResponseBuilder extends ServerResponseBuilder<HttpResponse> {



    @Override
    public void  readInputStream(final InputStream inputStream, final CompleteHandler<HttpResponse> completeHandler) {

        HttpResponseReader reader = new HttpResponseReader() {


            @Override
            public void handleRest(HttpReader reader) {

                System.out.println(getResponse());
                response.setBytes(bodyBytes.toByteArray());
                Mole.getInstance(HttpResponseBuilder.class).warn("READED  " + getResponse().toString());

                completeHandler.onComplete(response);
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

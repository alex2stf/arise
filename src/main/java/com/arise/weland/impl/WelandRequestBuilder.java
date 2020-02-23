package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpReader;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpRequestBuilder;
import com.arise.astox.net.models.http.HttpRequestReader;
import com.arise.core.tools.models.CompleteHandler;

import java.io.InputStream;

public class WelandRequestBuilder extends HttpRequestBuilder {
    private final IDeviceController deviceController;

    public WelandRequestBuilder(IDeviceController deviceController) {
        this.deviceController = deviceController;
    }


    @Override
    public void readInputStream(final InputStream inputStream,
                                final CompleteHandler<HttpRequest> onSuccess,
                                final CompleteHandler<Throwable> onError) {

        HttpRequestReader reader = new HttpRequestReader() {

            @Override
            public void handleRest(HttpReader reader) {
                byte[] bytes = this.bodyBytes.toByteArray();
                if (bytes.length > 0 && bytes[0] == '>'){
                    deviceController.digestBytes(bytes);
                    resetBodyBytes();
                    this.readInputStream(inputStream);
                }
                else {
                    getRequest().setBytes(bodyBytes.toByteArray());
                    onSuccess.onComplete(this.getRequest());
                    flush();
                }
            }

            @Override
            public void onError(Throwable e) {
                onError.onComplete(e);
            }
        };

        reader.readInputStream(inputStream);

    }




}

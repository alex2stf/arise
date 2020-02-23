package com.arise.astox.net.models.http;

import com.arise.astox.net.models.HttpProtocol;

import java.util.Map;

public abstract class HttpResponseReader extends HttpReader<HttpResponse> {

    protected HttpResponse response = new HttpResponse();

    public HttpResponse getResponse() {
        return response;
    }



    @Override
    protected void onHeadersParsed(Map<String, String> headers) {
        response.setHeaders(headers);
    }

    @Override
    protected void digestLineParts(String[] parts) {
        HttpProtocol protocol = HttpProtocol.findByValue(parts[0]);
        if (protocol != null){
            onProtocolFound(protocol);
        }
        Integer status = null;
        try {
            status = Integer.valueOf(parts[1]);
        } catch (NumberFormatException ex){

        }
        if (status != null){
            onStatusFound(status);
        }
    }

    @Override
    protected void onStatusFound(Integer status) {
        response.setStatusCode(status);
    }

    @Override
    protected void onProtocolFound(HttpProtocol protocol) {
        response.setProtocol(protocol);
    }


    protected int getContentLength() {
        return response != null ? response.getContentLength() : 0;
    }

    @Override
    protected int headerLength() {
        String headerLine = response.headerLine();
        return headerLine != null ? headerLine.length() : 0;
    }

    @Override
    public void flush() {
        super.flush();
        response = new HttpResponse();
    }
}

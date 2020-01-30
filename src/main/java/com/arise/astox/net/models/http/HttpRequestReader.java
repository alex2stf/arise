package com.arise.astox.net.models.http;

import com.arise.astox.net.models.HttpProtocol;

import java.net.URLDecoder;
import java.util.Map;

public abstract class HttpRequestReader extends HttpReader<HttpRequest> {
    protected HttpRequest request = new HttpRequest();


    public HttpRequest getRequest() {
        return request;
    }

    @Override
    protected void onHeadersParsed(Map<String, String> headers) {
        request.setHeaders(headers);
    }

    @Override
    protected void digestLineParts(String[] parts) {
        String last = parts[ parts.length -1 ];
        boolean hasHeaders = true;
//        if (StringUtil.endsWithNewline(last)){
//            last = last.trim();
//            hasHeaders = false;
//        }
        HttpProtocol protocol = HttpProtocol.findByValue(last);

        if (protocol != null){
            onProtocolFound(protocol);
        }

        if (parts.length > 0 && !last.equals(parts[0])){
            String method = parts[0];
            onMethodFound(method);
        }

        if (parts.length > 1 ){
            String url = parts[1];
            onUrlFound(url);
        }


//        if (!hasHeaders){
//            headersReaded = true;
//            onHeadersReadComplete(reset());
//        }
    }

    @Override
    public void onUrlFound(String url) {
        try {
            String decoded = URLDecoder.decode(url, "UTF-8"); //use string for android devices
            request.setUri(decoded);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            request.setUri(url);
        }
    }

    @Override
    public void onMethodFound(String method) {
        request.setMethod(method);
    }

    @Override
    protected void onProtocolFound(HttpProtocol protocol) {
        request.setProtocol(protocol);
    }

    @Override
    protected int getContentLength() {
        return request != null ? request.contentLength() : 0;
    }


    @Override
    protected int headerLength() {
        String headerLine = request.headerLine();
        return headerLine != null ? headerLine.length() : 0;
    }

    @Override
    public void flush() {
        super.flush();
        request = new HttpRequest();
    }
}

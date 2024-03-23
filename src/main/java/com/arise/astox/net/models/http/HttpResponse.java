package com.arise.astox.net.models.http;


import com.arise.astox.net.models.HttpProtocol;
import com.arise.astox.net.models.ServerResponse;
import com.arise.core.tools.Assert;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.arise.astox.net.models.http.HttpReader.CRLF;
import static com.arise.astox.net.models.http.HttpReader.HEADER_SEPARATOR;
import static com.arise.core.tools.FileUtil.findStream;

public class HttpResponse extends ServerResponse {

    public static final Map<String, String> EMPTY_HEADERS = new HashMap<>();


    protected Map<String, String> _headers = new HashMap<>();
    private int statusCode = 200;
    private byte[] bytes;
    private int contentLength;
    private HttpProtocol protocol = HttpProtocol.V1_0;
    private String statusText = "OK";


    public HttpResponse() {

    }




    public static HttpResponse json(String content) {
        return oK().setContentType(ContentType.APPLICATION_JSON).setText(content);
    }


    public static HttpResponse html(String content) {
        return oK().addHeader("Content-Type", "text/html; charset=UTF-8")
                .addHeader("X-Content-Type-Options", "nosniff")
                .setText(content);
    }

    public static HttpResponse htmlResource(String file) {
        return html(StreamUtil.toString(findStream(file)));
    }

    public static HttpResponse javascript(String content) {
        return oK().setContentType(ContentType.APPLICATION_JAVASCRIPT).setText(content);
    }

    public static HttpResponse plainText(String content) {
        return oK().setContentType(ContentType.TEXT_PLAIN).setText(content);
    }



    private ContentType contentType;
    public HttpResponse setContentType(ContentType contentType) {
        Assert.assertNotNull(contentType);
        addHeader("Content-Type", contentType.toString());
        this.contentType = contentType;
        return this;
    }

    public ContentType getContentType(){
        return contentType;
    }


    public String headerLine() {
        StringBuffer sb = new StringBuffer();
        sb.append(protocol.value()).append(" ").append(statusCode).append(" ").append(statusText);
        if (!_headers.isEmpty()) {
            sb.append("\n");
            int cnt = 0;
            for (Map.Entry<String, String> entry : _headers.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue());
                if (cnt < _headers.size() - 1) {
                    sb.append("\n");
                } else {
                    sb.append(CRLF);
                }
                cnt++;
            }
            sb.append(CRLF);
        } else {
            sb.append(HEADER_SEPARATOR);
        }


        return sb.toString();
    }

    public HttpResponse addHeader(String s, Object any) {
        if (_headers == null) {
            _headers = new HashMap<>();
        }
        _headers.put(s, String.valueOf(any));
        return this;
    }


    public Map<String, String> headers() {
        return _headers;
    }

    @Override
    public String toString() {
        return new String(bytes());
    }

    public boolean isSelfManageable() {
        return false;
    }

    public HttpResponse setBytes(byte [] bytes) {
        this.bytes = bytes;
        if (contentLength < bytes.length){
            setContentLength(bytes.length);
        }
        return this;
    }

    public HttpResponse setText(String text){
        if (StringUtil.hasContent(text)){
            if (!text.endsWith("\n")){
                text+="\n";
            }
            setBytes(text.getBytes());
            setContentLength(text.length());
        }
        return this;
    }

    public HttpResponse setContentLength(int length) {
        addHeader("Content-Length", String.valueOf(length));
        contentLength = length;
        return this;
    }

    public HttpResponse setHeaders(Map<String, String> headers) {
        this._headers = headers;
        return this;
    }

    public HttpResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @Deprecated
    public String getProtocol() {
        return protocol.value();
    }

    public HttpResponse setProtocol(HttpProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getStatusText() {
        return statusText;
    }

    public HttpResponse setStatusText(String statusText) {
        this.statusText = statusText;
        return this;
    }

    public int status() {
        return statusCode;
    }


    public int getContentLength() {
        if (_headers.containsKey("Content-Length")) {
            return Integer.valueOf(headers().get("Content-Length"));
        }
        return contentLength;
    }


    public int getStatusCode() {
        return statusCode;
    }


    public byte[] bodyBytes() {
        return bytes;
    }


    public byte[] bytes() {

        byte[] headBytes = (headerLine().getBytes());
        int bodyLength = 0;
        if (bytes != null) {
            bodyLength = bytes.length;
        }

        byte[] result = new byte[headBytes.length + bodyLength];
        for (int i = 0; i < headBytes.length; i++) {
            result[i] = headBytes[i];
        }
        if (bytes != null) {
            for (int i = 0; i < bytes.length; i++) {
                result[i + headBytes.length] = bytes[i];
            }
        }
        return result;
    }

    public static HttpResponse oK() {
        return new HttpResponse()
                .setStatusCode(200)
                .setStatusText("OK")
                .setProtocol(HttpProtocol.V1_0)
                .addHeader("Server", "Astox-Srv")
                .addHeader("Date", new Date())

                ;
    }


    public HttpResponse addCorelationId(String correlationId) {
        addHeader("Correlation-Id", correlationId);
        return this;
    }

    public HttpResponse allowAnyOrigin() {
        return addHeader("Access-Control-Allow-Origin", "*")
                .addHeader("Access-Control-Allow-Credentials", "true")
                .addHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
    }
}

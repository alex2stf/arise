package com.arise.astox.net.http;


import com.arise.core.tools.ContentType;
import com.arise.astox.net.models.ServerResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse extends ServerResponse {

    public static final Map<String, String> EMPTY_HEADERS = new HashMap<>();


    protected Map<String, String> _headers = new HashMap<>();
    private int statusCode = 200;
    private Object obj;
    private String protocol = "HTTP/1.0";
    private String statusText = "OK";


    public HttpResponse(){

    }


    public HttpResponse(int statusCode,
                          ContentType contentType,
                          Object payload,
                          Map<String, String> headers,
                          String protocol,
                          String statusText){
        this.statusCode = statusCode;
        this.obj = payload;
        this._headers = headers;
        this.protocol = protocol;
        this.statusText = statusText;
        addHeader("Content-Type", contentType);

    }
    public HttpResponse(int statusCode,
                          Object payload,
                          Map<String, String> headers,
                          String protocol,
                          String statusText){
        this.statusCode = statusCode;
        this.obj = payload;
        this._headers = headers;
        this.protocol = protocol;
        this.statusText = statusText;
    }


    public static HttpResponse json(String content){
        return oK().setContentType(ContentType.APPLICATION_JSON).setPayload(content);
    }


    public static HttpResponse html(String content) {
        return oK().setContentType(ContentType.TEXT_HTML).setPayload(content);
    }

    public static HttpResponse javascript(String content) {
        return oK().setContentType(ContentType.APPLICATION_JAVASCRIPT).setPayload(content);
    }

    public static HttpResponse plainText(String content) {
        return oK().setContentType(ContentType.TEXT_PLAIN).setPayload(content);
    }

    public HttpResponse setContentType(ContentType contentType) {
        addHeader("Content-Type", contentType.alias());
        return this;
    }

    public byte[] payloadBytes() throws IOException {
        if (obj == null){
            return new byte[]{};
        }
        if (obj instanceof InputStream){
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream in = (InputStream) obj;
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            byte[] bytes = (buffer.toByteArray());
            addHeader("Content-Length", bytes.length);
            return bytes;
        }

        if (obj instanceof byte[]){
            return (byte[]) obj;
        }

        if (obj instanceof String){
            return ((String) obj).getBytes();
        }

        if (obj != null){
            return String.valueOf(obj).getBytes();
        }
        return null;
    }

    public String headerLine() {
        StringBuilder sb =  new StringBuilder();
        sb.append(protocol).append(" ").append(statusCode).append(" ").append(statusText);
        if (!_headers.isEmpty()){
            sb.append("\n");
            int cnt = 0;
            for (Map.Entry<String, String> entry: _headers.entrySet()){
                sb.append(entry.getKey()).append(":").append(entry.getValue());
                if (cnt < _headers.size() -1){
                    sb.append("\n");
                } else {
                    sb.append(HttpEntity.CRLF);
                }
                cnt++;
            }
            sb.append(HttpEntity.CRLF);
        } else {
            sb.append(HttpEntity.HEADER_SEPARATOR);
        }



        return sb.toString();
    }

    public HttpResponse addHeader(String s, Object any) {
        if (_headers == null){
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
        return headerLine();
    }

    public boolean isSelfManageable() {
        return false;
    }

    public HttpResponse setPayload(Object payload) {
        if (payload != null && payload instanceof String){
            setContentLength(((String)payload).getBytes().length);
        }
        else if (payload != null && payload instanceof byte[]){
            byte [] bytes = (byte[]) payload;
            setContentLength(bytes.length);
        }
        this.obj = payload;
        return this;
    }

    private HttpResponse setContentLength(int length) {
        addHeader("Content-Length", String.valueOf(length));
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

    public String getProtocol() {
        return protocol;
    }

    public HttpResponse setProtocol(String protocol) {
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

    public int status(){
        return statusCode;
    }


    public int contentLength() {
        if (_headers.containsKey("Content-Length")){
            return Integer.valueOf(headers().get("Content-Length"));
        }
        return -90;
    }
    
    
    public int getStatusCode() {
        return statusCode;
    }



    @Deprecated
    public InputStream stream() {
        if (obj instanceof InputStream){
            return (InputStream) obj;
        }
        return new ByteArrayInputStream(String.valueOf(obj).getBytes(Charset.forName("UTF-8")));
    }

    public byte[] bytes() {

        byte[] headBytes = (headerLine().getBytes(Charset.forName("UTF-8")));
        byte[] body = null;
        int bodyLength = 0;
        try {
            body = payloadBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (body != null){
            bodyLength = body.length;
        }

        byte[] result = new byte[headBytes.length + bodyLength];
        for (int i = 0; i < headBytes.length; i++){
            result[i] = headBytes[i];
        }
        if (body != null){
            for (int i = 0; i < body.length; i++){
                result[i + headBytes.length] = body[i];
            }
        }
        return result;
    }

    public static HttpResponse oK(){
        return new HttpResponse().setStatusCode(200)
                .setStatusText("OK")
                .setProtocol("HTTP/1.0")
                .addHeader("Server", "Astox-Srv")
                .addHeader("Date", new Date());
    }
}

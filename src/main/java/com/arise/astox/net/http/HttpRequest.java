package com.arise.astox.net.http;

import static com.arise.astox.net.http.HttpEntity.CRLF;

import com.arise.core.tools.StringUtil;
import com.arise.astox.net.models.ServerRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest extends ServerRequest {

    private final List<String> pathParams;
    private final Map<String, List<String>> queryParams;
    private final Map<String, String> headers;
    private  String remoteIp;
    private  String _host;
    private final String _method;
    private final String _protocol;

    private String rootUri;

    public byte[] getBytes() {
        return toString().getBytes();
    }

    public boolean containsHeader(String header){
        return headers != null && headers.containsKey(header);
    };

    private byte[] bytes;


    public InputStream inputStream(){
        return new ByteArrayInputStream(bytes);
    }


    public HttpRequest(String method,
                         List<String> pathParams,
                         Map<String, List<String>> queryParams,
                         Map<String, String> headers,
                         String protocol){
        this.pathParams = pathParams;
        this.queryParams = queryParams;
        this._method = method;
        this._protocol = protocol.trim();

        if (headers != null){
            this.headers = headers;
        } else {
            this.headers = new HashMap<String, String>();
        }


        if (pathParams.size() == 0){
            rootUri = "*";
        }
        else {
            rootUri = pathParams.get(0);
            if (rootUri.charAt(0) != '/'){
                rootUri = "/" + rootUri;
            }
            for (int i = 1; i < pathParams.size(); i++){
                rootUri+= "/" + pathParams.get(i);
            }
        }
    }

    public String path() {
        return rootUri;
    }

    public boolean noPath(){
        return rootUri == null || "*".equals(rootUri);
    }

    public List<String> getPathParams() {
        return pathParams;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getRemoteIp() {
        return remoteIp != null ? remoteIp : "";
    }

    public String host() {
        return _host != null ? _host : "";
    }

    protected String headerLine(){
        StringBuilder sb = new StringBuilder();
        sb.append(_method).append(" ");
        if (pathParams.isEmpty()){
            sb.append("/");
        } else {
            for (String s: pathParams){
                sb.append("/").append(s);
            }
        }

        if (!queryParams.isEmpty()){
            sb.append("?");
            int cnt = 0;
            for (Map.Entry<String, List<String>> e: queryParams.entrySet()){
                sb.append(e.getKey());
                if (!e.getValue().isEmpty()){
                    sb.append("=").append(StringUtil.toCSV(e.getValue()));
                }
                if (cnt < queryParams.size() -1 ){
                    sb.append("&");
                }
                cnt++;
            }
        }

        sb.append(" ").append(protocol());


        if (!headers.isEmpty()){
            sb.append(CRLF);
            for (Map.Entry<String , String> e: headers.entrySet()){
                sb.append(e.getKey()).append(":").append(e.getValue()).append(CRLF);
            }
        } else {
            sb.append(CRLF);
        }

        sb.append(CRLF);
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(headerLine());
        if (bytes != null) {
            try {
                sb.append(new String(bytes, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public String getQueryParam(String k){
        if (queryParams.get(k) != null && queryParams.get(k).size() > 0){
            return queryParams.get(k).get(0);
        }
        return null;
    }

    public String getHeaderParam(String k) {
        if (headers.containsKey(k) && headers.get(k) != null){
            return headers.get(k);
        }

        k = k.toLowerCase();
        if (headers.containsKey(k) && headers.get(k) != null){
            return headers.get(k);
        }
        return null;
    }

    public String getPathAt(int index){
        if (index < pathParams.size()){
            return pathParams.get(index);
        }
        return null;
    }

    public int contentLength(){
        Integer response = null;
        String val = getHeaderParam("Content-Length");
        try {
            response = Integer.valueOf(val);
        } catch (NumberFormatException e){
            response = null;
        }
        return response != null ? response.intValue() : 0;
    }

    public String method() {
        return _method;
    }

    public String protocol() {
        return _protocol;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }


    public String contentType(){
        if (headers.containsKey("content-type")){
            return headers.get("content-type");
        }
        return null;
    }



    public Integer getQueryParamInt(String arg) {
        String element = getQueryParam(arg);
        Integer response = null;
        try {
            response = Integer.valueOf(element);
        } catch (Exception e){
            return  null;
        }

        return response;
    }



    public boolean hasContent() {
        return contentLength() > 0;
    }




    public static HttpRequest plainText(String text){
        List<String> pathParams = new ArrayList<>();
        pathParams.add(text);
        return new HttpRequest("POST",  pathParams, new HashMap<String, List<String>>(), new HashMap<String, String>(), HttpEntity.Protocol.HTTP_1_0.text());
    }


}

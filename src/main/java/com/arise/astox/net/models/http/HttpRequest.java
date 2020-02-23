package com.arise.astox.net.models.http;

import com.arise.astox.net.models.HttpProtocol;
import com.arise.astox.net.models.ServerRequest;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.arise.astox.net.models.http.HttpReader.CRLF;
import static com.arise.core.tools.CollectionUtil.isEmpty;

public class HttpRequest extends ServerRequest {

    private List<String> pathParams;
    private Map<String, List<String>> queryParams;
    private Map<String, String> headers;
    private  String remoteIp;
    private  String _host;
    private  String _method;
    private HttpProtocol protocol = HttpProtocol.V1_0;


    public HttpRequest setProtocol(HttpProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public HttpRequest setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public byte[] getBytes() {
        return toString().getBytes();
    }

    public boolean containsHeader(String header){
        return headers != null && headers.containsKey(header);
    };

    private byte[] bytes;


    public byte[] payload(){
        return bytes;
    }

    public boolean hasPayload(){
        return bytes != null && bytes.length > 0;
    }

    public InputStream inputStream(){
        return new ByteArrayInputStream(bytes);
    }

    public HttpRequest setMethod(String method){
        this._method = method;
        return this;
    }

    public HttpRequest setUri(String uri){
        StringUtil.URLDecodeResult urlDecodeResult = StringUtil.urlDecode(uri);
        pathParams = urlDecodeResult.getPaths();
        queryParams = urlDecodeResult.getQueryParams();
        return this;
    }

    public HttpRequest addQueryParam(String key, String ... values){
        if (queryParams == null){
            queryParams = new HashMap<>();
        }
        queryParams.put(key, Arrays.asList(values));
        return this;
    }

    public HttpRequest(){

    }

    public HttpRequest(String method,
                         List<String> pathParams,
                         Map<String, List<String>> queryParams,
                         Map<String, String> headers,
                         String protocol){
        this.pathParams = pathParams;
        this.queryParams = queryParams;
        this._method = method;
        this.protocol = HttpProtocol.findByValue(protocol);

        if (headers != null){
            this.headers = headers;
        } else {
            this.headers = new HashMap<String, String>();
        }



    }

    public String path() {
        if (!CollectionUtil.isEmpty(pathParams)){
            return "/" + StringUtil.join(pathParams, "/");
        }
        return "/";
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


    public String getUri(){
        StringBuilder sb = new StringBuilder().append("/");
        if (!isEmpty(pathParams)){
            sb.append(StringUtil.join(pathParams, "/"));
        }
        if (!isEmpty(queryParams)){
            sb.append("?");
            int cnt = 0;
            for (Map.Entry<String, List<String>> e: queryParams.entrySet()){
                sb.append(e.getKey());
                if (!e.getValue().isEmpty()){
                    sb.append("=").append(
                            URLEncoder.encode(StringUtil.toCSV(e.getValue()))
                    );
                }
                if (cnt < queryParams.size() -1 ){
                    sb.append("&");
                }
                cnt++;
            }
        }
        return sb.toString();

    }

    protected String headerLine(){
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtil.hasText(_method) ? _method : "GET").append(" ").append(getUri());
        sb.append(" ").append(getProtocol().value());


        if (!isEmpty(headers)){
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
        if (CollectionUtil.isEmpty(headers)){
            return null;
        }
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

    public HttpProtocol getProtocol() {
        return protocol;
    }

    public HttpRequest setBytes(byte[] bytes) {
        this.bytes = bytes;
        setHeader("Content-Length", "" + bytes.length);
        return this;
    }

    public HttpRequest setHeader(String key, String value){
        if (headers == null){
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
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
        return new HttpRequest("POST",  pathParams, new HashMap<String, List<String>>(), new HashMap<String, String>(), HttpProtocol.V1_0.value());
    }


    public boolean pathsStartsWith(String ... s) {
        if (CollectionUtil.isEmpty(pathParams)){
            return false;
        }
        if (pathParams.size() < s.length){
            return false;
        }
        for (int i = 0; i < s.length; i++){
            if (!pathParams.get(i).equals(s[i])){
                return false;
            }
        }
        return true;
    }

    public String pathAt(int i) {
        return pathParams.get(i);
    }

    public void addHeader(String key, String value) {
        if (headers == null){
            headers = new HashMap<>();
        }
        headers.put(key, value);
    }
}

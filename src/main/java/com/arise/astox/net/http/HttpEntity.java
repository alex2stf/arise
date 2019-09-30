package com.arise.astox.net.http;

import com.arise.core.tools.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HttpEntity {

    public static final String HEADER_SEPARATOR = "\r\n\r\n";

    public static final String CRLF = "\r\n";
    private final boolean asRequest;


    URLDecodeResult urlDecodeResult;
    String method;
    Map<String, String> headers = new HashMap<>();
    String protocol;
    private String statusText;
    private String statusIntStr;

    public HttpEntity(boolean asRequest){
        this.asRequest = asRequest;
    }


    public HttpEntity extractHeadersFromInputStream(InputStream inputStream) throws IOException {
        byte[] buf = new byte[1];
        StringBuilder sb = new StringBuilder();
        while(inputStream.read(buf) > 0) {
            sb.append((char) buf[0]);

            System.out.println(sb.toString());
            if (sb.toString().endsWith(HEADER_SEPARATOR) || sb.toString().endsWith("\n\n")){
                return extractHeaders(sb.toString());
            }
        }
        System.out.println(sb.toString() + " might be invalid payload");
        return this;
    }


    public HttpEntity extractHeaders(String input){


        String lines[] = input.split("\n");
        String cap[] = lines[0].split(" ");
        String uri = "";

        if (asRequest) {
            method = cap[0];
            uri = cap[1];
            protocol = cap[2];
        } else {
            protocol = cap[0];
            statusIntStr = cap[1];
            statusText = cap[2];
        }

        for (int i = 1; i < lines.length; i++){
            String pieces[] = lines[i].split(":");
            if (pieces.length == 2){
                headers.put(pieces[0].trim(), pieces[1].trim());
            }
        }

        urlDecodeResult = decodeUrl(uri);

        if (urlDecodeResult == null){
            System.out.println(" INVALID DECODE RESULT FOR " + input);
        }

        return this;
    }


    public static URLDecodeResult decodeUrl(String input){
        if (input == null || input.trim().isEmpty()) {
            return new URLDecodeResult();
        }
        if (input.indexOf("/?/") == 0){
            input = input.substring(3, input.length());
        }
        String p[] = input.split("\\?");
        if(p.length > 1){
            return new URLDecodeResult(getPathParams(p[0]), StringUtil.getQueryParams(p[1]) );
        }
        return new URLDecodeResult(getPathParams(p[0]));
    }



    public static List<String> getPathParams(String uri){
        List<String> r = new ArrayList<String>();
        if (uri == null || uri.trim().isEmpty() || uri.trim().equalsIgnoreCase("/")){
            return r;
        }
        String p[] = uri.split("\\/");
        for (String s: p){
            if (s != null && !s.trim().isEmpty()){
                r.add(s);
            }
        }

        return r;
    }


    public HttpResponse response(){
        int statusCode = Integer.valueOf(statusIntStr);
        return new HttpResponse()
            .setHeaders(headers)
            .setProtocol(Protocol.HTTP_1_1.text())
            .setStatusText(statusText)
            .setStatusCode(statusCode);
    }


    public HttpRequest request() {
        return new HttpRequest(method != null ? method : "GET",
            urlDecodeResult != null ? urlDecodeResult.pathParams() : emptyPathParams(),
            urlDecodeResult != null ? urlDecodeResult.queryParameters() : emptyQueryParams(),
            headers != null ? headers : emptyHeaders(),
            protocol != null ? protocol : Protocol.HTTP_1_1.name());
    }


    public static final class URLDecodeResult {
        private final List<String> _pp;
        private final Map<String, List<String>> _qp;

        public URLDecodeResult(List<String> pp, Map<String, List<String>> qp) {
            _pp = pp;
            _qp = qp;
        }

        public URLDecodeResult() {
            _pp = new ArrayList<>();
            _qp = new HashMap<>();
        }

        public URLDecodeResult(List<String> pp) {
            _pp = pp;
            _qp = new HashMap<>();
        }

        public Map<String, List<String>> queryParameters() {
            return _qp;
        }

        public List<String> pathParams() {
            return _pp;
        }
    }

    public enum  Protocol {

        HTTP_1_1("HTTP/1.1"),
        HTTP_1_0("HTTP/1.0");

        private final String s;

        Protocol(String s) {
            this.s = s;
        }

        public String text() {
            return s;
        }


    }


    public static List<String> emptyPathParams(){
        return new ArrayList<>();
    }
    public static Map<String, List<String>> emptyQueryParams(){
        Map<String, List<String>> map = new HashMap<>();
        return map;
    }

    public static Map<String, String> emptyHeaders(){
        return new HashMap<String, String>();
    }
}

package com.arise.astox.net.models.http;

import com.arise.astox.net.models.HttpProtocol;
import com.arise.astox.net.models.ServerRequest;
import com.arise.core.tools.*;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.StringUtil.*;

public class HttpRequest extends ServerRequest {

    private List<String> _pp;
    private Map<String, List<String>> _qp;
    private Map<String, String> _hd;
    private String _mth;
    private HttpProtocol protocol = HttpProtocol.V1_0;
    private byte[] bytes;
    private String mfileName;
    private static String fileName;

    public static final String CRLF = "\r\n";
    public static final String HEADER_SEPARATOR = CRLF + CRLF;

    public boolean isHeaderReadComplete() {
        return headerReadComplete;
    }

    private boolean headerReadComplete;

    @Deprecated
    public HttpRequest() {

    }


    int lines = 0;

    int headerLength = 0;

    //todo package protected
    public void putHeaderLine(String line){
        if (lines == 0){
            String parts[] = line.split(" ");
            _mth = parts[0].trim();
            setUri(parts[1]);
            setProtocol(HttpProtocol.findByValue(parts[2]));
        }
        else {
            addHeaderLine(line);
        }
        lines++;
        headerLength += line.length();
    }

    public int getHeaderLength(){
        return headerLength;
    }

    private void addHeaderLine(String line) {
        if(_hd == null){
            _hd = new HashMap<>();
        }
        int index = line.indexOf(":");
        if (index > -1){
            String key = line.substring(0, index);
            String value = line.substring(index + 1);
            _hd.put(key.trim(), value.trim());
        }
    }


    public HttpRequest(String method,
                       List<String> pathParams,
                       Map<String, List<String>> queryParams,
                       Map<String, String> headers,
                       String protocol) {
        this._pp = pathParams;
        this._qp = queryParams;
        this._mth = method;
        this.protocol = HttpProtocol.findByValue(protocol);

        if (headers != null) {
            this._hd = headers;
        } else {
            this._hd = new HashMap<>();
        }


    }

    public static HttpRequest plainText(String text) {
        List<String> pathParams = new ArrayList<>();
        pathParams.add(text);
        return new HttpRequest("POST", pathParams, new HashMap<String, List<String>>(), new HashMap<String, String>(), HttpProtocol.V1_0.value());
    }

    public byte[] getBodyBytes() {
        return bytes;
    }

    ;

    public byte[] getBytes() {
        int bodySize = 0;
        if (bytes != null && bytes.length > 0) {
            bodySize = bytes.length;
        }
        String headerLine = headerLine();

        byte[] buff = new byte[bodySize + headerLine.length()];
        int i;
        for (i = 0; i < headerLine.length(); i++) {
            buff[i] = (byte) headerLine.charAt(i);
        }
        if (bytes != null && bytes.length > 0) {
            for (int j = 0; j < bytes.length; j++) {
                buff[i + j] = bytes[j];
            }
        }
        return buff;
    }

    public HttpRequest setBytes(byte[] bytes) {
        this.bytes = bytes;
        setHeader("Content-Length", "" + bytes.length);
        return this;
    }

    public boolean containsHeader(String header) {
        return _hd != null && _hd.containsKey(header);
    }

    public byte[] payload() {
        return bytes;
    }

    public boolean hasPayload() {
        return bytes != null && bytes.length > 0;
    }

    public InputStream inputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public HttpRequest setMethod(String method) {
        this._mth = method;
        return this;
    }

    public HttpRequest addQueryParam(String key, String... values) {
        if (_qp == null) {
            _qp = new HashMap<>();
        }
        _qp.put(key, Arrays.asList(values));
        return this;
    }

    public String path() {
        if (!CollectionUtil.isEmpty(_pp)) {
            return "/" + StringUtil.join(_pp, "/");
        }
        return "/";
    }

    public List<String> pathParams() {
        return _pp;
    }

    public Map<String, List<String>> queryParams() {
        return _qp;
    }

    public Map<String, String> getHeaders() {
        return _hd;
    }

    public HttpRequest setHeaders(Map<String, String> headers) {
        this._hd = headers;
        return this;
    }

    public String getUri() {
        return getUri(true);
    }

    public HttpRequest setUri(String uri) {
        StringUtil.URLDecodeResult urlDecodeResult = StringUtil.urlDecode(uri);
        _pp = urlDecodeResult.getPaths();
        _qp = urlDecodeResult.getQueryParams();
        return this;
    }

    public String getUri(boolean encoded) {
        StringBuilder sb = new StringBuilder().append("/");
        if (!isEmpty(_pp)) {
            sb.append(StringUtil.join(_pp, "/"));
        }
        if (!isEmpty(_qp)) {
            sb.append("?");
            int cnt = 0;
            for (Map.Entry<String, List<String>> e : _qp.entrySet()) {
                sb.append(e.getKey());
                if (!e.getValue().isEmpty()) {
                    String value = toCSV(e.getValue());
                    sb.append("=").append(
                            encoded ? urlEncodeUTF8(value) : value
                    );
                }
                if (cnt < _qp.size() - 1) {
                    sb.append("&");
                }
                cnt++;
            }
        }
        return sb.toString();

    }

    protected String headerLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(hasText(_mth) ? _mth : "GET").append(" ").append(getUri());
        sb.append(" ").append(getProtocol().value());


        if (!isEmpty(_hd)) {
            sb.append(CRLF);
            for (Map.Entry<String, String> e : _hd.entrySet()) {
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

    public String getQueryParam(String k) {
        if (_qp.get(k) != null && _qp.get(k).size() > 0) {
            try {
                return URLDecoder.decode(
                        _qp.get(k).get(0), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return _qp.get(k).get(0);
            }
        }
        return null;
    }

    public String[] getQueryParamList(String k) {
        if (_qp.get(k) != null && _qp.get(k).size() > 0) {
            return CollectionUtil.toArray(_qp.get(k));

        }
        return new String[]{};
    }

    public String getHeaderParam(String k) {
        if (CollectionUtil.isEmpty(_hd)) {
            return null;
        }
        if (_hd.containsKey(k) && _hd.get(k) != null) {
            return _hd.get(k);
        }

        k = k.toLowerCase();
        if (_hd.containsKey(k) && _hd.get(k) != null) {
            return _hd.get(k);
        }
        return null;
    }

    public String getPathAt(int index) {
        if (index < _pp.size()) {
            return _pp.get(index);
        }
        return null;
    }

    public int contentLength() {
        Integer response = null;
        String val = getHeaderParam("Content-Length");
        try {
            response = Integer.valueOf(val);
        } catch (NumberFormatException e) {
            response = null;
        }
        return response != null ? response.intValue() : 0;
    }

    public boolean isOptions() {
        return "OPTIONS".equalsIgnoreCase(_mth);
    }

    public String method() {
        return _mth;
    }

    public HttpProtocol getProtocol() {
        return protocol;
    }

    public HttpRequest setProtocol(HttpProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public HttpRequest setHeader(String key, String value) {
        if (_hd == null) {
            _hd = new HashMap<>();
        }
        _hd.put(key, value);
        return this;
    }

    public String contentType() {
        if (_hd.containsKey("content-type")) {
            return _hd.get("content-type");
        }
        return null;
    }

    public Integer getQueryParamInt(String arg) {
        String element = getQueryParam(arg);
        Integer response = null;
        try {
            response = Integer.valueOf(element);
        } catch (Exception e) {
            return null;
        }

        return response;
    }

    public int getQueryParamInt(String arg, int defaultValue) {
        Integer x = getQueryParamInt(arg);
        if (x == null) {
            return defaultValue;
        }
        return x;
    }

    public boolean hasContent() {
        return contentLength() > 0;
    }

    public boolean pathsStartsWith(String... s) {
        if (CollectionUtil.isEmpty(_pp)) {
            return false;
        }
        if (_pp.size() < s.length) {
            return false;
        }
        for (int i = 0; i < s.length; i++) {
            if (!_pp.get(i).equals(s[i])) {
                return false;
            }
        }
        return true;
    }

    public String pathAt(int i) {
        return _pp.get(i);
    }

    public HttpRequest addHeader(String key, String value) {
        if (_hd == null) {
            _hd = new HashMap<>();
        }
        _hd.put(key, value);
        return this;
    }

    public String getQueryParamString(String key, String defaultVal) {
        String x = getQueryParam(key);
        if (x == null) {
            return defaultVal;
        }
        return x;
    }


    @SuppressWarnings("unused")
    public boolean isMultipartFormData() {
        if(!isHeaderReadComplete()){
            return false;
        }
        if(!"POST".equalsIgnoreCase(method())){
            return false;
        }
        String ct = getHeaderParam("Content-Type");
        if (!hasText(ct)) {
            return false;
        }
        return ct.indexOf("multipart/form-data") > -1;
    }

    public String getBoundary() {
        String contentType = getHeaderParam("Content-Type");
        if (!hasText(contentType)) {
            return null;
        }
        String parts[] = contentType.split(";");
        for (String s : parts) {
            s = s.trim();
            if (s.startsWith("boundary=")) {
                String kv[] = s.split("=");
                if (kv.length == 2) {
                    return kv[1];
                }
            }
        }
        return null;

    }

    public int getContentLength() {
        String val = getHeaderParam("Content-Length");
        if(StringUtil.hasText(val)){
            try{
                return Integer.valueOf(val);
            }catch (NumberFormatException e){
                return 0;
            }
        }
        return 0;
    }


    public void setHeaderReadComplete(boolean headerReadComplete) {
        this.headerReadComplete = headerReadComplete;
    }

    private MFile mFile = null;



    public void startMFile() {
        mFile = new MFile();
    }

    public boolean mFileIsNull() {
        return mFile == null;
    }

    public boolean mFileCanWrite() {
        return !mFileIsNull() && mFile.canWrite;
    }

    public void mFileAddHeaderLine(String line) {
        if (line.trim().equalsIgnoreCase("")){
            return;
        }
        mFile.addHeaderLine(line);
    }

    public void setCanWriteMfile(boolean x) {
        mFile.canWrite  = x;
    }

    public void mFileWrite(byte b, ByteArrayOutputStream parentBytes) {
       if(mFile != null && !mFile.write(b)){
           startMFile();
           parentBytes.reset();
       }
    }





    public void closeMFilesResidue() {
        if (mFile != null){
            Util.close(mFile.checkStream);
            Util.close(mFile.fileStream);
        }
    }

    public boolean mFileHasHeaders() {
        return mFile.fName != null;
    }
    static final Mole log = Mole.getInstance(HttpRequest.class);


    private class MFile {
        public boolean canWrite;
        String fName = null;
        ByteQueue checkStream;
        ByteArrayOutputStream fileStream;



        public String setFileName(String fileName) {
            checkStream = new ByteQueue(getBoundary().length() + 50);
            fileStream = new ByteArrayOutputStream();
            return fileName;
        }


        public void addHeaderLine(String line) {
            if (fName == null){
                fName = getTitleFromHeaderLine(line);
            }
        }

        boolean write(byte b) {
            if (checkStream.endsWith("--" + getBoundary())){
                    log.info("write " + fName);

                    try {
                        FileOutputStream fos = new FileOutputStream(new File(FileUtil.getUploadDir(), fName));
                        byte bytes[] = fileStream.toByteArray();

//                        fos.write(bytes, 0, bytes.length - ("--" +getBoundary()).getBytes().length);
                        fos.write(bytes, 0, bytes.length - ("--" +getBoundary()).getBytes().length + 1);
                        Util.close(fos);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return false;

            }
            else {
                checkStream.write(b);
                fileStream.write(b);
            }
            return true;
        }

        public void close() {

        }

        String getTitleFromHeaderLine(String headerLine) {
            String headerLines[] = headerLine.split("\n");

            for (String hline : headerLines) {
                if (hline.indexOf(":") > -1) {
                    String hparts[] = hline.split(":");
                    if (hparts.length > 1) {
                        String value = hparts[1].trim();
                        String headerKey = hparts[0].trim();

                        if (value.indexOf(";") > -1) {
                            String vparts[] = value.split(";");

                            if (vparts.length > 0) {
                                for (String vp : vparts) {
                                    if (vp.indexOf("=") > -1) {
                                        String kv[] = vp.trim().split("=");
                                        if (kv.length > 1) {
                                            String key = kv[0].trim();
                                            String val = kv[1].trim();
                                            if ("filename".equalsIgnoreCase(key)) {
                                                return setFileName(val.replaceAll("\"", ""));

                                            }
//                                        if ("name".equalsIgnoreCase(key) &&  "Content-Disposition".equalsIgnoreCase(headerKey)){
//                                            return val.replaceAll("\"", "");
//                                        }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return UUID.randomUUID().toString();
        }


    }






}

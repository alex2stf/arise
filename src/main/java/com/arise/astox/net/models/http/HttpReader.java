package com.arise.astox.net.models.http;

import com.arise.astox.net.models.HttpProtocol;
import com.arise.core.tools.StringUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


@Deprecated
public abstract class HttpReader<TYPE> {
//    protected StringBuffer builder = new StringBuffer();
    protected ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream();

    protected volatile boolean headersReaded = false;
    int limit = -1;
    protected int readedBytes = 0;

    public HttpReader setLimit(int limit) {
        this.limit = limit;
        return this;
    }




    public static final String CRLF = "\r\n";
    public static final String HEADER_SEPARATOR = CRLF + CRLF;



    public synchronized void readChar(byte b) {
        readedBytes++;


        bodyBytes.write(b);

        if(!headersReaded) {
            try {
                String data = bodyBytes.toString("UTF-8");
                if (data.endsWith("\n\n") || data.endsWith("\r\n\r\n")){
                    onHeaderReadComplete(data);
                    headersReaded = true;
                    resetBodyBytes();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {

        }
    }




    public abstract void handleRest(HttpReader reader);


    protected void onHeaderReadComplete(String text){
        headersReaded = true;
        if (!StringUtil.hasContent(text)){
            return;
        }
        String lines[] = text.split("\n");
        onFirstLineReadComplete(lines[0]);


        Map<String, String> headers = new HashMap<>();

        for (int i = 1; i < lines.length; i++){
            String cline = lines[i].trim();
            int index = cline.indexOf(":");
            if (index > -1){
                String key = cline.substring(0, index);
                String value = cline.substring(index + 1);
                headers.put(key.trim(), value.trim());
            }
        }

        onHeadersParsed(headers);
    }

    protected abstract void onHeadersParsed(Map<String, String> headers);

    protected void onFirstLineReadComplete(String line){
        digestLineParts(line.split(" "));
    };

    protected abstract void digestLineParts(String[] parts);


    public void onUrlFound(String url) {

    }

    public void onMethodFound(String method) {

    }

    protected void onStatusFound(Integer status){

    }

    protected abstract void onProtocolFound(HttpProtocol protocol);



    protected abstract int getContentLength();
    protected abstract int headerLength();

    public void readInputStream(InputStream inputStream){


        try {
            int initChunk = 99999;
            int actualRead = 0;
            int sumRead;
            actualRead = readStream(inputStream, initChunk);
            sumRead = actualRead;

//            if (actualRead == -1){
//                System.out.println("ACTUAL READ -1");
//                return;
//            }
            //works in ndk
            if (getContentLength() > 0) {
//                while (sumRead < getContentLength() + headerLength() ){
                while (actualRead > 0 ){
                    actualRead = readStream(inputStream, initChunk);
                    sumRead += actualRead;
                }
            }

//            System.out.println("found " + new String(bodyBytes.toByteArray()) + " rest is ???");
            handleRest(this);
        } catch (Throwable t){
            onError(t);
        }

    }

    private int readStream(InputStream inputStream, int chunkSize) throws IOException {
        DataInputStream mmInStream = new DataInputStream(inputStream);
        byte[] buffer = new byte[chunkSize];
        int readed = 0;
        readed = mmInStream.read(buffer);
        for (int i = 0; i < readed; i++){
            readChar(buffer[i]);
        }
        return readed;
    }

    public void onError(Throwable e) {
    }

    public void resetBodyBytes(){
        try {
            bodyBytes.close();
            bodyBytes.reset();
            bodyBytes.flush();
        } catch (Exception e) {

        }
        bodyBytes = new ByteArrayOutputStream();
        readedBytes = 0;
    }

    public void flush(){
        resetBodyBytes();
        headersReaded = false;
    }

}

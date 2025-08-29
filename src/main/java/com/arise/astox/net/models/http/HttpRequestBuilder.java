package com.arise.astox.net.models.http;

import com.arise.astox.net.models.ServerRequestBuilder;
import com.arise.core.models.Handler;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.Util;

import java.io.*;
import java.util.UUID;


public class HttpRequestBuilder extends ServerRequestBuilder<HttpRequest> {
    @Override
    public void readInputStream(InputStream in, Handler<HttpRequest> onComplete, Handler<Throwable> onError) {

        HttpRequest request = new HttpRequest();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {



            int sumRead = 0;
            byte[] buffer = new byte[9999];
            int actualRead  = in.read(buffer);
            sumRead = sumRead + actualRead;
            readChunk(buffer, baos, request);


           //aici intra numai daca are si body cu content-length definit
            if(request.getContentLength() > 0){
                while (sumRead < request.getContentLength() + request.getHeaderLength() ){
                    actualRead  = in.read(buffer);

                    readChunk(buffer, baos, request);
                    System.out.println("readed = " + actualRead);
                    sumRead += actualRead;
//                baos.write(buffer, 0, readed);
                }

            }


           onComplete.handle(request);

        } catch (Exception e) {
           onError.handle(e);
        }
    }

    String getTitleFromHeaderLine(String headerLine){
        String headerLines[] = headerLine.split("\n");

        for (String hline: headerLines){
            if(hline.indexOf(":") > -1){
                String hparts[] = hline.split(":");
                if (hparts.length > 1){
                    String value = hparts[1].trim();
                    String headerKey = hparts[0].trim();

                    if (value.indexOf(";") > -1 ){
                        String vparts[] = value.split(";");

                        if (vparts.length > 0){
                            for (String vp: vparts){
                                if (vp.indexOf("=") > -1){
                                    String kv[] = vp.trim().split("=");
                                    if (kv.length > 1) {
                                        String key = kv[0].trim();
                                        String val = kv[1].trim();
                                        if ("filename".equalsIgnoreCase(key)){
                                            return val.replaceAll("\"", "");
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


    private boolean readChunk(byte[] buffer, ByteArrayOutputStream bodyBytes, HttpRequest request) throws Exception {



        for (int i = 0; i < buffer.length; i++){


            if (!request.isHeaderReadComplete() ){
                if(buffer[i] == '\n') {
                    String line = bodyBytes.toString("UTF-8").trim();
                    if(line.equalsIgnoreCase("")){
                        request.setHeaderReadComplete(true);
                        bodyBytes.reset();
                    } else {
                        request.putLine(line);
                        bodyBytes.reset();
                    }
                }
                else {
                    bodyBytes.write(buffer[i]);
                }
            }

            else {
                if(request.isMultipartFormData()){



                    if (request.mFileCanRead()){
                        request.mfileWrite(bodyBytes.toByteArray());
                    }

                    else if (buffer[i] == '\n'){

                        String line = bodyBytes.toString("UTF-8").trim();

                        if(line.endsWith(request.getBoundary())){
                            request.startNewMFile();
                        }
                        else if (line.toLowerCase().indexOf("content-disposition") > -1 && request.hasMfile()){
                            request.setMfileName(
                                    getTitleFromHeaderLine(line)
                            );
                        }
                        else if (line.equalsIgnoreCase("") && request.hasMfile()){
                            request.setMfileCanRead(true);

                        }
                        System.out.println("multipart line: " + line);
                        bodyBytes.reset();
                    }
                    else {
                        bodyBytes.write(buffer[i]);
                    }

                } /// exit multipart


                else {
                    bodyBytes.write(buffer[i]);
                }



            }//exit else body

        }




        return false;
    }



//    @Override
//    public void readInputStream(final InputStream inputStream,
//                                final Handler<HttpRequest> onSuccess,
//                                final Handler<Throwable> onError) {
//        HttpRequestReader reader = new HttpRequestReader() {
//            @Override
//            public void handleRest(HttpReader x) {
//                    getRequest().setBytes(bodyBytes.toByteArray());
//                    onSuccess.handle(this.getRequest());
//                    flush();
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                onError.handle(e);
//            }
//        };
//        reader.readInputStream(inputStream);
//    }






}
